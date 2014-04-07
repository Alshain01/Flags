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

import io.github.alshain01.flags.api.CuboidType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import io.github.alshain01.flags.api.area.Nameable;
import io.github.alshain01.flags.api.area.Ownable;
import io.github.alshain01.flags.api.exception.InvalidAreaException;
import org.bukkit.Location;
import org.bukkit.World;

import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColls;
import com.massivecraft.mcore.ps.PS;

/**
 * Class for creating areas to manage a Factions Territory.
 */
final public class AreaFactions extends AreaRemovable implements Nameable, Ownable {
	private final Faction faction;
	private final World world;

	/**
	 * Creates an instance of AreaFactions based on a Bukkit Location
	 * 
	 * @param location
	 *            The Bukkit location
	 */
	public AreaFactions(Location location) {
		faction = BoardColls.get().getFactionAt(PS.valueOf(location));
		world = location.getWorld();
	}

	/**
	 * Creates an instance of AreaFactions based on a Bukkit World and
	 * faction ID
	 * 
	 * @param factionID
	 *            The faction ID
	 * @param world
	 *            The Bukkit world
	 */
	public AreaFactions(World world, String factionID) {
		faction = FactionColls.get().getForWorld(world.getName()).get(factionID);
		this.world = world;
	}

    /**
     * Gets the faction object embedded in the area class.
     *
     * @return The faction object
     */
    @SuppressWarnings("WeakerAccess") // API
    public Faction getFaction() {
        return faction;
    }

    /**
     * Gets if there is a territory at the location.
     *
     * @return True if a territory exists at the location.
     */
    public static boolean hasTerritory(Location location) {
        return BoardColls.get().getFactionAt(PS.valueOf(location)) != null;
    }

    @Override
    public UUID getUniqueId() {
        if (isArea()) return null;
        throw new InvalidAreaException();
    }

    public String getId() {
        if (isArea()) return getFaction().getId();
        throw new InvalidAreaException();
    }

    @Override
    public CuboidType getCuboidType() {
        return CuboidType.FACTIONS;
    }

    @Override
    public String getName() {
        if (isArea()) return faction.getName();
        throw new InvalidAreaException();
    }

    @Override
    public Set<UUID> getOwnerUniqueId() {
        //TODO Waiting on Factions
        return new HashSet<UUID>(Arrays.asList(UUID.randomUUID()));
    }

	@Override
	public Set<String> getOwnerName() {
        if (isArea()) return new HashSet<String>(Arrays.asList(getFaction().getLeader().getName()));
        throw new InvalidAreaException();
    }

	@Override
	public World getWorld() {
        if (isArea()) return world;
        throw new InvalidAreaException();
    }

	@Override
	public boolean isArea() {
        return world != null && faction != null;
    }
}
