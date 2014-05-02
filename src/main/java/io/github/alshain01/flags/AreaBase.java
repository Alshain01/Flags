/* Copyright 2013 Kevin Seiden. All rights reserved.

This works is licensed under the Creative Commons Attribution-NonCommercial 3.0

You are Free to:
   to Share: to copy, distribute and transmit the work
   to Remix: to adapt the work

Under the following conditions:
   Attribution: You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
   Non-commercial: You may not use this work for commercial purposes.

With the understanding that:
   Waiver: Any of the above conditions can be waived if you get permission from the copyright holder.
   Public Domain: Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
   Other Rights: In no way are any of the following rights affected by the license:
       Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
       The author's moral rights;
       Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.

Notice: For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
http://creativecommons.org/licenses/by-nc/3.0/
*/

package io.github.alshain01.flags;

import io.github.alshain01.flags.api.Flag;
import io.github.alshain01.flags.api.FlagsAPI;
import io.github.alshain01.flags.api.area.Area;
import io.github.alshain01.flags.api.area.Administrator;
import io.github.alshain01.flags.api.area.Ownable;
import io.github.alshain01.flags.api.area.Subdividable;
import io.github.alshain01.flags.api.economy.EconomyBaseValue;
import io.github.alshain01.flags.api.economy.EconomyPurchaseType;
import io.github.alshain01.flags.api.economy.EconomyTransactionType;
import io.github.alshain01.flags.api.event.FlagChangedEvent;
import io.github.alshain01.flags.api.event.FlagMessageChangedEvent;
import io.github.alshain01.flags.api.event.FlagPermissionTrustChangedEvent;
import io.github.alshain01.flags.api.event.FlagPlayerTrustChangedEvent;

import java.util.*;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Class for base functions of a specific area.
 */
abstract class AreaBase implements Area, Comparable<Area> {
    AreaBase() { }

    @Override
    public boolean getState(@Nonnull Flag flag) {
        return getState(flag, false);
    }

    @Override
    public Boolean getAbsoluteState(@Nonnull Flag flag) {
        return getState(flag, true);
    }

    Boolean getState(@Nonnull Flag flag, boolean absolute) {
        Boolean value = Flags.getDataStore().readFlag(this, flag);
        return absolute || value != null ? value : FlagsAPI.getWildernessArea(getWorld()).getState(flag);
    }

    @Override
    public final boolean setState(@Nonnull Flag flag, @Nullable Boolean value, @Nullable CommandSender sender) {
        // Check to see if this can be paid for
        EconomyTransactionType transaction = null;
        if (Flags.getEconomy() != null // No economy
                && sender != null
                && sender instanceof Player // Need a player to charge
                && value != getState(flag, true) // The flag isn't actually
                // changing
                && flag.getPrice(EconomyPurchaseType.FLAG) != 0 // No defined price
                && !(this instanceof AreaWilderness) // No charge for world flags
                && !(this instanceof AreaDefault) // No charge for defaults
                && !(this instanceof Administrator && ((Administrator) this)
                .isAdminArea())) // No charge for admin areas
        {
            if (value != null
                    && (EconomyBaseValue.ALWAYS.isSet()
                    || EconomyBaseValue.PLUGIN.isSet()
                    && (getState(flag, true) == null || getState(flag, true) != flag.getDefault())
                    || EconomyBaseValue.DEFAULT.isSet()
                    && getState(flag, true) != FlagsAPI.getDefaultArea(((Player) sender).getLocation().getWorld())
                    .getAbsoluteState(flag))) {
                // The flag is being set, see if the player can afford it.
                if (isFundingLow(EconomyPurchaseType.FLAG, flag,
                        (Player) sender)) {
                    return false;
                }
                transaction = EconomyTransactionType.WITHDRAW;
            } else {
                // Check whether or not to refund the account for setting the
                // flag value
                if (EconomyPurchaseType.FLAG.isRefundable()
                        && !EconomyBaseValue.ALWAYS.isSet()) {
                    transaction = EconomyTransactionType.DEPOSIT;
                }
            }
        }

        final FlagChangedEvent event = new FlagChangedEvent(this, flag, sender, value);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) { return false; }

        // Delay making the transaction in case the event is cancelled.
        if (transaction != null) {
            if (failedTransaction(transaction, EconomyPurchaseType.FLAG, flag,
                    (Player) sender)) {
                return true;
            }
        }

        final Boolean val = value == null ? null : value;
        Flags.getDataStore().writeFlag(this, flag, val);
        return true;
    }

    @Override
    public final String getMessage(@Nonnull Flag flag) {
        return getMessage(flag, false, true);
    }

    @Override
    public final String getAbsoluteMessage(@Nonnull Flag flag) {
        return getMessage(flag, true, true);
    }

    @Override
    public final String getMessage(@Nonnull Flag flag, @Nonnull String playerName) {
        return getMessage(flag, false, true).replace("{Player}", playerName);
    }

    @Override
    public final String getAbsoluteMessage(@Nonnull Flag flag, @Nonnull String playerName) {
        String message = getMessage(flag, true, true);
        return message == null ? null : message.replace("{Player}", playerName);
    }

    @Override
    public final String getRawMessage(@Nonnull Flag flag) {
        return getMessage(flag, false, false);
    }

    @Override
    public final String getAbsoluteRawMessage(@Nonnull Flag flag) {
        return getMessage(flag, true, false);
    }

    String getMessage(@Nonnull Flag flag, boolean absolute, boolean parse) {
		String message = Flags.getDataStore().readMessage(this, flag);

		if (message == null) {
            if(absolute) return null;
			message = FlagsAPI.getDefaultArea(getWorld()).getMessage(flag);
		}

		if (parse) {
			message = message
                    .replace("{World}", getWorld().getName())
                    .replace("{AreaType}", getAreaPlugin().getCuboidName().toLowerCase())
                    .replace("{AreaName}", this.getName());

            if(this instanceof Ownable) {
                message = message.replace("{Owner}", new ArrayList<OfflinePlayer>(((Ownable) this).getOwners()).get(0).getName());
            } else {
                message = message.replace("{Owner}", "the administrator");
            }
			message = ChatColor.translateAlternateColorCodes('&', message);
		}
		return message;
	}

    @Override
    public final boolean setMessage(@Nonnull Flag flag, @Nullable String message, @Nullable CommandSender sender) {
        EconomyTransactionType transaction = null;

        // Check to see if this is a purchase or deposit
        if (Flags.getEconomy() != null // No economy
                && sender != null
                && sender instanceof Player // Need a player to charge
                && flag.getPrice(EconomyPurchaseType.MESSAGE) != 0 // No defined price
                && !(this instanceof AreaWilderness) // No charge for world flags
                && !(this instanceof AreaDefault) // No charge for defaults
                && !(this instanceof Administrator && ((Administrator) this)
                .isAdminArea())) // No charge for admin areas
        {
            // Check to make sure we aren't removing the message
            if (message != null) {
                // Check to make sure the message isn't identical to what we
                // have
                // (if they are just correcting caps, don't charge, I hate
                // discouraging bad spelling & grammar)
                if (!getAbsoluteRawMessage(flag).equalsIgnoreCase(message)) {
                    if (isFundingLow(EconomyPurchaseType.MESSAGE, flag, (Player) sender)) {
                        return false;
                    }
                    transaction = EconomyTransactionType.WITHDRAW;
                }
            } else {
                // Check whether or not to refund the account
                if (EconomyPurchaseType.MESSAGE.isRefundable()) {
                    // Make sure the message we are refunding isn't identical to
                    // the default message
                    if (!getAbsoluteRawMessage(flag).equals(
                            flag.getDefaultAreaMessage())) {
                        transaction = EconomyTransactionType.DEPOSIT;
                    }
                }
            }
        }

        final FlagMessageChangedEvent event = new FlagMessageChangedEvent(this, flag, message, sender);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        // Delay making the transaction in case the event is cancelled.
        if (transaction != null) {
            if (failedTransaction(transaction, EconomyPurchaseType.MESSAGE, flag, (Player) sender)) {
                return true;
            }
        }

        Flags.getDataStore().writeMessage(this, flag, message);
        return true;
    }

    @Override
    public final Collection<OfflinePlayer> getPlayerTrust(@Nonnull Flag flag) {
        return getPlayerTrust(flag, false);
    }

    @Override
    public final Collection<OfflinePlayer> getAbsolutePlayerTrust(@Nonnull Flag flag) {
        return getPlayerTrust(flag, true);
    }

    Collection<OfflinePlayer> getPlayerTrust(@Nonnull Flag flag, boolean absolute) {
        Collection<OfflinePlayer> trust = Flags.getDataStore().readPlayerTrust(this, flag);
        if(!absolute && !(this instanceof AreaWilderness) && !(this instanceof AreaDefault)) {
            trust.addAll(FlagsAPI.getDefaultArea(getWorld()).getPlayerTrust(flag));
        }
        return trust;
    }

    @Override
    public final Collection<Permission> getPermissionTrust(@Nonnull Flag flag) {
        return getPermissionTrust(flag, false);
    }

    @Override
    public final Collection<Permission> getAbsolutePermissionTrust(@Nonnull Flag flag) {
        return getPermissionTrust(flag, true);
    }

    Collection<Permission> getPermissionTrust(@Nonnull Flag flag, boolean absolute) {
        Collection<Permission> trust = Flags.getDataStore().readPermissionTrust(this, flag);
        if(!absolute && !(this instanceof AreaWilderness) && !(this instanceof AreaDefault)) {
            trust.addAll(FlagsAPI.getDefaultArea(getWorld()).getPermissionTrust(flag));
        }
        return trust;
    }


    @Override
    public final boolean setTrust(@Nonnull Flag flag, @Nonnull OfflinePlayer trustee, @Nullable CommandSender sender) {
        final Set<OfflinePlayer> trustList = Flags.getDataStore().readPlayerTrust(this, flag);

        // Set player to trusted.
        if (trustList.contains(Bukkit.getOfflinePlayer(trustee.getUniqueId()))) {
            return false;
        }
        trustList.add(trustee);

        final FlagPlayerTrustChangedEvent event = new FlagPlayerTrustChangedEvent(this, flag, trustee, true, sender);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        Flags.getDataStore().writePlayerTrust(this, flag, trustList);
        return true;
    }


    @Override
    public final boolean setTrust(@Nonnull Flag flag, @Nonnull Permission permission, @Nullable CommandSender sender) {
        final Set<Permission> trustList = Flags.getDataStore().readPermissionTrust(this, flag);

        // Set player to trusted.
        if (trustList.contains(permission)) {
            return false;
        }
        trustList.add(permission);

        final FlagPermissionTrustChangedEvent event = new FlagPermissionTrustChangedEvent(this, flag, permission, true, sender);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        Flags.getDataStore().writePermissionTrust(this, flag, trustList);
        return true;
    }

    @Override
    public final boolean removeTrust(@Nonnull Flag flag, @Nonnull OfflinePlayer trustee, @Nullable CommandSender sender) {
        final Set<OfflinePlayer> trustList = Flags.getDataStore().readPlayerTrust(this, flag);

        // Remove player from trusted.
        if (!trustList.contains(Bukkit.getOfflinePlayer(trustee.getUniqueId()))) {
            return false;
        }
        trustList.remove(trustee);

        final FlagPlayerTrustChangedEvent event = new FlagPlayerTrustChangedEvent(this, flag, trustee, false, sender);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        Flags.getDataStore().writePlayerTrust(this, flag, trustList);
        return true;
    }

    @Override
    public final boolean removeTrust(@Nonnull Flag flag, @Nonnull Permission permission, @Nullable CommandSender sender) {
        final Set<Permission> trustList = Flags.getDataStore().readPermissionTrust(this, flag);

        // Remove player from trusted.
        if (!trustList.contains(permission)) {
            return false;
        }
        trustList.remove(permission);

        final FlagPermissionTrustChangedEvent event = new FlagPermissionTrustChangedEvent(this, flag, permission, false, sender);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        Flags.getDataStore().writePermissionTrust(this, flag, trustList);
        return true;
    }

    @Override
    public final boolean hasTrust(@Nonnull Flag flag, @Nonnull Player player) {
        if (player.hasPermission(flag.getBypassPermission())) return true;
        if (this instanceof Ownable && (((Ownable)this).getOwners().contains(Bukkit.getOfflinePlayer(player.getUniqueId())))) return true;

        Collection<OfflinePlayer> tl = getPlayerTrust(flag);
        if(tl.contains(Bukkit.getOfflinePlayer(player.getUniqueId()))) return true;

        Collection<Permission> pTl = getPermissionTrust(flag);
        for(Permission p : pTl) {
            if(player.hasPermission(p)) return true;
        }

        return false;
    }

    @Override
    public boolean hasFlagPermission(@Nonnull Permissible p) {
        if (this instanceof Ownable && p instanceof Player
                && (((Ownable)this).getOwners().contains(Bukkit.getOfflinePlayer(((Player)p).getUniqueId())))) {
            return p.hasPermission("flags.command.flag.set");
        }

        if (this instanceof Administrator
                && ((Administrator) this).isAdminArea()) {
            return p.hasPermission("flags.area.flag.admin");
        }

        return p.hasPermission("flags.area.flag.others");
    }

    @Override
	public boolean hasBundlePermission(@Nonnull Permissible p) {
		if (this instanceof Ownable && p instanceof Player
                && (((Ownable)this).getOwners().contains(Bukkit.getOfflinePlayer(((Player)p).getUniqueId())))) {
			return p.hasPermission("flags.command.bundle.set");
		}

		if (this instanceof Administrator
				&& ((Administrator) this).isAdminArea()) {
			return p.hasPermission("flags.area.bundle.admin");
		}

		return p.hasPermission("flags.area.bundle.others");
	}

    @Override
    final public AreaRelationship getRelationship(@Nonnull Area area) {
        if (area.getClass().equals(this.getClass())) {
            if (getId().equals(area.getId())) return AreaRelationship.EQUAL;

            if (this instanceof Subdividable) {
                Subdividable thisSub = (Subdividable) this;
                Subdividable areaSub = (Subdividable) area; // The earlier class check guarantees this cast
                if (thisSub.isSubdivision()) {
                    if (areaSub.isSubdivision() && areaSub.getParent().getId().equals(thisSub.getParent().getId()))
                        return AreaRelationship.SIBLING;
                    if (thisSub.getParent().getId().equals(area.getId()))
                        return AreaRelationship.CHILD;
                } else if (areaSub.isSubdivision() && areaSub.getParent().getId().equals(getId())) {
                    return AreaRelationship.PARENT;
                }
            }
        }
        return AreaRelationship.UNRELATED;
    }

    @Override
    final public int compareTo(@Nonnull Area area) {
        return this.getId().compareTo(area.getId());
    }

    @Override
    final public int compareNameTo(@Nonnull Area area) { return this.getName().compareTo(area.getName()); }

    /*
     * Checks to make sure the player can afford the item. If false, the player
     * is automatically notified.
     */
    private static boolean isFundingLow(EconomyPurchaseType product, Flag flag, Player player) {
        final double price = flag.getPrice(product);

        if (price > Flags.getEconomy().getBalance(player.getName())) {
            player.sendMessage(Message.LOW_FUNDS.get()
                    .replace("{PurchaseType}", product.getLocal().toLowerCase())
                    .replace("{Price}", Flags.getEconomy().format(price))
                    .replace("{Flag}", flag.getName()));
            return true;
        }
        return false;
    }

    /*
     * Makes the final purchase transaction.
     */
    private static boolean failedTransaction(EconomyTransactionType transaction,
                                           EconomyPurchaseType product, Flag flag, Player player) {
        final double price = flag.getPrice(product);

        final EconomyResponse r = transaction == EconomyTransactionType.WITHDRAW ? Flags
                .getEconomy().withdrawPlayer(player.getName(), price) // Withdrawal
                : Flags.getEconomy().depositPlayer(player.getName(), price); // Deposit

        if (r.transactionSuccess()) {
            player.sendMessage(transaction.getMessage().replace("{Price}", Flags.getEconomy().format(price)));
            return false;
        }

        // Something went wrong if we made it this far.
        Bukkit.getPluginManager().getPlugin("Flags").getLogger().warning(String.format("[Economy Error] %s", r.errorMessage));
        player.sendMessage(Message.ERROR.get().replace("{Error}", r.errorMessage));
        return true;
    }
}
