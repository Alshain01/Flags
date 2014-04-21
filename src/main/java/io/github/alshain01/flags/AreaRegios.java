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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import io.github.alshain01.flags.api.AreaPlugin;
import io.github.alshain01.flags.api.area.Nameable;
import io.github.alshain01.flags.api.area.Ownable;
import io.github.alshain01.flags.api.exception.InvalidAreaException;
import net.jzx7.regiosapi.RegiosAPI;
import net.jzx7.regiosapi.events.RegionDeleteEvent;
import net.jzx7.regiosapi.regions.Region;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/**
 * Class for creating areas to manage a Regios Region.
 */
final class AreaRegios extends AreaRemovable implements Nameable, Ownable {
	private final Region region;

	/**
	 * Creates an instance of AreaRegios based on a Bukkit Location
	 * 
	 * @param location
	 *            The Bukkit location
	 */
	public AreaRegios(Location location) {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Regios");
		if(plugin == null || !((RegiosAPI)plugin).isInRegion(location)) {
			region = null;
			return;
		}
		region = ((RegiosAPI)plugin).getRegion(location);
	}
	
	/**
	 * Creates an instance of AreaRegios based on a region name
	 * 
	 * @param name
	 *            The region name
	 */
	public AreaRegios(String name) {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Regios");
		if(plugin == null) {
			region = null;
			return;
		}
		region = ((RegiosAPI)plugin).getRegion(name);
	}

    /**
     * Creates an instance of AreaRegios based on a region
     *
     * @param region
     *            The region
     */
    public AreaRegios(Region region) {
        this.region = region;
    }
	
	/**
	 * Gets if there is a region at the location.
	 * 
	 * @return True if a region exists at the location.
	 */
	public static boolean hasRegion(Location location) {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Regios");
        return plugin != null && ((RegiosAPI)plugin).isInRegion(location);
	}

    @Override
	public Set<OfflinePlayer> getOwners() {
        if (isArea()) return new HashSet<OfflinePlayer>(Arrays.asList(Bukkit.getOfflinePlayer(region.getOwner())));
        throw new InvalidAreaException();
    }

    @Override
    public AreaPlugin getCuboidPlugin() {
        return AreaPlugin.REGIOS;
    }

    @Override
    public String getName() {
        if (isArea()) return region.getName();
        throw new InvalidAreaException();
    }

    @Override
    public String getId() {
        if (isArea()) return region.getName();
        throw new InvalidAreaException();
    }

	@Override
	public World getWorld() {
        if (isArea()) return Bukkit.getWorld(region.getWorld().getName());
        throw new InvalidAreaException();
    }

	@Override
	public boolean isArea() {
        return region != null;
    }

    static class Cleaner implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        private static void onRegionDelete(RegionDeleteEvent e) {
            // Cleanup the database, keep the file from growing too large.
            new AreaRegios(e.getRegion()).remove();
        }
    }

}
