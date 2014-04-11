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

import io.github.alshain01.flags.api.CuboidPlugin;
import io.github.alshain01.flags.api.area.Area;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Class for acquiring areas from a cuboid system.
 * Flag extension plugins should consider using FlagsAPI instead of this class.
 */
@SuppressWarnings("unused") // They are used, just through valueOf()
public enum AreaFactory {
    DEFAULT{
        Class<? extends Area> getAreaClass() { return AreaDefault.class; }
        Area getCuboidAt(Location location) { return new AreaDefault(location); }
        Area getCuboidByName(String name) { return new AreaDefault(name); }
        boolean hasCuboid(Location location) { return true; }
        void registerCleaner(Plugin plugin) { }
    },

    WILDERNESS {
        Class<? extends Area> getAreaClass() { return AreaWilderness.class; }
        Area getCuboidAt(Location location) { return new AreaWilderness(location); }
        Area getCuboidByName(String name) { return new AreaWilderness(name); }
        boolean hasCuboid(Location location) { return true; }
        void registerCleaner(Plugin plugin) { }
    },

    FLAGS {
        Class<? extends Area> getAreaClass() { return AreaFlags.class; }
        Area getCuboidAt(Location location) { return new AreaFlags(location); }
        Area getCuboidByName(String name) { return new AreaFlags(UUID.fromString(name)); }
        boolean hasCuboid(Location location) { return AreaFlags.hasSector(location); }
        void registerCleaner(Plugin plugin) { Bukkit.getPluginManager().registerEvents(new AreaFlags.Cleaner(), plugin);}
    },

    GRIEF_PREVENTION {
        Class<? extends Area> getAreaClass() { return AreaGriefPrevention.class; }
        Area getCuboidAt(Location location) {
            if(!hasCuboid(location)) { return new AreaWilderness(location); }
            return new AreaGriefPrevention(location);
        }

        Area getCuboidByName(String name) { return new AreaGriefPrevention(Long.parseLong(name)); }
        boolean hasCuboid(Location location) { return AreaGriefPrevention.hasClaim(location); }
        void registerCleaner(Plugin plugin) { Bukkit.getPluginManager().registerEvents(new AreaGriefPrevention.Cleaner(), plugin);}
    },

    WORLDGUARD {
        Class<? extends Area> getAreaClass() { return AreaWorldGuard.class; }
        Area getCuboidAt(Location location) {
            if(!hasCuboid(location)) { return new AreaWilderness(location); }
            return new AreaWorldGuard(location);
        }

        Area getCuboidByName(String name) {
            String path[] = name.split("\\.");
            return new AreaWorldGuard(Bukkit.getWorld(path[0]), path[1]);
        }

        boolean hasCuboid(Location location) { return AreaWorldGuard.hasRegion(location); }

        void registerCleaner(Plugin plugin) { }
    },

    RESIDENCE {
        Class<? extends Area> getAreaClass() { return AreaResidence.class; }
        Area getCuboidAt(Location location) {
            if(!hasCuboid(location)) { return new AreaWilderness(location); }
            return new AreaResidence(location);
        }

        Area getCuboidByName(String id) { return new AreaResidence(id); }
        boolean hasCuboid(Location location) { return AreaResidence.hasResidence(location); }
        void registerCleaner(Plugin plugin) { Bukkit.getPluginManager().registerEvents(new AreaResidence.Cleaner(), plugin);}
    },

    INFINITEPLOTS {
        Class<? extends Area> getAreaClass() { return AreaInfinitePlots.class; }
        Area getCuboidAt(Location location) {
            if(!hasCuboid(location)) { return new AreaWilderness(location); }
            return new AreaInfinitePlots(location);
        }

        Area getCuboidByName(String name) {
            String path[] = name.split("\\.");
            final String[] coordinates = path[1].split(";");
            return new AreaInfinitePlots(Bukkit.getWorld(path[0]), Integer.valueOf(coordinates[0]), Integer.valueOf(coordinates[1]));
        }

        boolean hasCuboid(Location location) { return AreaInfinitePlots.hasPlot(location); }

        void registerCleaner(Plugin plugin) { }
    },

    FACTIONS {
        Class<? extends Area> getAreaClass() { return AreaFactions.class; }
        Area getCuboidAt(Location location) {
            if(!hasCuboid(location)) { return new AreaWilderness(location); }
            return new AreaFactions(location);
        }

        Area getCuboidByName(String name) {
            String path[] = name.split("\\.");
            return new AreaFactions(Bukkit.getWorld(path[0]), path[1]);
        }

        boolean hasCuboid(Location location) { return AreaFactions.hasTerritory(location); }

        void registerCleaner(Plugin plugin) { }
    },

    FACTOID {
        Class<? extends Area> getAreaClass() { return AreaFactoid.class; }
        Area getCuboidAt(Location location) {
            if(!hasCuboid(location)) { return new AreaWilderness(location); }
            return new AreaFactoid(location);
        }

        public Area getCuboidByName(String id) {
            return new AreaFactoid(UUID.fromString(id));
        }

        public boolean hasCuboid(Location location) { return AreaFactoid.hasLand(location); }
        void registerCleaner(Plugin plugin) { Bukkit.getPluginManager().registerEvents(new AreaFactoid.Cleaner(), plugin);}
    },

    PLOTME {
        Class<? extends Area> getAreaClass() { return AreaPlotMe.class; }
        Area getCuboidAt(Location location) {
            if(!hasCuboid(location)) { return new AreaWilderness(location); }
            return new AreaPlotMe(location);
        }

        Area getCuboidByName(String name) {
            String path[] = name.split("\\.");
            return new AreaPlotMe(Bukkit.getWorld(path[0]), path[1]);
        }

        boolean hasCuboid(Location location) { return AreaPlotMe.hasPlot(location); }

        void registerCleaner(Plugin plugin) { }
    },

    REGIOS {
        Class<? extends Area> getAreaClass() { return AreaRegios.class; }
        Area getCuboidAt(Location location) {
            if(!hasCuboid(location)) { return new AreaWilderness(location); }
            return new AreaRegios(location);
        }
        Area getCuboidByName(String name) { return new AreaRegios(name); }
        boolean hasCuboid(Location location) { return AreaRegios.hasRegion(location); }
        void registerListener(JavaPlugin plugin) { Bukkit.getPluginManager().registerEvents(new AreaRegios.Cleaner(), plugin);}

        void registerCleaner(Plugin plugin) { }
    },

    PRECIOUSSTONES {
        Class<? extends Area> getAreaClass() { return AreaPreciousStones.class; }
        Area getCuboidAt(Location location) {
            if(!hasCuboid(location)) { return new AreaWilderness(location); }
            return new AreaPreciousStones(location);
        }

        Area getCuboidByName(String name) {
            String path[] = name.split("\\.");
            return new AreaPreciousStones(Bukkit.getWorld(path[0]), Long.valueOf(path[1]));
        }

        boolean hasCuboid(Location location) { return AreaPreciousStones.hasField(location); }

        void registerCleaner(Plugin plugin) { }
    };

    abstract Class<? extends Area> getAreaClass();
    abstract Area getCuboidAt(Location location);
    abstract Area getCuboidByName(String name);
    abstract boolean hasCuboid(Location location);
    abstract void registerCleaner(Plugin plugin);

    /*
     * Gets an area from the data store at a specific location.
     *
     * @param location
     *            The location to request an area.
     * @return An Area from the configured cuboid system which may fail isArea()
     */
    public static Area getAreaAt(CuboidPlugin type, Location location){
        return valueOf(type.toString()).getCuboidAt(location);
    }

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
    public static Area getArea(CuboidPlugin type, String name) {
        return valueOf(type.toString()).getCuboidByName(name);
    }

    /*
     * Gets whether there is an area for this cuboid system that Flags can use at the location
     *
     * @param location The location to check for an area
     * @return True if there is an area
     */
    public static boolean hasArea(CuboidPlugin type, Location location) {
        return valueOf(type.toString()).hasCuboid(location);
    }

    public static Area getWildernessArea(World world) {
        return new AreaWilderness(world);
    }

    public static Area getDefaultArea(World world) {
        return new AreaDefault(world);
    }

    public static Class<? extends Area> getAreaClass(CuboidPlugin type) {
        return valueOf(type.toString()).getAreaClass();
    }

    static void registerCleaner(CuboidPlugin type, Plugin plugin) {
        valueOf(type.toString()).registerCleaner(plugin);
    }
}

