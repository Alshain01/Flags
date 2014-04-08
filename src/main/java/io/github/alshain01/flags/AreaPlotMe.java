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

import io.github.alshain01.flags.api.CuboidPlugin;
import io.github.alshain01.flags.api.area.Ownable;
import io.github.alshain01.flags.api.exception.InvalidAreaException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.worldcretornica.plotme.Plot;
import com.worldcretornica.plotme.PlotManager;

/**
 * Class for creating areas to manage a PlotMe Plot.
 */
final class AreaPlotMe extends AreaRemovable implements Ownable {
	private final Plot plot;

	/**
	 * Creates an instance of AreaPlotMe based on a Bukkit Location
	 * 
	 * @param location
	 *            The Bukkit location
	 */
	public AreaPlotMe(Location location) {
		plot = PlotManager.getPlotById(location);
	}

	/**
	 * Creates an instance of AreaPlotMe based on a plot ID and Bukkit world
	 * 
	 * @param plotID
	 *            The plot ID
	 * @param world
	 *            The Bukkit world
	 */
	public AreaPlotMe(World world, String plotID) {
		plot = PlotManager.getPlotById(world, plotID);
	}

    /**
	 * Gets if there is a plot at the location.
	 * 
	 * @return True if a plot exists at the location.
	 */
	public static boolean hasPlot(Location location) {
		return PlotManager.getPlotById(location) != null;
	}

    @Override
    public String getId() {
        if (isArea()) return plot.id;
        throw new InvalidAreaException();
    }

    @Override
    public CuboidPlugin getCuboidPlugin() {
        return CuboidPlugin.PLOTME;
    }

    @Override
    public Set<UUID> getOwnerUniqueId() {
        //TODO: Waiting on PlotMe
        return new HashSet<UUID>(Arrays.asList(UUID.randomUUID()));
    }

    @Override
    public Set<String> getOwnerName() {
        if (isArea()) return new HashSet<String>(Arrays.asList(plot.owner));
        throw new InvalidAreaException();
    }

	@Override
	public World getWorld() {
        if (isArea())return Bukkit.getWorld(plot.world);
        throw new InvalidAreaException();
    }

	@Override
	public boolean isArea() {
        return plot != null;
    }
}
