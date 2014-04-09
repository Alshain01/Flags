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
@SuppressWarnings("unused")
public enum CuboidPlugin {
    DEFAULT("Default", false, true),
    WILDERNESS("Wilderness", false, true),
    FLAGS("Flags", true, true),
    GRIEF_PREVENTION("Grief Prevention", true, false),
    WORLDGUARD("WorldGuard", false, false),
    RESIDENCE("Residence", true, false),
    INFINITEPLOTS("InfinitePlots", false, false),
    FACTIONS("Factions", false, false),
    FACTOID("Factoid", true, true),
    PLOTME("PlotMe",false, false),
    REGIOS("Regios", false, false),
    PRECIOUSSTONES("PreciousStones", true, false);

    private String cuboidName;
    private String displayName = null;
    private final boolean subdivisions;
    private final boolean uniqueIdSupport;

    private CuboidPlugin(String displayName, boolean hasSubivisions, boolean uniqueIdSupport) {
        this.displayName = displayName;
        this.subdivisions = hasSubivisions;
        this.uniqueIdSupport = uniqueIdSupport;
    }

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
     * Gets if the cuboid plugin identifies areas by UUID
     * @return true if the cuboid plugin identifies areas by UUID.
     */
    public boolean hasUniqueId() {
        return uniqueIdSupport;
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
                System.out.print("Failed to load message " + t.getName());
            }
        }
    }
}
