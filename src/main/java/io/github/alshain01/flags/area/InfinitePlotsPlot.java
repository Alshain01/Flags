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

import io.github.alshain01.flags.Flags;
import io.github.alshain01.flags.System;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;

import uk.co.jacekk.bukkit.infiniteplots.InfinitePlots;
import uk.co.jacekk.bukkit.infiniteplots.plot.Plot;
import uk.co.jacekk.bukkit.infiniteplots.plot.PlotLocation;

/**
 * Class for creating areas to manage a InfinitePlots Plot.
 */
public class InfinitePlotsPlot extends Area implements Removable {
	private final Plot plot;

	/**
	 * Creates an instance of InfinitePlotsPlot based on a Bukkit Location
	 * 
	 * @param location
	 *            The Bukkit location
	 */
	public InfinitePlotsPlot(Location location) {
		plot = InfinitePlots.getInstance().getPlotManager()
				.getPlotAt(PlotLocation.fromWorldLocation(location));
	}

	/**
	 * Creates an instance of InfinitePlotsPlot based on a Bukkit world and Plot
	 * Location
	 * 
	 * @param world
	 *            The Bukkit world
	 * @param X
	 *            The Plot X Location (not Bukkit location)
	 * @param Z
	 *			  The Plot Z Location (not Bukkit location)
	 */
	public InfinitePlotsPlot(World world, int X, int Z) {
		plot = InfinitePlots.getInstance().getPlotManager()
				.getPlotAt(new PlotLocation(world.getName(), X, Z));
	}

	/**
	 * Gets if there is a plot at the location.
	 * 
	 * @return True if a territory exists at the plot.
	 */
	public static boolean hasPlot(Location location) {
		return InfinitePlots.getInstance().getPlotManager().getPlotAt(PlotLocation.fromWorldLocation(location)) != null;
	}

    /**
     * Gets the plot object embedded in the area class.
     *
     * @return The plot object
     */
    public Plot getPlot() {
        return plot;
    }

    @Override
    public String getSystemID() {
        return isArea() ? getPlot().getLocation().getX() + ";" + getPlot().getLocation().getZ() : null;
    }

    @Override
    public System getSystemType() {
        return System.INFINITEPLOTS;
    }

	@Override
	public Set<String> getOwners() { return new HashSet<String>(Arrays.asList(getPlot().getAdmin())); }

	@Override
	public World getWorld() { return getPlot().getLocation().getWorld(); }

	@Override
	public boolean isArea() { return plot != null; }

	@Override
	public void remove() { Flags.getDataStore().remove(this); }

    /**
     * 0 if the the worlds are the same, 3 if they are not.
     *
     * @return The value of the comparison.
     */
    @Override
    public int compareTo(Area a) {
        return a instanceof InfinitePlotsPlot && a.getSystemID().equals(getSystemID()) ? 0 : 3;
    }
}