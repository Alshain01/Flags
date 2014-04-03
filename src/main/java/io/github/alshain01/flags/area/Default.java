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

package io.github.alshain01.flags.area;

import io.github.alshain01.flags.*;
import io.github.alshain01.flags.System;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import io.github.alshain01.flags.exception.InvalidAreaException;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.permissions.Permissible;

import javax.annotation.Nonnull;

/**
 * Class for creating areas to manage server defaults.
 */
final public class Default extends Area {
	private final World world;

	/**
	 * Creates an instance of Default based on a Bukkit Location
	 * 
	 * @param location
	 *            The Bukkit location
	 */
	public Default(Location location) {
		this(location.getWorld());
	}

	/**
	 * Creates an instance of Default based on a Bukkit World
	 * 
	 * @param world
	 *            The Bukkit world
	 */
	public Default(World world) {
		this.world = world;
	}
	
	/**
	 * Creates an instance of Default based on a Bukkit World name
	 * 
	 * @param worldName
	 *            The Bukkit world
	 */
	public Default(String worldName) {
        this.world = Bukkit.getWorld(worldName);
	}

    @Override
    public UUID getUniqueId() {
        if(!isArea()) { throw new InvalidAreaException(); }
        return world.getUID();
    }

    @Override
    public String getSystemID() {
        if(!isArea()) { throw new InvalidAreaException(); }
        return world.getName();
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public System getSystemType() {
        return System.DEFAULT;
    }

    @Override
    public CuboidType getCuboidType() {
        return CuboidType.DEFAULT;
    }

    @Override
    public String getName() {
        if(!isArea()) { throw new InvalidAreaException(); }
        return world.getName() + " Default";
    }

    @Override
    public Set<String> getOwnerNames() {
        if(!isArea()) { throw new InvalidAreaException(); }
        return new HashSet<String>(Arrays.asList("default"));
    }

    @Override
    public World getWorld() {
        if(!isArea()) { throw new InvalidAreaException(); }
        return world;
    }

    @Override
    public boolean isArea() { return world != null; }

    @Override
    public boolean hasBundlePermission(Permissible p) {
        if(!isArea()) { throw new InvalidAreaException(); }
        Validate.notNull(p);
        return p.hasPermission("flags.area.bundle.default");
    }

    @Override
    public boolean hasPermission(Permissible p) {
        if(!isArea()) { throw new InvalidAreaException(); }
        Validate.notNull(p);
        return p.hasPermission("flags.area.flag.default");
    }

    @Override
    public Boolean getValue(Flag flag, boolean absolute) {
        if(!isArea()) { throw new InvalidAreaException(); }
        Validate.notNull(flag);

        final Boolean value = super.getValue(flag, true);
        if (absolute) { return value; }
        return value != null ? value : flag.getDefault();
    }

	/**
	 * Gets the message associated with a player flag.
	 * 
	 * @param flag
	 *            The flag to retrieve the message for.
	 * @param parse
	 *            Ignored by Default area.
	 * @return The message associated with the flag.
	 */
	@Override
	public String getMessage(Flag flag, boolean parse) {
        if (!isArea()) { throw new InvalidAreaException(); }
        Validate.notNull(flag);

		// We are ignore parse here. We just want to override it.
		final String message = Flags.getDataStore().readMessage(this, flag);
		return message != null ? message : flag.getDefaultAreaMessage();
	}

    /**
     * 0 if the the worlds are the same, 3 if they are not.
     *
     * @return The value of the comparison.
     */
    @Override
    public int compareTo(@Nonnull Area a) {
        Validate.notNull(a);
        return a instanceof Default && getSystemID().equals(a.getSystemID()) ? 0 : 3;
    }
}