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

import java.util.HashSet;
import java.util.Set;

import com.sk89q.worldedit.BlockVector;
import io.github.alshain01.flags.api.AreaPlugin;
import io.github.alshain01.flags.api.area.Cuboid;
import io.github.alshain01.flags.api.area.Nameable;
import io.github.alshain01.flags.api.area.Ownable;
import io.github.alshain01.flags.api.exception.InvalidAreaException;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * Class for creating areas to manage a WorldGuard Region.
 */
final class AreaWorldGuard extends AreaRemovable implements Nameable, Ownable, Cuboid {
	private final ProtectedRegion region;
	private final World world;

    /**
     * Creates an instance of AreaWorldGuard based on a Bukkit Location
     *
     * @param location
     *            The Bukkit location
     */
	public AreaWorldGuard(Location location) {
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
     * Creates an instance of AreaWorldGuard based on a region ID
     *
     * @param world
     *            The world the region is in.
     * @param regionID
     *            The ID of the region.
     */
	public AreaWorldGuard(World world, String regionID) {
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

    @Override
    public String getId() {
        if (isArea()) return region.getId();
        throw new InvalidAreaException();
    }

    @Override
    public String getName() {
        // WorldGuard's ID is a name
        return getId();
    }

    @Override
    public AreaPlugin getAreaPlugin() {
        return AreaPlugin.WORLDGUARD;
    }

    @Override
    public Set<OfflinePlayer> getOwners() {
        if (isArea()) {
            Set<OfflinePlayer> owners = new HashSet<OfflinePlayer>();
            for(String player : region.getOwners().getPlayers())
                owners.add(PlayerCache.getOfflinePlayer(player));

            return owners;
        }
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

    @Override
    public Location getGreaterCorner() {
        if (isArea()) {
            BlockVector point = region.getMaximumPoint();
            return new Location(getWorld(), point.getBlockX(), point.getBlockY(), point.getBlockZ());
        }
        throw new InvalidAreaException();
    }

    @Override
    public Location getLesserCorner() {
        if (isArea()) {
            BlockVector point = region.getMinimumPoint();
            return new Location(getWorld(), point.getBlockX(), point.getBlockY(), point.getBlockZ());
        }
        throw new InvalidAreaException();
    }

    @Override
    public Location getAdjustedGreaterCorner() {
        return getGreaterCorner();
    }

    @Override
    public Location getAdjustedLesserCorner() {
        return getLesserCorner();
    }
}
