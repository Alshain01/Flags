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

package io.github.alshain01.flags.api;

import io.github.alshain01.flags.*;
import io.github.alshain01.flags.api.area.Area;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.UUID;

/**
 * Class for acquiring data from the active cuboid system
 */
public enum CuboidPlugin {
    DEFAULT("AreaDefault", false) {
        Area getCuboidAt(Location location) { return new AreaDefault(location); }
        Area getArea(String name) { return new AreaDefault(name); }
        boolean hasArea(Location location) { return true; }
    },

    WILDERNESS("AreaWilderness", false) {
        Area getCuboidAt(Location location) { return new AreaWilderness(location); }
        Area getArea(String name) { return new AreaWilderness(name); }
        boolean hasArea(Location location) { return true; }
    },

    FLAGS("Flags", true) {
        Area getCuboidAt(Location location) { return new AreaFlags(location); }
        Area getArea(String name) { return new AreaFlags(UUID.fromString(name)); }
        boolean hasArea(Location location) { return AreaFlags.hasSector(location); }
    },

    GRIEF_PREVENTION("Grief Prevention", true) {
        Area getCuboidAt(Location location) {
            if(!hasArea(location)) { return new AreaWilderness(location); }

            final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("GriefPrevention");
            final float pluginVersion = Float.valueOf(plugin.getDescription().getVersion().substring(0, 3));

            return pluginVersion >= (float)7.8
                    ? new AreaGriefPrevention78(location)
                    : new AreaGriefPrevention(location);
        }

        Area getArea(String name) {
            final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("GriefPrevention");
            final float pluginVersion = Float.valueOf(plugin.getDescription().getVersion().substring(0, 3));

            return pluginVersion >= (float)7.8
                    ? new AreaGriefPrevention78(Long.parseLong(name))
                    : new AreaGriefPrevention(Long.parseLong(name));
        }

        boolean hasArea(Location location) { return AreaGriefPrevention.hasClaim(location); }
    },

    WORLDGUARD("WorldGuard", false) {
        Area getCuboidAt(Location location) {
            if(!hasArea(location)) { return new AreaWilderness(location); }
            return new AreaWorldGuard(location);
        }

        Area getArea(String name) {
            String path[] = name.split("\\.");
            return new AreaWorldGuard(Bukkit.getWorld(path[0]), path[1]);
        }

        boolean hasArea(Location location) { return AreaWorldGuard.hasRegion(location); }
    },

    RESIDENCE("Residence", true) {
        Area getCuboidAt(Location location) {
            if(!hasArea(location)) { return new AreaWilderness(location); }
            return new AreaResidence(location);
        }
        Area getArea(String name) { return new AreaResidence(name); }
        boolean hasArea(Location location) { return AreaResidence.hasResidence(location); }
    },

    INFINITEPLOTS("InfinitePlots", false) {
        Area getCuboidAt(Location location) {
            if(!hasArea(location)) { return new AreaWilderness(location); }
            return new AreaInfinitePlots(location);
        }

        Area getArea(String name) {
            String path[] = name.split("\\.");
            final String[] coordinates = path[1].split(";");
            return new AreaInfinitePlots(Bukkit.getWorld(path[0]), Integer.valueOf(coordinates[0]), Integer.valueOf(coordinates[1]));
        }

        boolean hasArea(Location location) { return AreaInfinitePlots.hasPlot(location); }
    },

    FACTIONS("Factions", false) {
        Area getCuboidAt(Location location) {
            if(!hasArea(location)) { return new AreaWilderness(location); }
            return new AreaFactions(location);
        }

        Area getArea(String name) {
            String path[] = name.split("\\.");
            return new AreaFactions(Bukkit.getWorld(path[0]), path[1]);
        }

        boolean hasArea(Location location) { return AreaFactions.hasTerritory(location); }
    },

    FACTOID("Factoid", true) {
        Area getCuboidAt(Location location) {
            if(!hasArea(location)) { return new AreaWilderness(location); }
            return new AreaFactoid(location);
        }

        public Area getArea(String id) {
            return new AreaFactoid(UUID.fromString(id));
        }

        public boolean hasArea(Location location) { return AreaFactoid.hasLand(location); }
    },

    PLOTME("PlotMe",false) {
        Area getCuboidAt(Location location) {
            if(!hasArea(location)) { return new AreaWilderness(location); }
            return new AreaPlotMe(location);
        }

        Area getArea(String name) {
            String path[] = name.split("\\.");
            return new AreaPlotMe(Bukkit.getWorld(path[0]), path[1]);
        }

        boolean hasArea(Location location) { return AreaPlotMe.hasPlot(location); }
    },

    REGIOS("Regios", false) {
        Area getCuboidAt(Location location) {
            if(!hasArea(location)) { return new AreaWilderness(location); }
            return new AreaRegios(location);
        }
        Area getArea(String name) { return new AreaRegios(name); }
        boolean hasArea(Location location) { return AreaRegios.hasRegion(location); }
    },

    PRECIOUSSTONES("PreciousStones", true){
        Area getCuboidAt(Location location) {
            if(!hasArea(location)) { return new AreaWilderness(location); }
            return new AreaPreciousStones(location);
        }

        Area getArea(String name) {
            String path[] = name.split("\\.");
            return new AreaPreciousStones(Bukkit.getWorld(path[0]), Long.valueOf(path[1]));
        }

        boolean hasArea(Location location) { return AreaPreciousStones.hasField(location); }
    };

    private String cuboidName;
    private String displayName = null;
    private final boolean subdivisions;

    private CuboidPlugin(String displayName, boolean hasSubivisions) {
        this.displayName = displayName;
        this.subdivisions = hasSubivisions;
    }

    /*
     * Gets an area from the data store at a specific location.
     *
     * @param location
     *            The location to request an area.
     * @return An Area from the configured cuboid system which may fail isArea()
     */
    abstract Area getCuboidAt(Location location);

    /*
     * Gets an area by cuboid system specific name. The name is formatted based on the
     * cuboid system.
     *
     * GriefPrevention = ID number
     * WorldGuard = WorldName.RegionName
     * Regios = Region name
     * Residence = Residence name OR ResidenceName.Sub-zoneName
     * PreciousStones = WorldName.ID
     * InfinitePlots = WorldName.PlotLoc (X;Z)
     * Factoid = Land UUID
     * Factions = WorldName.FactionID
     * PlotMe = WorldName.PlotID
     * Flags = Sector UUID
     *
     * @param name
     *            The cuboid system specific name of the area or world name
     * @return The Area requested, may be null in cases of invalid cuboid system
     *         selection.
     */
    abstract Area getArea(String name);

    /*
     * Gets whether there is an area for this cuboid system that Flags can use at the location
     *
     * @param location The location to check for an area
     * @return True if there is an area
     */
    abstract boolean hasArea(Location location);

    /**
     * Gets the plug-in name as indicated in it's plugin.yml
     *
     * @return The case sensitive plugin.yml name for the enumerated value
     */
    public String getName() {
        return displayName.replaceAll("\\s","");
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
     * Gets the name of the area division for the cuboid system
     * (i.e. Claim, Residence, Territory, Region, etc.)
     *
     * @return The name of the area division
     */
    public String getCuboidName() {
        return cuboidName;
    }

    /**
     * Gets the enumeration that matches the case sensitive plugin.yml name.
     *
     * @return The enumeration. CuboidPlugin.FLAGS if no matches found.
     */
    public static CuboidPlugin getByName(String name) {
        for (final CuboidPlugin p : CuboidPlugin.values()) {
            if (name.equalsIgnoreCase(p.getName())) {
                return p;
            }
        }
        return CuboidPlugin.FLAGS;
    }

    /**
     * Gets if the cuboid plugin supports subdivisions
     * @return true if the cuboid plugin supports subdivisions.
     */
    public boolean hasSubdivisions() {
        return subdivisions;
    }

    /**
     * Instruct the enum to reload the cuboid names from the yaml file.
     */
    public static void loadNames() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Flags");
        File messageFile = new File(plugin.getDataFolder(), "message.yml");
        if (!messageFile.exists()) {
            plugin.saveResource("message.yml", false);
        }

        YamlConfiguration defaults = YamlConfiguration.loadConfiguration(plugin.getResource("message.yml"));
        YamlConfiguration messages = YamlConfiguration.loadConfiguration(messageFile);
        messages.setDefaults(defaults);

        for (CuboidPlugin t : CuboidPlugin.values()) {
            try {
                t.cuboidName = ChatColor.translateAlternateColorCodes('&', messages.getString(t.getName()));
            } catch (NullPointerException ex) {
                Logger.error("Failed to load message " + t.getName());
            }
        }
    }
}
