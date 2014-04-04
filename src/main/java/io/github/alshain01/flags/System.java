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

import io.github.alshain01.flags.area.*;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Location;
import org.bukkit.entity.Player;

@Deprecated
/**
 * Class for acquiring data from the active cuboid system
 * @deprecated Use CuboidType instead
 */
public enum System {
    @Deprecated
	DEFAULT("Default", "Default", false),
    @Deprecated
	WORLD("Wilderness", "Wilderness", false),
    @Deprecated
    FLAGS("Flags", "Flags", true),
    @Deprecated
	GRIEF_PREVENTION("GriefPrevention",	"Grief Prevention", true),
    @Deprecated
	WORLDGUARD("WorldGuard", "WorldGuard", false),
    @Deprecated
	RESIDENCE("Residence", "Residence", true),
    @Deprecated
	INFINITEPLOTS("InfinitePlots", "InfinitePlots", false),
    @Deprecated
	FACTIONS("Factions", "Factions", false),
    @Deprecated
    FACTOID("Factoid", "Factoid", true),
    @Deprecated
	PLOTME("PlotMe", "PlotMe",false),
    @Deprecated
	REGIOS("Regios", "Regios", false),
    @Deprecated
	PRECIOUSSTONES("PreciousStones", "PreciousStones", true);

    /*
     * Gets an area from the data store at a specific location.
     *
     * @param location
     *            The location to request an area.
     * @return An Area from the configured system which may fail isArea()
     * @deprecated Use CuboidType.getCuboidAt() instead
     */
    @SuppressWarnings("unused, deprecation")
    @Deprecated
    Area getSystemAreaAt(Location location) {
        return CuboidType.getByName(this.toString()).getCuboidAt(location);
    }

    /*
     * Gets an area by system specific name. The name is formatted based on the
     * system.
     *
     * GriefPrevention = ID number OR ID.SubID
     * WorldGuard = WorldName.RegionName
     * Regios = Region name
     * Residence = Residence name OR ResidenceName.Sub-zoneName
     * PreciousStones = WorldName.ID
     * InfinitePlots = WorldName.PlotLoc (X;Z)
     * Factions = WorldName.FactionID
     * PlotMe = WorldName.PlotID
     * Flags = Sector UUID
     *
     * @param name
     *            The system specific name of the area or world name
     * @return The Area requested, may be null in cases of invalid system
     *         selection.
     * @deprecated Use CuboidType.getByName() instead
     */
    @SuppressWarnings("unused, deprecation") // API
    @Deprecated
    public Area getArea(String name) {
        return CuboidType.getByName(this.toString()).getArea(name);
    }

    /*
     * Gets whether there is an area for this system that Flags can use at the location
     *
     * @param location The location to check for an area
     * @return True if there is an area
     * @deprecated Use CuboidType.hasArea() instead
     */
    @SuppressWarnings("unused, deprecation") // API
    @Deprecated
    public boolean hasArea(Location location) {
        return CuboidType.getByName(this.toString()).hasArea(location);
    }

    /*
     * Gets an area from the data store at a specific location.
     *
     * @param location
     *            The location to request an area.
     * @return An Area from the configured system or the world if no area is
     *         defined.
     * @deprecated Use CuboidType.getAreaAt() instead
     */
    @Deprecated
    @SuppressWarnings("unused, deprecation")
    public Area getAreaAt(Location location) {
        Area area = getSystemAreaAt(location);
        return area.isArea() ? area : CuboidType.WILDERNESS.getCuboidAt(location);
    }

	/**
	 * Gets the enumeration that matches the case sensitive plugin.yml name.
	 * 
	 * @return The enumeration. LandSystem.FLAGS if no matches found.
     * @deprecated Use CuboidType.getByName() instead
	 */
    @SuppressWarnings("unused, deprecation")
    @Deprecated
	public static System getByName(String name) {
		for (final System p : System.values()) {
			if (name.equals(p.pluginName)) {
				return p;
			}
		}
		return System.FLAGS;
	}
	
	/**
	 * Gets the area type enumeration of the land system that Flags is currently using.
	 * 
	 * @return The enumeration.
     * @deprecated Use CuboidType.getActive() instead
	 */
    @SuppressWarnings("unused, deprecation")
    @Deprecated
	public static System getActive() {
		return getByName(CuboidType.getActive().toString());
	}

	private String pluginName = null, displayName = null;
    private final boolean subdivisions;
    //private Logger logger;

	private System(String name, String displayName, boolean hasSubivisions) {
		pluginName = name;
		this.displayName = displayName;
        this.subdivisions = hasSubivisions;
	}
	
	/**
	 * Gets a user friendly string, including spaces, for the plug-in.
	 * 
	 * @return The user friendly name of the plugin
     * @deprecated Use CuboidType.getDisplayName() instead
	 */
    @SuppressWarnings("unused, deprecation")
    @Deprecated
	public String getDisplayName() {
		return displayName;
	}

    /**
     * Gets the name of the area division for the system (i.e. Claim, Residence, Territory, Region, etc.)
     *
     * @return The name of the area division
     * @deprecated Use CuboidType.getCuboidDescriptor() instead
     */
    @SuppressWarnings("unused, deprecation")
    @Deprecated
	public String getAreaType() {
        return CuboidType.getByName(this.toString()).getCuboidName();
	}

	/**
	 * Gets the plug-in name as indicated in it's plugin.yml
	 * 
	 * @return The case sensitive plugin.yml name for the enumerated value
     * @deprecated Use CuboidType.toString() instead
	 */
    @SuppressWarnings("unused, deprecation")
    @Deprecated
	@Override
	public String toString() {
		return pluginName;
	}
	
	/**
	 * Checks if a player is in Pvp combat that is being monitored by the system
	 * 
	 * @param player
	 *            The player to request information for
	 * @return True if the player is in pvp combat, false is not or if system is
	 *         unsupported.
     * @deprecated Use CuboidType.inPvpCombat() instead
	 */
    @SuppressWarnings("unused, deprecation")
    @Deprecated
	public boolean inPvpCombat(Player player) {
		return this == System.GRIEF_PREVENTION
                && GriefPrevention.instance.dataStore.getPlayerData(player.getName()).inPvpCombat();
	}

    @SuppressWarnings("unused, deprecation")
    @Deprecated
    public boolean hasSubdivisions() {
        return subdivisions;
    }
}