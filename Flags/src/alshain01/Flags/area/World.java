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

package alshain01.Flags.area;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.permissions.Permissible;

import alshain01.Flags.Flag;
import alshain01.Flags.Flags;
import alshain01.Flags.LandSystem;
import alshain01.Flags.Message;

public class World extends Area {
	private final static String dataHeader = "World.";
	private final static HashSet<String> owners = new HashSet<String>(Arrays.asList("world"));
	private final org.bukkit.World world;

	/**
	 * Creates an instance of World based on a Bukkit Location
	 * 
	 * @param location
	 *            The Bukkit location
	 */
	public World(Location location) {
		this(location.getWorld());
	}

	/**
	 * Creates an instance of World based on a Bukkit World
	 * 
	 * @param world
	 *            The Bukkit world
	 */
	public World(org.bukkit.World world) {
		this.world = world;
	}

	/**
	 * 0 if the the worlds are the same, 3 if they are not.
	 * 
	 * @return The value of the comparison.
	 */
	@Override
	public int compareTo(Area a) {
		return a instanceof World && a.getSystemID().equals(world.getName()) ? 0 : 3;
	}

	@Override
	public String getAreaType() {
		return Message.World.get();
	}

	@Override
	protected String getDataPath() {
		return dataHeader + world.getName();
	}

	@Override
	public String getMessage(Flag flag, boolean parse) {
		String message = Flags.getDataStore().readMessage(this, flag);

		if (message == null) {
			message = flag.getDefaultWorldMessage();
		}

		if (parse) {
			message = message
					.replaceAll("\\{AreaType\\}", Message.World.get().toLowerCase())
					.replaceAll("\\{World\\}", world.getName());
			message = ChatColor.translateAlternateColorCodes('&', message);
		}
		return message;
	}

	@Override
	public Set<String> getOwners() {
		return owners;
	}

	@Override
	public String getSystemID() {
		return world.getName();
	}

	@Override
	public Set<String> getTrustList(Flag flag) {
		final Set<String> trustedPlayers = Flags.getDataStore().readTrust(this, flag);
		return trustedPlayers != null ? trustedPlayers : new HashSet<String>();
	}

	@Override
	public Boolean getValue(Flag flag, boolean absolute) {
		final Boolean value = super.getValue(flag, true);
		if (absolute) {
			return value;
		}
		return value != null ? value : flag.getDefault();
	}

	@Override
	public org.bukkit.World getWorld() {
		return world;
	}

	@Override
	public boolean hasBundlePermission(Permissible p) {
		return p.hasPermission("flags.area.bundle.world");
	}

	@Override
	public boolean hasPermission(Permissible p) {
		return p.hasPermission("flags.area.flag.world");
	}

	@Override
	public boolean isArea() {
		return world != null;
	}

	@Override
	public LandSystem getSystem() {
		return LandSystem.WORLD;
	}
}
