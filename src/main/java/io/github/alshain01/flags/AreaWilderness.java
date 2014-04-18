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


import io.github.alshain01.flags.api.AreaPlugin;
import io.github.alshain01.flags.api.Flag;

import java.util.UUID;

import io.github.alshain01.flags.api.area.Identifiable;
import io.github.alshain01.flags.api.area.Nameable;
import io.github.alshain01.flags.api.exception.InvalidAreaException;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.permissions.Permissible;

/**
 * Class for creating areas to manage a AreaWilderness.
  */
final class AreaWilderness extends AreaBase implements Nameable, Identifiable {
    private final World world;

    /**
     * Creates an instance of AreaWilderness based on a Bukkit Location
     *
     * @param location
     *            The Bukkit location
     */
    public AreaWilderness(Location location) {
        this(location.getWorld());
    }

    /**
     * Creates an instance of AreaWilderness based on a Bukkit World
     *
     * @param world
     *            The Bukkit world
     */
    public AreaWilderness(World world) {
        this.world = world;
    }

    /**
     * Creates an instance of AreaWilderness based on the world UUID
     *
     * @param worldId
     *            The Bukkit world UUID
     */
    public AreaWilderness(UUID worldId) { this(Bukkit.getWorld(worldId)); }

    @Override
    public UUID getUniqueId() {
        if(isArea()) return world.getUID();
        throw new InvalidAreaException();
    }

    @Override
    public String getId() {
        if(isArea()) return world.getName();
        throw new InvalidAreaException();
    }

    @Override
    public String getName() {
        if (isArea()) return world.getName();
        throw new InvalidAreaException();
    }

    @Override
    public AreaPlugin getCuboidPlugin() { return AreaPlugin.WILDERNESS; }

    @Override
    public org.bukkit.World getWorld() {
        if(isArea()) return world;
        throw new InvalidAreaException();
    }

    @Override
    public boolean isArea() {
        return world != null;
    }

    @Override
    public boolean hasFlagPermission(Permissible p) {
        Validate.notNull(p);
        return p.hasPermission("flags.area.flag.wilderness");
    }

    @Override
    public boolean hasBundlePermission(Permissible p) {
        Validate.notNull(p);
        return p.hasPermission("flags.area.bundle.wilderness");
    }

    @Override
    public Boolean getState(Flag flag, boolean absolute) {
        Validate.notNull(flag);

        final Boolean value = super.getState(flag, true);
        if (absolute) return value;
        return value != null ? value : flag.getDefault();
    }

    @Override
    public String getMessage(Flag flag, boolean parse) {
        Validate.notNull(flag);

        String message = Flags.getDataStore().readMessage(this, flag);

        if (message == null) {
            message = flag.getDefaultWildernessMessage();
        }

        if (parse) {
            message = message
                    .replace("{AreaType}", AreaPlugin.WILDERNESS.getCuboidName().toLowerCase())
                    .replace("{World}", getWorld().getName())
                    .replace("{AreaName}", getWorld().getName());
            message = ChatColor.translateAlternateColorCodes('&', message);
        }
        return message;
    }
}
