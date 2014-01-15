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

package io.github.alshain01.Flags;

import io.github.alshain01.Flags.area.Area;
import io.github.alshain01.Flags.area.World;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Class for retrieving area system specific information.
 * 
 * @author Alshain01
 */
@SuppressWarnings("ALL")
public final class Director {
	/*
	 * Gets the area at a specific location if one exists, otherwise null
	 */
	@Deprecated
	private static Area getArea(Location location) {
		return System.getActive().getAreaAt(location);
	}

	/**
	 * Gets an area by system specific name. The name is formatted based on the
	 * system.
	 * 
	 * GriefPrevention = ID number
	 * WorldGuard = WorldName.RegionName
	 * Residence = Residence name OR ResidenceName.Sub-zoneName
	 * InfinitePlots = WorldName.PlotLoc (X;Z)
	 * Factions = WorldName.FactionID
	 * PlotMe = WorldName.PlotID
	 * 
	 * @deprecated Use System.getActive().getArea(String)
	 * @param name
	 *            The system specific name of the area or world name
	 * @return The Area requested, may be null in cases of invalid system
	 *         selection.
	 */
	@Deprecated
	public static Area getArea(String name) {
		return System.getActive().getArea(name);
	}

	/**
	 * Gets an area from the data store at a specific location.
	 * @deprecated Uses System.getActive().getAreaAt(Location)
	 * @param location
	 *            The location to request an area.
	 * @return An Area from the configured system or the world if no area is
	 *         defined.
	 */
	@Deprecated
	public static Area getAreaAt(Location location) {
		// hasArea() and area.isArea() may not necessarily be the same for all
		// systems,
		// however hasArea() is faster than constructing an area object, and
		// calling both has minimal impact.
		final Area area = System.getActive().getAreaAt(location);
		return area.isArea() ? area : new World(location);
	}

/*	*//**
	 * Gets a set of system specific area names stored in the database
	 * 
	 * @return A list containing all the area names.
	 *//*
	public static Set<String> getAreaNames() {
		Set<String> worlds, localAreas;
		final Set<String> allAreas = new HashSet<String>();
		switch (LandSystem.getActive()) {
		case GRIEF_PREVENTION:
			return Flags.getDataStore().readKeys("GriefPreventionData");
		case RESIDENCE:
			return Flags.getDataStore().readKeys("ResidenceData");
		case WORLDGUARD:
			worlds = Flags.getDataStore().readKeys("WorldGuardData");
			for (final String world : worlds) {
				localAreas = Flags.getDataStore().readKeys("WorldGuardData." + world);
				for (final String area : localAreas) {
					allAreas.add(world + "." + area);
				}
			}
			return allAreas;
		case INFINITEPLOTS:
			worlds = Flags.getDataStore().readKeys("InfinitePlotsData");
			for (final String world : worlds) {
				localAreas = Flags.getDataStore().readKeys("InfinitePlotsData." + world);
				for (final String localArea : localAreas) {
					allAreas.add(world + "." + localArea);
				}
			}
			return allAreas;
		case FACTIONS:
			worlds = Flags.getDataStore().readKeys("FactionsData");
			for (final String world : worlds) {
				localAreas = Flags.getDataStore().readKeys("FactionsData." + world);
				for (final String localArea : localAreas) {
					if (!allAreas.contains(localArea)) {
						allAreas.add(world + "." + localArea);
					}
				}
			}
			return allAreas;
		case PLOTME:
			worlds = Flags.getDataStore().readKeys("PlotMeData");
			for (final String world : worlds) {
				localAreas = Flags.getDataStore().readKeys("PlotMeData." + world);
				for (final String localArea : localAreas) {
					allAreas.add(world + "." + localArea);
				}
			}
			return allAreas;
		default:
			return null;
		}
	}*/

	/**
	 * Gets a user friendly name of the area type of the configured system,
	 * capitalized. For use when the name is required even though an area does
	 * not exist (such as error messages). If you have an area instance, use
	 * Area.getAreaType() instead.
	 * 
	 * @return The user friendly name.
	 * @deprecated Use System.getActive().getAreaType()
	 */
	@Deprecated
	public static String getSystemAreaType() {
		return System.getActive().getAreaType();
	}

	/**
	 * Checks if a player is in Pvp combat that is being monitored by the system
	 * 
	 * @deprecated Use System.getActive().inPvPCombat(Player)
	 * @param player
	 *            The player to request information for
	 * @return True if the player is in pvp combat, false is not or if system is
	 *         unsupported.
	 */
	@Deprecated
	public static boolean inPvpCombat(Player player) {
		return System.getActive() == System.GRIEF_PREVENTION
                && GriefPrevention.instance.dataStore.getPlayerData(player.getName()).inPvpCombat();
	}

	private Director() {
	}
}