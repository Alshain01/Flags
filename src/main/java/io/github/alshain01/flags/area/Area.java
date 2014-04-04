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

package io.github.alshain01.flags.area;

import io.github.alshain01.flags.*;
import io.github.alshain01.flags.System;
import io.github.alshain01.flags.economy.EconomyBaseValue;
import io.github.alshain01.flags.economy.EconomyPurchaseType;
import io.github.alshain01.flags.economy.EconomyTransactionType;
import io.github.alshain01.flags.events.FlagChangedEvent;
import io.github.alshain01.flags.events.MessageChangedEvent;
import io.github.alshain01.flags.events.PermissionTrustChangedEvent;
import io.github.alshain01.flags.events.PlayerTrustChangedEvent;
import io.github.alshain01.flags.exception.InvalidAreaException;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.milkbowl.vault.economy.EconomyResponse;

import org.apache.commons.lang.Validate;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;

import javax.annotation.Nonnull;

/**
 * Class for base functions of a specific area.
 */
public abstract class Area implements Comparable<Area> {
    Area() { }

    /**
     * Returns the system type that this object belongs to.
     *
     * @return The Cuboid System that created this object
     */
    public abstract CuboidType getCuboidType();

    /**
     * Checks if the underlying object from the cuboid system is null.
     *
     * @return true if the area is not null.
     */
    public abstract boolean isArea();

    /**
     * Returns a unique id of the cuboid area,
     * if supported by the cuboid system.
     * Otherwise null.
     *
     * @return The UUID for the area or null.
     * @throws InvalidAreaException
     */
    protected abstract UUID getUniqueId();

    /**
     * Gets the cuboid system's ID for this area.
     *
     * @return the area's ID in the format provided by the cuboid system.
     * @throws InvalidAreaException
     */
    @SuppressWarnings("WeakerAccess") // API
    public abstract String getId();


    /**
     * Returns the name of the cuboid defined in the system.
     * If the system does not support naming, the ID will be returned.
     *
     * @return The LandSystem that created this object
     * @throws InvalidAreaException
     */
    @SuppressWarnings("WeakerAccess") // API
    public abstract String getName();


    /**
     * Gets the world for the area.
     *
     * @return the world associated with the area.
     * @throws InvalidAreaException
     */
    public abstract org.bukkit.World getWorld();

    /**
     * Gets a set of owner names for the area.
     * On many systems, there will only be one.
     *
     * @return the player name of the area owner.
     * @throws InvalidAreaException
     */
    public abstract Set<String> getOwnerNames();

    /**
     * Returns the description of the cuboid for the system.
     * i.e. Claim, Region, Residence, Field, Sector, etc.
     *
     * @return The cuboid descriptor for the cuboid system.
     */
    public String getCuboidDescriptor() {
        return getCuboidType().getCuboidName();
    }

    /**
     * Gets the value of the flag for this area.
     *
     * @param flag
     *            The flag to retrieve the value for.
     * @param absolute
     *            True if you want a null value if the flag is not defined.
     *            False if you want the inherited default (ensures not null).
     * @return The value of the flag or the inherited value of the flag from
     *         defaults if not defined.
     */
    public Boolean getValue(Flag flag, boolean absolute) {
        Validate.notNull(flag);

        Boolean value = Flags.getDataStore().readFlag(this, flag);
        if (absolute) { return value; }
        return value != null ? value : new Default(getWorld()).getValue(flag, false);
    }

    /**
     * Sets the value of the flag for this area.
     *
     * @param flag
     *            The flag to set the value for.
     * @param value
     *            The value to set, null to remove.
     * @param sender
     *            The command sender for event call and economy, may be null if
     *            no associated player or console.
     * @return False if the event was canceled.
     */
    public final boolean setValue(Flag flag, Boolean value, CommandSender sender) {
        Validate.notNull(flag);

        // Check to see if this can be paid for
        EconomyTransactionType transaction = null;
        if (Flags.getEconomy() != null // No economy
                && sender != null
                && sender instanceof Player // Need a player to charge
                && value != getValue(flag, true) // The flag isn't actually
                // changing
                && flag.getPrice(EconomyPurchaseType.Flag) != 0 // No defined price
                && !(this instanceof Wilderness) // No charge for world flags
                && !(this instanceof Default) // No charge for defaults
                && !(this instanceof Administrator && ((Administrator) this)
                .isAdminArea())) // No charge for admin areas
        {
            if (value != null
                    && (EconomyBaseValue.ALWAYS.isSet()
                    || EconomyBaseValue.PLUGIN.isSet()
                    && (getValue(flag, true) == null || getValue(flag, true) != flag.getDefault())
                    || EconomyBaseValue.DEFAULT.isSet()
                    && getValue(flag, true) != new Default(
                    ((Player) sender).getLocation().getWorld())
                    .getValue(flag, true))) {
                // The flag is being set, see if the player can afford it.
                if (isFundingLow(EconomyPurchaseType.Flag, flag,
                        (Player) sender)) {
                    return false;
                }
                transaction = EconomyTransactionType.Withdraw;
            } else {
                // Check whether or not to refund the account for setting the
                // flag value
                if (EconomyPurchaseType.Flag.isRefundable()
                        && !EconomyBaseValue.ALWAYS.isSet()) {
                    transaction = EconomyTransactionType.Deposit;
                }
            }
        }

        final FlagChangedEvent event = new FlagChangedEvent(this, flag, sender, value);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) { return false; }

        // Delay making the transaction in case the event is cancelled.
        if (transaction != null) {
            if (failedTransaction(transaction, EconomyPurchaseType.Flag, flag,
                    (Player) sender)) {
                return true;
            }
        }

        final Boolean val = value == null ? null : value;
        Flags.getDataStore().writeFlag(this, flag, val);
        return true;
    }

	/**
	 * Gets the message associated with a player flag. Translates the color
	 * codes and populates instances of {AreaType} and {Owner}
	 * 
	 * @param flag
	 *            The flag to retrieve the message for.
	 * @return The message associated with the flag.
	 */
	@SuppressWarnings("WeakerAccess") // API
    public final String getMessage(Flag flag) {
        return getMessage(flag, true);
    }

    /**
     * Gets the message associated with a player flag and parses {AreaType},
     * {Owner}, {World}, and {Player}
     *
     * @param flag
     *            The flag to retrieve the message for.
     * @param playerName
     *            The player name to insert into the message.
     * @return The message associated with the flag.
     */
    public final String getMessage(Flag flag, String playerName) {
        Validate.notNull(playerName);
        return getMessage(flag, true).replace("{Player}", playerName);
    }

	/**
	 * Gets the message associated with a player flag.
	 * 
	 * @param flag
	 *            The flag to retrieve the message for.
	 * @param parse
	 *            True if you wish to populate instances of {AreaType}, {Owner},
	 *            and {World} and translate color codes
	 * @return The message associated with the flag.
	 */
	@SuppressWarnings("WeakerAccess") // API
    public String getMessage(Flag flag, boolean parse) {
        Validate.notNull(flag);

		String message = Flags.getDataStore().readMessage(this, flag);

		if (message == null) {
			message = new Default(getWorld()).getMessage(flag);
		}

		if (parse) {
			message = message
                    .replace("{World}", getWorld().getName())
                    .replace("{AreaName}", getName())
                    .replace("{AreaType}", getCuboidType().getCuboidName().toLowerCase())
                    .replace("{Owner}", getOwnerNames().toArray()[0].toString());
			message = ChatColor.translateAlternateColorCodes('&', message);
		}
		return message;
	}

    /**
     * Sets or removes the message associated with a player flag.
     *
     * @param flag
     *            The flag to set the message for.
     * @param message
     *            The message to set, null to remove.
     * @param sender
     *            CommandSender for event, may be null if no associated player
     *            or console.
     * @return True if successful
     */
    public final boolean setMessage(Flag flag, String message, CommandSender sender) {
        Validate.notNull(flag);

        EconomyTransactionType transaction = null;

        // Check to see if this is a purchase or deposit
        if (Flags.getEconomy() != null // No economy
                && sender != null
                && sender instanceof Player // Need a player to charge
                && flag.getPrice(EconomyPurchaseType.Message) != 0 // No defined price
                && !(this instanceof Wilderness) // No charge for world flags
                && !(this instanceof Default) // No charge for defaults
                && !(this instanceof Administrator && ((Administrator) this)
                .isAdminArea())) // No charge for admin areas
        {
            // Check to make sure we aren't removing the message
            if (message != null) {
                // Check to make sure the message isn't identical to what we
                // have
                // (if they are just correcting caps, don't charge, I hate
                // discouraging bad spelling & grammar)
                if (!getMessage(flag, false).equalsIgnoreCase(message)) {
                    if (isFundingLow(EconomyPurchaseType.Message, flag, (Player) sender)) {
                        return false;
                    }
                    transaction = EconomyTransactionType.Withdraw;
                }
            } else {
                // Check whether or not to refund the account
                if (EconomyPurchaseType.Message.isRefundable()) {
                    // Make sure the message we are refunding isn't identical to
                    // the default message
                    if (!getMessage(flag, false).equals(
                            flag.getDefaultAreaMessage())) {
                        transaction = EconomyTransactionType.Deposit;
                    }
                }
            }
        }

        final MessageChangedEvent event = new MessageChangedEvent(this, flag, message, sender);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        // Delay making the transaction in case the event is cancelled.
        if (transaction != null) {
            if (failedTransaction(transaction, EconomyPurchaseType.Message, flag, (Player) sender)) {
                return true;
            }
        }

        Flags.getDataStore().writeMessage(this, flag, message);
        return true;
    }

    /**
     * Gets a list of trusted players
     *
     * @param flag
     *            The flag to retrieve the trust list for.
     * @return The list of players
     */
    public final Map<UUID, String> getPlayerTrustList(Flag flag) {
        Validate.notNull(flag);

        return Flags.getDataStore().readPlayerTrust(this, flag);
    }

    /**
     * Gets a list of trusted permissions
     *
     * @param flag
     *            The flag to retrieve the trust list for.
     * @return The list of permissions
     */
    @SuppressWarnings("unused") // API
    public final Set<Permission> getPermissionTrustList(Flag flag) {
        Validate.notNull(flag);

        return Flags.getDataStore().readPermissionTrust(this, flag);
    }

    /**
     * Adds player to a the trust list.
     *
     * @param flag
     *            The flag to change trust for.
     * @param trustee
     *            The player being trusted
     * @param sender
     *            CommandSender for event, may be null if no associated player
     *            or console.
     * @return True if successful.
     */
    public final boolean setPlayerTrust(Flag flag, Player trustee, CommandSender sender) {
        Validate.notNull(flag);
        Validate.notNull(trustee);

        final Map<UUID, String> trustList = Flags.getDataStore().readPlayerTrust(this, flag);

        // Set player to trusted.
        if (trustList.containsKey(trustee.getUniqueId())) {
            return false;
        }
        trustList.put(trustee.getUniqueId(), trustee.getName());

        final PlayerTrustChangedEvent event = new PlayerTrustChangedEvent(this, flag, trustee.getUniqueId(), true, sender);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        Flags.getDataStore().writePlayerTrust(this, flag, trustList);
        return true;
    }


    /**
     * Adds permission to a the trust list.
     *
     * @param flag
     *            The flag to change trust for.
     * @param permission
     *            The permission being trusted
     * @param sender
     *            CommandSender for event, may be null if no associated player
     *            or console.
     * @return True if successful.
     */
    public final boolean setPermissionTrust(Flag flag, String permission, CommandSender sender) {
        return setPermissionTrust(flag, new Permission(permission), sender);
    }

    /**
     * Adds permission to a the trust list.
     *
     * @param flag
     *            The flag to change trust for.
     * @param permission
     *            The permission being trusted
     * @param sender
     *            CommandSender for event, may be null if no associated player
     *            or console.
     * @return True if successful.
     */
    public final boolean setPermissionTrust(Flag flag, Permission permission, CommandSender sender) {
        Validate.notNull(flag);
        Validate.notNull(permission);

        final Set<Permission> trustList = Flags.getDataStore().readPermissionTrust(this, flag);

        // Set player to trusted.
        if (trustList.contains(permission)) {
            return false;
        }
        trustList.add(permission);

        final PermissionTrustChangedEvent event = new PermissionTrustChangedEvent(this, flag, permission, true, sender);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        Flags.getDataStore().writePermissionTrust(this, flag, trustList);
        return true;
    }

    /**
     * Removes a player from the trust list.
     *
     * @param flag
     *            The flag to change trust for.
     * @param trustee
     *            The player being distrusted
     * @param sender
     *            CommandSender for event, may be null if no associated player
     *            or console.
     * @return True if successful.
     */
    public final boolean removePlayerTrust(Flag flag, UUID trustee, CommandSender sender) {
        Validate.notNull(flag);
        Validate.notNull(trustee);

        final Map<UUID, String> trustList = Flags.getDataStore().readPlayerTrust(this, flag);

        // Remove player from trusted.
        if (!trustList.containsKey(trustee)) {
            return false;
        }
        trustList.remove(trustee);

        final PlayerTrustChangedEvent event = new PlayerTrustChangedEvent(this, flag, trustee, false, sender);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        Flags.getDataStore().writePlayerTrust(this, flag, trustList);
        return true;
    }

    /**
     * Removes a permission from the trust list.
     *
     * @param flag
     *            The flag to change trust for.
     * @param permission
     *            The permission being distrusted
     * @param sender
     *            CommandSender for event, may be null if no associated player
     *            or console.
     * @return True if successful.
     */
    public final boolean removePermissionTrust(Flag flag, String permission, CommandSender sender) {
        return removePermissionTrust(flag, new Permission(permission), sender);
    }

    /**
     * Removes a permission from the trust list.
     *
     * @param flag
     *            The flag to change trust for.
     * @param permission
     *            The permission being distrusted
     * @param sender
     *            CommandSender for event, may be null if no associated player
     *            or console.
     * @return True if successful.
     */
    public final boolean removePermissionTrust(Flag flag, Permission permission, CommandSender sender) {
        Validate.notNull(flag);
        Validate.notNull(permission);

        final Set<Permission> trustList = Flags.getDataStore().readPermissionTrust(this, flag);

        // Remove player from trusted.
        if (!trustList.contains(permission)) {
            return false;
        }
        trustList.remove(permission);

        final PermissionTrustChangedEvent event = new PermissionTrustChangedEvent(this, flag, permission, false, sender);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        Flags.getDataStore().writePermissionTrust(this, flag, trustList);
        return true;
    }

    /**
     * Returns true if the provided player is the area owner, has explicit trust, or permission trust.
     *
     * @param flag
     *            The flag to check the trust list for.
     * @param player
     *            The player to check trust for.
     * @return The list of permissions
     */
    @SuppressWarnings("unused") // API
    public final boolean hasTrust(Flag flag, Player player) {
        Validate.notNull(flag);
        Validate.notNull(player);

        if (getOwnerNames().contains(player.getName().toLowerCase())) { return true; }

        Map<UUID, String> tl = getPlayerTrustList(flag);
        if(tl.containsKey(player.getUniqueId())) {
            return true;
        }

        Set<Permission> pTl = getPermissionTrustList(flag);
        for(Permission p : pTl) {
            if(player.hasPermission(p)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks the players permission to set flags at this location.
     *
     * @param p
     *            The player to check.
     * @return true if the player has permissions.
     */
    public boolean hasPermission(Permissible p) {
        Validate.notNull(p);

        if (p instanceof HumanEntity
                && getOwnerNames().contains(((HumanEntity) p).getName())) {
            return p.hasPermission("flags.command.flag.set");
        }

        if (this instanceof Administrator
                && ((Administrator) this).isAdminArea()) {
            return p.hasPermission("flags.area.flag.admin");
        }

        return p.hasPermission("flags.area.flag.others");
    }

	/**
	 * Checks the players permission to set bundles at this location
	 * 
	 * @param p
	 *            The player to check.
	 * @return true if the player has permissions.
	 */
	public boolean hasBundlePermission(Permissible p) {
        Validate.notNull(p);

		if (p instanceof HumanEntity
				&& getOwnerNames().contains(((HumanEntity) p).getName())) {
			return p.hasPermission("flags.command.bundle.set");
		}

		if (this instanceof Administrator
				&& ((Administrator) this).isAdminArea()) {
			return p.hasPermission("flags.area.bundle.admin");
		}

		return p.hasPermission("flags.area.bundle.others");
	}

    /**
     * 0 if the the areas are the same
     * -1 if the area is a subdivision of the provided area.
     * 1 if the area is a parent of the provided area.
     * 2 if they are "sister" subdivisions.
     * 3 if they are completely unrelated.
     *
     * @return The value of the comparison.
     */
    @Override
    final public int compareTo(@Nonnull Area a) {
        Validate.notNull(a);
        if ((a.getClass().equals(this.getClass()))) {
            if (getId().equals(a.getId())) {
                return 0;
            }

            if (this instanceof Subdivision) {
                if (((Subdivision) this).isSubdivision()) {
                    if (((Subdivision) a).isSubdivision() && ((Subdivision) a).getParent().getId().equals(((Subdivision) this).getParent().getId()))
                        return 2;
                    if (((Subdivision) this).getParent().getId().equals(a.getId()))
                        return -1;
                } else if (((Subdivision) a).isSubdivision() && ((Subdivision) a).getParent().getId().equals(getId())) {
                    return 1;
                }
            }
        }
        return 3;
    }

    /*
     * Checks to make sure the player can afford the item. If false, the player
     * is automatically notified.
     */
    private static boolean isFundingLow(EconomyPurchaseType product, Flag flag, Player player) {
        final double price = flag.getPrice(product);

        if (price > Flags.getEconomy().getBalance(player.getName())) {
            player.sendMessage(Message.LowFunds.get()
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

        final EconomyResponse r = transaction == EconomyTransactionType.Withdraw ? Flags
                .getEconomy().withdrawPlayer(player.getName(), price) // Withdrawal
                : Flags.getEconomy().depositPlayer(player.getName(), price); // Deposit

        if (r.transactionSuccess()) {
            player.sendMessage(transaction.getMessage().replace("{Price}", Flags.getEconomy().format(price)));
            return false;
        }

        // Something went wrong if we made it this far.
        Bukkit.getPluginManager().getPlugin("Flags").getLogger().warning(String.format("[Economy Error] %s", r.errorMessage));
        player.sendMessage(Message.Error.get().replace("{Error}", r.errorMessage));
        return true;
    }

    /**
     * Gets a set of owners for the area. On many systems, there will only be
     * one.
     *
     * @return the player name of the area owner.
     * @throws InvalidAreaException
     * @deprecated Use getOwnerNames() instead.  Future UUID support
     */
    @Deprecated
    @SuppressWarnings("deprecation, unused")
    public Set<String> getOwners() {
        return getOwnerNames();
    }

    /**
     * Returns the system type that this object belongs to.
     *
     * @return The LandSystem that created this object
     * @deprecated Use getCuboidType instead
     */
    @SuppressWarnings("deprecation, unused")
    @Deprecated
    public abstract System getSystemType();

    /**
     * Gets the land system's ID for this area.
     *
     * @return the area's ID in the format provided by the land management
     *         system.
     * @throws InvalidAreaException
     * @deprecated Use getId() and getParent().getId() instead
     */
    @Deprecated
    public abstract String getSystemID();
}