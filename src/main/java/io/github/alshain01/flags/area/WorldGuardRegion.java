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

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * Class for creating areas to manage a WorldGuard Region.
 */
public class WorldGuardRegion extends Area implements Removable {
	private final ProtectedRegion region;
	private final String worldName;

    /**
     * Creates an instance of WorldGuardRegion based on a Bukkit Location
     *
     * @param location
     *            The Bukkit location
     */
	public WorldGuardRegion(Location location) {
		worldName = location.getWorld().getName();
		ProtectedRegion tempRegion = null;
		final ApplicableRegionSet regionSet = WGBukkit.getRegionManager(location.getWorld()).getApplicableRegions(location);
		if (regionSet != null) {
			int currentPriority = -2147483648;

			for (final ProtectedRegion region : regionSet) {
				if (region.getPriority() >= currentPriority) {
					tempRegion = region;
					currentPriority = region.getPriority();
				}
			}
		}
		this.region = tempRegion;
	}

    /**
     * Creates an instance of WorldGuardRegion based on a region ID
     *
     * @param world
     *            The world the region is in.
     * @param regionID
     *            The ID of the region.
     */
	public WorldGuardRegion(World world, String regionID) {
		this.worldName = world.getName();
		region = WGBukkit.getRegionManager(world).getRegionExact(regionID);
	}
	
	/**
	 * Gets if there is a region at the location.
	 * 
	 * @return True if a region exists at the location.
	 */
	public static boolean hasRegion(Location location) {
		return WGBukkit.getRegionManager(location.getWorld()).getApplicableRegions(location).size() != 0;
	}

    /**
     * Gets the region object embedded in the area class.
     *
     * @return The region object
     */
    public ProtectedRegion getRegion() {
        return region;
    }

	@Override
	public String getSystemID() { return !isArea() ? null : region.getId(); }

    @Override
    public System getSystemType() {
        return System.WORLDGUARD;
    }

    @Override
    public Set<String> getOwners() { return getRegion().getOwners().getPlayers(); }

	@Override
	public org.bukkit.World getWorld() { return Bukkit.getWorld(worldName);	}

	@Override
	public boolean isArea() { return region != null && getWorld() != null; }

	@Override
	public void remove() { Flags.getDataStore().remove(this); }

    /**
     * 0 if the the worlds are the same, 3 if they are not.
     *
     * @return The value of the comparison.
     */
    @Override
    public int compareTo(Area a) {
        return a instanceof WorldGuardRegion && a.getSystemID().equals(getSystemID()) ? 0 : 3;
    }
}