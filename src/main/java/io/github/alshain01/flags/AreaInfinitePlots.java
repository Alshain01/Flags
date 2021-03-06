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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.github.alshain01.flags.api.area.Cuboid;
import io.github.alshain01.flags.api.area.Ownable;
import io.github.alshain01.flags.api.area.Renameable;
import io.github.alshain01.flags.api.exception.InvalidAreaException;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import uk.co.jacekk.bukkit.infiniteplots.InfinitePlots;
import uk.co.jacekk.bukkit.infiniteplots.event.PlotUnclaimedEvent;
import uk.co.jacekk.bukkit.infiniteplots.plot.Plot;
import uk.co.jacekk.bukkit.infiniteplots.plot.PlotLocation;

import javax.annotation.Nonnull;

/**
 * Class for creating areas to manage a InfinitePlots Plot.
 */
final class AreaInfinitePlots extends AreaRemovable implements Cuboid, Renameable, Ownable {
	private final Plot plot;

	/**
	 * Creates an instance of AreaInfinitePlots based on a Bukkit Location
	 * 
	 * @param location
	 *            The Bukkit location
	 */
	public AreaInfinitePlots(Location location) {
		plot = InfinitePlots.getInstance().getPlotManager()
				.getPlotAt(PlotLocation.fromWorldLocation(location));
	}

	/**
	 * Creates an instance of AreaInfinitePlots based on a Bukkit world and Plot
	 * Location
	 * 
	 * @param world
	 *            The Bukkit world
	 * @param X
	 *            The Plot X Location (not Bukkit location)
	 * @param Z
	 *			  The Plot Z Location (not Bukkit location)
	 */
	public AreaInfinitePlots(World world, int X, int Z) {
		plot = InfinitePlots.getInstance().getPlotManager()
				.getPlotAt(new PlotLocation(world.getName(), X, Z));
	}

    /**
     * Creates an instance of AreaInfinitePlots using an exiting plot
     *
     * @param plot
     *            The plot to create the area for
     */
    public AreaInfinitePlots(Plot plot) {
        this.plot = plot;
    }

	/**
	 * Gets if there is a plot at the location.
	 * 
	 * @return True if a territory exists at the plot.
	 */
	public static boolean hasPlot(Location location) {
		return InfinitePlots.getInstance().getPlotManager().getPlotAt(PlotLocation.fromWorldLocation(location)) != null;
	}

    @Override
    public String getId() {
        if (isArea()) return plot.getLocation().getX() + ";" + plot.getLocation().getZ();
        throw new InvalidAreaException();
    }

    @Override
    public AreaPlugin getAreaPlugin() {
        return AreaPlugin.INFINITEPLOTS;
    }

    @Override
    public String getName() {
        if (isArea()) return plot.getName();
        throw new InvalidAreaException();
    }

    @Override
    public void setName(@Nonnull String name) {
        if (isArea())
            plot.setName(name);
        else
            throw new InvalidAreaException();
    }

    @Override
    public Set<OfflinePlayer> getOwners()
    {
        if (isArea()) return new HashSet<OfflinePlayer>(Arrays.asList(plot.getAdmin()));
        throw new InvalidAreaException();
    }

	@Override
	public World getWorld() {
        if (isArea()) return plot.getLocation().getWorld();
        throw new InvalidAreaException();
    }

    @Override
    public Location getLesserCorner() {
        int[] coordinates = plot.getBuildLimits();
        return new Location(getWorld(), coordinates[0], 0, coordinates[1]);
    }

    @Override
    public Location getGreaterCorner() {
        int[] coordinates = plot.getBuildLimits();
        return new Location(getWorld(), coordinates[2], 255, coordinates[3]);
    }

    @Override
    public Location getAdjustedLesserCorner() {
        return getLesserCorner();
    }

    @Override
    public Location getAdjustedGreaterCorner() {
        return getGreaterCorner();
    }

	@Override
	public boolean isArea() {
        return plot != null;
    }

    static class Cleaner implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        private static void onPlotUnclaimed(PlotUnclaimedEvent e) {
            new AreaInfinitePlots(e.getPlot()).remove();
        }
    }
}
