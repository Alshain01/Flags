/* Copyright 2013 Kevin Seiden. All rights reserved.

 This works is licensed under the Creative Commons Attribution-NonCommercial 3.0

 You are Free to:
    to Share � to copy, distribute and transmit the work
    to Remix � to adapt the work

 Under the following conditions:
    Attribution � You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
    Non-commercial � You may not use this work for commercial purposes.

 With the understanding that:
    Waiver � Any of the above conditions can be waived if you get permission from the copyright holder.
    Public Domain � Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
    Other Rights � In no way are any of the following rights affected by the license:
        Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
        The author's moral rights;
        Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.

 Notice � For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
 http://creativecommons.org/licenses/by-nc/3.0/
 */

package alshain01.Flags;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import alshain01.Flags.economy.EPurchaseType;

/**
 * Represents a flag registered with the plug-in.
 * 
 * @author Alshain01
 */
public final class Flag {
	private final boolean def;
	private final String name;
	private final String description;

	private final boolean player;
	private final String area;
	private final String world;
	private final String plugin;

	/**
	 * Creates an instance of the Flag class.
	 * 
	 * @param name
	 *            The flag name
	 * @param description
	 *            The flag description
	 * @param def
	 *            The flag default value
	 * @param plugin
	 *            The flag group
	 * @param player
	 *            True if the flag is a player flag
	 * @param area
	 *            The default area message for the flag.
	 * @param world
	 *            The default world message for the flag.
	 */
	protected Flag(String name, String description, boolean def, String plugin,
			boolean player, String area, String world) {
		this.name = name;
		this.description = description;
		this.def = def;
		this.plugin = plugin;
		this.area = area;
		this.world = world;
		this.player = player;
	}

	/**
	 * Gets the bypass permission string.
	 * 
	 * @return The bypass permission string (flags.bypass.flagname)
	 */
	public Permission getBypassPermission() {
		final Permission perm = new Permission("flags.bypass." + name,
				"Grants ability to bypass the effects of the flag " + name,	PermissionDefault.FALSE);
		perm.addParent("flags.bypass", true);
		return perm;
	}

	/**
	 * Gets the plug-in default value of the flag.
	 * 
	 * @return The default value.
	 */
	public boolean getDefault() {
		return def;
	}

	/**
	 * Gets the default area message of the flag.
	 * 
	 * @return The default area message.
	 */
	public String getDefaultAreaMessage() {
		return area;
	}

	/**
	 * Gets the default world message of the flag
	 * 
	 * @return The default world message.
	 */
	public String getDefaultWorldMessage() {
		return world;
	}

	/**
	 * Gets the flag's description
	 * 
	 * @return The description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gets the group the flag is assigned to.
	 * 
	 * @return The group this flag is assigned to.
	 */
	public String getGroup() {
		return plugin;
	}

	/**
	 * Gets the name of the flag.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the flag permission string.
	 * 
	 * @return The permission string (flags.flagtype.flagname)
	 */
	public Permission getPermission() {
		final Permission perm = new Permission("flags.flag." + name,
				"Grants ability to use the flag " + name, PermissionDefault.FALSE);
		perm.addParent("flags.flag", true);
		return perm;
	}

	/**
	 * Gets the price of the flag or message.
	 * 
	 * @param type
	 *            The PurchaseType to get for this flag
	 * @return The price of the purchase.
	 */
	public double getPrice(EPurchaseType type) {
		return !Flags.getDataStore().isSet("Price." + type.toString() + "." + name) 
				? 0	: Flags.getDataStore().readDouble("Price." + type.toString() + "." + name);
	}

	/**
	 * Checks if the flag is a player flag. (Supports messaging and trust)
	 * 
	 * @return True if this flag is a player flag.
	 */
	public boolean isPlayerFlag() {
		return player;
	}

	/**
	 * Sets the price of the flag or message
	 * 
	 * @param type
	 *            The PurchaseType to set for this flag
	 * @param price
	 *            The new price of the purchase.
	 */
	public void setPrice(EPurchaseType type, double price) {
		Flags.getDataStore().write("Price." + type.toString() + "." + name,	price);
	}

	/**
	 * Gets a string representation of the flag meta.
	 * 
	 * @return The flag as a string
	 */
	@Override
	public String toString() {
		return "N=" + name + ",V=" + def + ",P=" + player + ",G=" + plugin
				+ ",D=" + description + ",A=" + area + ",W=" + world;
	}
}