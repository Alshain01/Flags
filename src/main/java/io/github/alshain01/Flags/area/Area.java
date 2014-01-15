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

package io.github.alshain01.Flags.area;

import io.github.alshain01.Flags.Flag;
import io.github.alshain01.Flags.Flags;
import io.github.alshain01.Flags.Message;
import io.github.alshain01.Flags.SystemType;
import io.github.alshain01.Flags.economy.EBaseValue;
import io.github.alshain01.Flags.economy.EPurchaseType;
import io.github.alshain01.Flags.economy.ETransactionType;
import io.github.alshain01.Flags.events.FlagChangedEvent;
import io.github.alshain01.Flags.events.MessageChangedEvent;
import io.github.alshain01.Flags.events.TrustChangedEvent;

import java.util.Set;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.Plugin;

public abstract class Area implements Comparable<Area> {
	/*
	 * Checks to make sure the player can afford the item. If false, the player
	 * is automatically notified.
	 */
	private static boolean isFundingLow(EPurchaseType product, Flag flag, Player player) {
		final double price = flag.getPrice(product);

		if (price > Flags.getEconomy().getBalance(player.getName())) {
			player.sendMessage(Message.LowFunds
					.get()
					.replaceAll("\\{PurchaseType\\}",
							product.getLocal().toLowerCase())
					.replaceAll("\\{Price\\}", Flags.getEconomy().format(price))
					.replaceAll("\\{Flag\\}", flag.getName()));
			return true;
		}
		return false;
	}

	/*
	 * Makes the final purchase transaction.
	 */
	private static boolean makeTransaction(ETransactionType transaction,
			EPurchaseType product, Flag flag, Player player) {
		final double price = flag.getPrice(product);

		final EconomyResponse r = transaction == ETransactionType.Withdraw ? Flags
				.getEconomy().withdrawPlayer(player.getName(), price) // Withdrawal
				: Flags.getEconomy().depositPlayer(player.getName(), price); // Deposit

		if (r.transactionSuccess()) {
			player.sendMessage(transaction.getMessage().replaceAll(
					"\\{Price\\}", Flags.getEconomy().format(price)));
			return true;
		}

		// Something went wrong if we made it this far.
		Flags.severe(String.format("An error occurred: %s", r.errorMessage));
		player.sendMessage(Message.Error.get().replaceAll("\\{Error\\}",
				r.errorMessage));
		return false;
	}
	
	/**
	 * Gets an area from the data store at a specific location.
	 * 
	 * @param location
	 *            The location to request an area.
	 * @return An Area from the configured system or the world if no area is
	 *         defined.
	 */
	public static Area getAt(Location location) {
		if (!Area.hasArea(location)) {
			return new World(location);
		}
		
		Area area = null;
		
		switch (SystemType.getActive()) {
		case GRIEF_PREVENTION:
			final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("GriefPrevention");
			final float pluginVersion = Float.valueOf(plugin.getDescription().getVersion().substring(0, 3));

			if (pluginVersion >= (float)7.8) {
				area = new GriefPreventionClaim78(location);
			} else if (pluginVersion == (float)7.7) {
				area = new GriefPreventionClaim(location);
			}
			break;
		case WORLDGUARD:
			area = new WorldGuardRegion(location);
			break;
		case RESIDENCE:
			area = new ResidenceClaimedResidence(location);
			break;
		case INFINITEPLOTS:
			area = new InfinitePlotsPlot(location);
			break;
		case FACTIONS:
			area = new FactionsTerritory(location);
			break;
		case PLOTME:
			area = new PlotMePlot(location);
			break;
		case PRECIOUSSTONES:
			area = new PreciousStonesField(location);
			break;
		case REGIOS:
			area = new RegiosRegion(location);
			break;
		case WORLD:
			area = new World(location);
			break;
		default:
			break;
		}
		return area != null && area.isArea() ? area : new World(location);
	}
	
	/**
	 * Gets an area by system specific name. The name is formatted based on the
	 * system.
	 * 
	 * GriefPrevention = ID number
	 * WorldGuard = WorldName.RegionName
	 * Regios = Region name
	 * Residence = Residence name OR ResidenceName.Sub-zoneName
	 * PreciousStones = WorldName.ID
	 * InfinitePlots = WorldName.PlotLoc (X;Z)
	 * Factions = WorldName.FactionID
	 * PlotMe = WorldName.PlotID
	 * 
	 * @param name
	 *            The system specific name of the area or world name
	 * @return The Area requested, may be null in cases of invalid system
	 *         selection.
	 */
	public static Area get(String name) {
		String[] path;
		Area area = null;
		
		switch (SystemType.getActive()) {
		case GRIEF_PREVENTION:
			final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("GriefPrevention");
			final float pluginVersion = Float.valueOf(plugin.getDescription().getVersion().substring(0, 3));
			final Long ID = Long.parseLong(name);
			
			if (pluginVersion >= (float)7.8) {
				area = new GriefPreventionClaim78(ID);
			} else if (pluginVersion == (float)7.7) {
				area = new GriefPreventionClaim(ID);
			}
			break;
		case RESIDENCE:
			area = new ResidenceClaimedResidence(name);
			break;
		case WORLDGUARD:
			path = name.split("\\.");
			area = new WorldGuardRegion(Bukkit.getWorld(path[0]), path[1]);
			break;
		case INFINITEPLOTS:
			path = name.split("\\.");
			final String[] coordinates = path[1].split(";");
			area = new InfinitePlotsPlot(Bukkit.getWorld(path[0]), Integer.valueOf(coordinates[0]), Integer.valueOf(coordinates[1]));
			break;
		case FACTIONS:
			path = name.split("\\.");
			area = new FactionsTerritory(Bukkit.getWorld(path[0]), path[1]);
			break;
		case PRECIOUSSTONES:
			path = name.split("\\.");
			area = new PreciousStonesField(Bukkit.getWorld(path[0]), Long.valueOf(path[1]));
			break;
		case PLOTME:
			path = name.split("\\.");
			area = new PlotMePlot(Bukkit.getWorld(path[0]), path[1]);
			break;
		case REGIOS:
			area = new RegiosRegion(name);
			break;
		case WORLD:
			area = new World(Bukkit.getWorld(name));
			break;
		default:
			break;
		}
		return area;
	}
	
	/**
	 * Gets whether there is a non world area that Flags can use at the location
	 * 
	 * @param location The location to check for an area
	 * @return True if there is an area
	 */
	public static boolean hasArea(Location location) {
		switch (SystemType.getActive()) {
		case GRIEF_PREVENTION:
			return GriefPreventionClaim.hasClaim(location);
		case WORLDGUARD:
			return WorldGuardRegion.hasRegion(location);
		case RESIDENCE:
			return ResidenceClaimedResidence.hasResidence(location);
		case INFINITEPLOTS:
			return InfinitePlotsPlot.hasPlot(location);
		case FACTIONS:
			return FactionsTerritory.hasTerritory(location);
		case PRECIOUSSTONES:
			return PreciousStonesField.hasField(location);
		case PLOTME:
			return PlotMePlot.hasPlot(location);
		case REGIOS:
			return RegiosRegion.hasRegion(location);
		default:
			return false;
		}
	}

	/**
	 * Gets the friendly name of the area type.
	 * 
	 * @return the area's type as a user friendly name.
	 */
	public abstract String getAreaType();

	/**
	 * Gets the message associated with a player flag. Translates the color
	 * codes and populates instances of {AreaType} and {Owner}
	 * 
	 * @param flag
	 *            The flag to retrieve the message for.
	 * @return The message associated with the flag.
	 */
	public final String getMessage(Flag flag) {
		return getMessage(flag, true);
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
	public String getMessage(Flag flag, boolean parse) {
		if (!isArea()) {
			return null;
		}
		String message = Flags.getDataStore().readMessage(this, flag);

		if (message == null) {
			message = new Default(getWorld()).getMessage(flag);
		}

		if (parse) {
			message = message.replaceAll("\\{AreaType\\}",
					getAreaType().toLowerCase()).replaceAll("\\{Owner\\}",
					getOwners().toArray()[0].toString());
			message = ChatColor.translateAlternateColorCodes('&', message);
		}
		return message;
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
		return getMessage(flag, true).replaceAll("\\{Player\\}", playerName);
	}

	/**
	 * Gets a set of owners for the area. On many systems, there will only be
	 * one.
	 * 
	 * @return the player name of the area owner.
	 */
	public abstract Set<String> getOwners();

	/**
	 * Returns the system type that this object belongs to.
	 * 
	 * @return The LandSystem that created this object (null for Default)
	 */
	public abstract SystemType getType();

	/**
	 * Gets the land system's ID for this area.
	 * 
	 * @return the area's ID in the format provided by the land management
	 *         system.
	 */
	public abstract String getSystemID();

	/**
	 * Gets a list of trusted players
	 * 
	 * @param flag
	 *            The flag to retrieve the trust list for.
	 * @return The list of players
	 */
	public final Set<String> getTrustList(Flag flag) {
		if (!isArea()) {
			return null;
		}

		final Set<String> trustedPlayers = Flags.getDataStore().readTrust(this,	flag);
		if (!(this instanceof Default || this instanceof World)) {
			for(String owner: getOwners()) {
				trustedPlayers.add(owner.toLowerCase());
			}
		}
		return trustedPlayers;
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
		if (!isArea()) {
			return null;
		}

		Boolean value = Flags.getDataStore().readFlag(this, flag);
		if (absolute) {
			return value;
		}

		return value != null ? value : new Default(getWorld()).getValue(flag, false);
	}

	/**
	 * Gets the world for the area.
	 * 
	 * @return the world associated with the area.
	 */
	public abstract org.bukkit.World getWorld();

	/**
	 * Checks the players permission to set bundles at this location
	 * 
	 * @param p
	 *            The player to check.
	 * @return true if the player has permissions.
	 */
	public boolean hasBundlePermission(Permissible p) {
		if (!isArea()) {
			return false;
		}

		if (p instanceof HumanEntity
				&& getOwners().contains(((HumanEntity) p).getName())) {
			return p.hasPermission("flags.command.bundle.set");
		}

		if (this instanceof Administrator
				&& ((Administrator) this).isAdminArea()) {
			return p.hasPermission("flags.area.bundle.admin");
		}

		return p.hasPermission("flags.area.bundle.others");
	}

	/**
	 * Checks the players permission to set flags at this location.
	 * 
	 * @param p
	 *            The player to check.
	 * @return true if the player has permissions.
	 */
	public boolean hasPermission(Permissible p) {
		if (!isArea()) {
			return false;
		}

		if (p instanceof HumanEntity
				&& getOwners().contains(((HumanEntity) p).getName())) {
			return p.hasPermission("flags.command.flag.set");
		}

		if (this instanceof Administrator
				&& ((Administrator) this).isAdminArea()) {
			return p.hasPermission("flags.area.flag.admin");
		}

		return p.hasPermission("flags.area.flag.others");
	}

	/**
	 * Checks if area exists on the server and cam be flagged.
	 * 
	 * @return true if the area exists.
	 */
	public abstract boolean isArea();

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
		if (!isArea()) {
			return false;
		}

		ETransactionType transaction = null;

		// Check to see if this is a purchase or deposit
		if (Flags.getEconomy() != null // No economy
				&& sender != null
				&& sender instanceof Player // Need a player to charge
				&& flag.getPrice(EPurchaseType.Message) != 0 // No defined price
				&& !(this instanceof World) // No charge for world flags
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
					if (isFundingLow(EPurchaseType.Message, flag,
                            (Player) sender)) {
						return false;
					}
					transaction = ETransactionType.Withdraw;
				}
			} else {
				// Check whether or not to refund the account
				if (EPurchaseType.Message.isRefundable()) {
					// Make sure the message we are refunding isn't identical to
					// the default message
					if (!getMessage(flag, false).equals(
							flag.getDefaultAreaMessage())) {
						transaction = ETransactionType.Deposit;
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
			if (!makeTransaction(transaction, EPurchaseType.Message, flag,
					(Player) sender)) {
				return true;
			}
		}
		Flags.getDataStore().writeMessage(this, flag, message);
		return true;
	}

	/**
	 * Adds or removes a player from the trust list.
	 * 
	 * @param flag
	 *            The flag to change trust for.
	 * @param trustee
	 *            The player being trusted or distrusted
	 * @param trusted
	 *            True if adding to the trust list, false if removing.
	 * @param sender
	 *            CommandSender for event, may be null if no associated player
	 *            or console.
	 * @return True if successful.
	 */
	public final boolean setTrust(Flag flag, String trustee, boolean trusted, CommandSender sender) {
		if (!isArea()) {
			return false;
		}

		final Set<String> trustList = Flags.getDataStore().readTrust(this, flag);

		// Set player to trusted.
		if (trusted) {
			if (trustList.contains(trustee.toLowerCase())) {
				return false;
			}
			trustList.add(trustee.toLowerCase());

			final TrustChangedEvent event = 
					new TrustChangedEvent(this, flag, trustee, true, sender);
			Bukkit.getServer().getPluginManager().callEvent(event);
			if (event.isCancelled()) {
				return false;
			}

			// Set the list
			Flags.getDataStore().writeTrust(this, flag, trustList);
			return true;
		}

        Flags.log("Removing Player from trust", true);
		// Remove player from trusted.
		if (!trustList.contains(trustee.toLowerCase())) {
            Flags.log("Player not found in trust list", true);
			return false;
		}

		final TrustChangedEvent event = new TrustChangedEvent(this, flag, trustee, false, sender);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
            Flags.log("TrustChangedEvent Cancelled", true);
			return false;
		}
        Flags.log("Removing " + trustee.toLowerCase(), true);
		trustList.remove(trustee.toLowerCase());
        Flags.log("Writing trust list to data store", true);
		Flags.getDataStore().writeTrust(this, flag, trustList);
		return true;
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
		if (!isArea()) {
			return false;
		}

		// Check to see if this can be paid for
		ETransactionType transaction = null;
		if (Flags.getEconomy() != null // No economy
				&& sender != null
				&& sender instanceof Player // Need a player to charge
				&& value != getValue(flag, true) // The flag isn't actually
													// changing
				&& flag.getPrice(EPurchaseType.Flag) != 0 // No defined price
				&& !(this instanceof World) // No charge for world flags
				&& !(this instanceof Default) // No charge for defaults
				&& !(this instanceof Administrator && ((Administrator) this)
						.isAdminArea())) // No charge for admin areas
		{
			if (value != null
					&& (EBaseValue.ALWAYS.isSet()
							|| EBaseValue.PLUGIN.isSet()
							&& (getValue(flag, true) == null || getValue(flag,
									true) != flag.getDefault()) || EBaseValue.DEFAULT
							.isSet()
							&& getValue(flag, true) != new Default(
									((Player) sender).getLocation().getWorld())
									.getValue(flag, true))) {
				// The flag is being set, see if the player can afford it.
				if (isFundingLow(EPurchaseType.Flag, flag,
                        (Player) sender)) {
					return false;
				}
				transaction = ETransactionType.Withdraw;
			} else {
				// Check whether or not to refund the account for setting the
				// flag value
				if (EPurchaseType.Flag.isRefundable()
						&& !EBaseValue.ALWAYS.isSet()) {
					transaction = ETransactionType.Deposit;
				}
			}
		}

		final FlagChangedEvent event = new FlagChangedEvent(this, flag, sender,
				value);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return false;
		}

		// Delay making the transaction in case the event is cancelled.
		if (transaction != null) {
			if (!makeTransaction(transaction, EPurchaseType.Flag, flag,
					(Player) sender)) {
				return true;
			}
		}

		final Boolean val = value == null ? null : value;
		Flags.getDataStore().writeFlag(this, flag, val);
		return true;
	}
}