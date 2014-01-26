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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public enum System {
	DEFAULT("Default", "Default", false) {
        public Area getAreaAt(Location location) { return new Default(location); }
        public Area getArea(String name) { return new Default(name); }
        public boolean hasArea(Location location) { return true; }
    },

	WORLD("World", "World", false) {
        public Area getAreaAt(Location location) { return new World(location); }
        public Area getArea(String name) { return new World(name); }
        public boolean hasArea(Location location) { return true; }
    },

	GRIEF_PREVENTION("GriefPrevention",	"Grief Prevention", true) {
        public Area getAreaAt(Location location) {
            if(!hasArea(location)) { return new World(location); }

            final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("GriefPrevention");
            final float pluginVersion = Float.valueOf(plugin.getDescription().getVersion().substring(0, 3));

            return pluginVersion >= (float)7.8
                    ? new GriefPreventionClaim78(location)
                    : new GriefPreventionClaim(location);
        }

        public Area getArea(String name) {
            final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("GriefPrevention");
            final float pluginVersion = Float.valueOf(plugin.getDescription().getVersion().substring(0, 3));
            String[] id = name.split("\\.");

            if (pluginVersion >= (float)7.8) {
                if(id.length > 1) {
                    return new GriefPreventionClaim78(Long.parseLong(id[0]), Long.parseLong(id[1]));
                } else {
                    return new GriefPreventionClaim78(Long.parseLong(id[0]));
                }
            }
            return new GriefPreventionClaim(Long.parseLong(id[0]));
        }

        public boolean hasArea(Location location) { return GriefPreventionClaim.hasClaim(location); }
    } ,

	WORLDGUARD("WorldGuard", "WorldGuard", false) {
        public Area getAreaAt(Location location) {
            if(!hasArea(location)) { return new World(location); }
            return new WorldGuardRegion(location);
        }

        public Area getArea(String name) {
            String path[] = name.split("\\.");
            return new WorldGuardRegion(Bukkit.getWorld(path[0]), path[1]);
        }

        public boolean hasArea(Location location) { return WorldGuardRegion.hasRegion(location); }
    },

	RESIDENCE("Residence", "Residence", true) {
        public Area getAreaAt(Location location) {
            if(!hasArea(location)) { return new World(location); }
            return new ResidenceClaimedResidence(location);
        }
        public Area getArea(String name) { return new ResidenceClaimedResidence(name); }
        public boolean hasArea(Location location) { return ResidenceClaimedResidence.hasResidence(location); }
    },

	INFINITEPLOTS("InfinitePlots", "InfinitePlots", false) {
        public Area getAreaAt(Location location) {
            if(!hasArea(location)) { return new World(location); }
            return new InfinitePlotsPlot(location);
        }

        public Area getArea(String name) {
            String path[] = name.split("\\.");
            final String[] coordinates = path[1].split(";");
            return new InfinitePlotsPlot(Bukkit.getWorld(path[0]), Integer.valueOf(coordinates[0]), Integer.valueOf(coordinates[1]));
        }

        public boolean hasArea(Location location) { return InfinitePlotsPlot.hasPlot(location); }
    },

	FACTIONS("Factions", "Factions", false) {
        public Area getAreaAt(Location location) {
            if(!hasArea(location)) { return new World(location); }
            return new FactionsTerritory(location);
        }

        public Area getArea(String name) {
            String path[] = name.split("\\.");
            return new FactionsTerritory(Bukkit.getWorld(path[0]), path[1]);
        }

        public boolean hasArea(Location location) { return FactionsTerritory.hasTerritory(location); }
    },

	PLOTME("PlotMe", "PlotMe",false) {
        public Area getAreaAt(Location location) {
            if(!hasArea(location)) { return new World(location); }
            return new PlotMePlot(location);
        }

        public Area getArea(String name) {
            String path[] = name.split("\\.");
            return new PlotMePlot(Bukkit.getWorld(path[0]), path[1]);
        }

        public boolean hasArea(Location location) { return PlotMePlot.hasPlot(location); }
    },

	REGIOS("Regios", "Regios", false) {
        public Area getAreaAt(Location location) {
            if(!hasArea(location)) { return new World(location); }
            return new RegiosRegion(location);
        }
        public Area getArea(String name) { return new RegiosRegion(name); }
        public boolean hasArea(Location location) { return RegiosRegion.hasRegion(location); }
    },

	PRECIOUSSTONES("PreciousStones", "PreciousStones", true){
        public Area getAreaAt(Location location) {
            if(!hasArea(location)) { return new World(location); }
            return new PreciousStonesField(location);
        }

        public Area getArea(String name) {
            String path[] = name.split("\\.");
            return new PreciousStonesField(Bukkit.getWorld(path[0]), Long.valueOf(path[1]));
        }

        public boolean hasArea(Location location) { return PreciousStonesField.hasField(location); }
    };

    /*
     * Gets an area from the data store at a specific location.
     *
     * @param location
     *            The location to request an area.
     * @return An Area from the configured system or the world if no area is
     *         defined.
     */
    public abstract Area getAreaAt(Location location);

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
     *
     * @param name
     *            The system specific name of the area or world name
     * @return The Area requested, may be null in cases of invalid system
     *         selection.
     */
    public abstract Area getArea(String name);

    /*
     * Gets whether there is an area for this system that Flags can use at the location
     *
     * @param location The location to check for an area
     * @return True if there is an area
     */
    public abstract boolean hasArea(Location location);

	/**
	 * Gets the enumeration that matches the case sensitive plugin.yml name.
	 * 
	 * @return The enumeration. LandSystem.NONE if no matches found.
	 */
	public static System getByName(String name) {
		for (final System p : System.values()) {
			if (name.equals(p.pluginName)) {
				return p;
			}
		}
		return System.WORLD;
	}
	
	/**
	 * Gets the area type enumeration of the land system that Flags is currently using.
	 * 
	 * @return The enumeration.
	 */
	public static System getActive() {
		return Flags.currentSystem;
	}

	private String pluginName = null, displayName = null;
    private boolean subdivisions;

	private System(String name, String displayName, boolean hasSubivisions) {
		pluginName = name;
		this.displayName = displayName;
        this.subdivisions = hasSubivisions;
	}
	
	/**
	 * Gets a user friendly string, including spaces, for the plug-in.
	 * 
	 * @return The user friendly name of the plugin
	 */
	public String getDisplayName() {
		return displayName;
	}

    /**
     * Gets the name of the area division for the system (i.e. Claim, Residence, Territory, Region, etc.)
     *
     * @return The name of the area division
     */
	public String getAreaType() {
		final String message = Flags.messageStore.getConfig().getString("Message." + toString());
		if (message == null) {
			Flags.warn("ERROR: Invalid message.yml Message for " + toString());
			return "ERROR: Invalid message.yml Message. Please contact your server administrator.";
		}
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	/**
	 * Gets the plug-in name as indicated in it's plugin.yml
	 * 
	 * @return The case sensitive plugin.yml name for the enumerated value
	 */
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
	 */
	public boolean inPvpCombat(Player player) {
		return this == System.GRIEF_PREVENTION
                && GriefPrevention.instance.dataStore.getPlayerData(player.getName()).inPvpCombat();
	}

    public boolean hasSubdivisions() {
        return subdivisions;
    }
}