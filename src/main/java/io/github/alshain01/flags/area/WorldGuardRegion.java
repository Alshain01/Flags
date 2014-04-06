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

import java.util.Set;
import java.util.UUID;

import io.github.alshain01.flags.exception.InvalidAreaException;
import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * Class for creating areas to manage a WorldGuard Region.
 */
final public class WorldGuardRegion extends RemovableArea {
	private final ProtectedRegion region;
	private final World world;

    /**
     * Creates an instance of WorldGuardRegion based on a Bukkit Location
     *
     * @param location
     *            The Bukkit location
     */
	public WorldGuardRegion(Location location) {
		world = location.getWorld();
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
		this.world = world;
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
    @SuppressWarnings("unused") // API
    public ProtectedRegion getRegion() {
        if (isArea()) return region;
        throw new InvalidAreaException();
    }

    @Override
    public UUID getUniqueId() {
        if (isArea()) return null;
        throw new InvalidAreaException();
    }

    @Override
    public String getId() {
        if (isArea()) return region.getId();
        throw new InvalidAreaException();
    }

    @Override
    public CuboidType getCuboidType() {
        return CuboidType.WORLDGUARD;
    }

    @Override
    public String getName() {
        if (isArea()) return region.getId();
        throw new InvalidAreaException();
    }

    @Override
    public Set<String> getOwnerNames() {
        if (isArea()) return region.getOwners().getPlayers();
        throw new InvalidAreaException();
    }

	@Override
	public org.bukkit.World getWorld() {
        if (isArea()) return world;
        throw new InvalidAreaException();
    }

	@Override
	public boolean isArea() {
        return region != null && world != null;
    }
}
