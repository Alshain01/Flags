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
import io.github.alshain01.flags.api.area.*;
import io.github.alshain01.flags.api.exception.InvalidAreaException;
import io.github.alshain01.flags.api.exception.InvalidSubdivisionException;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.areas.ResidenceArea;
import net.t00thpick1.residence.api.events.ResidenceAreaDeletedEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import javax.annotation.Nonnull;

/**
 * Class for creating areas to manage a Residence Claimed Residences.
 */
final class AreaResidence extends AreaRemovable implements Identifiable, Cuboid, Renameable, Ownable, Subdividable {
	private ResidenceArea residence;

	/**
	 * Creates an instance of AreaResidence based on a Bukkit
	 * Location
	 * 
	 * @param location
	 *            The Bukkit location
	 */
	public AreaResidence(Location location) {
		residence = ResidenceAPI.getResidenceManager().getByLocation(location);
	}


	/**
	 * Creates an instance of AreaResidence based on a residence
	 * name
	 * 
	 * @param id
	 *            The residence UUID as a string
	 */
	public AreaResidence(String id) {
		residence = ResidenceAPI.getResidenceManager().getByUUID(UUID.fromString(id));
	}

    /**
     * Creates an instance of AreaResidence based on a ClaimedResidence object
     *
     * @param residence
     *            The residence object
     */
    private AreaResidence(ResidenceArea residence) {
        this.residence = residence;
    }

	/**
	 * Gets if there is a residence at the location.
	 * 
	 * @return True if a residence exists at the location.
	 */
	public static boolean hasResidence(Location location) {
		return ResidenceAPI.getResidenceManager().getByLocation(location) != null;
	}

    @Override
    public UUID getUniqueId() {
        if (isArea()) return residence.getResidenceUUID();
        throw new InvalidAreaException();
    }

    @Override
    public String getId() {
        if (isArea()) return residence.getResidenceUUID().toString();
        throw new InvalidAreaException();
    }

    @Override
    public AreaPlugin getAreaPlugin() {
        return AreaPlugin.RESIDENCE;
    }

    @Override
    public String getName() {
        if (isArea()) return residence.getName();
        throw new InvalidAreaException();
    }

    @Override
    public void setName(@Nonnull String name) {
        if (isArea())
            residence.rename(name);
        else
            throw new InvalidAreaException();
    }

	@Override
	public Set<OfflinePlayer> getOwners() {
        if (isArea()) return new HashSet<OfflinePlayer>(Arrays.asList(Bukkit.getOfflinePlayer(residence.getOwner())));
        throw new InvalidAreaException();
    }

    @Override
    public org.bukkit.World getWorld() {
        if (isArea()) return residence.getWorld();
        throw new InvalidAreaException();
    }

    @Override
    public boolean isArea() {
        return residence != null;
    }

    @Override
    public boolean isSubdivision() {
        if (isArea()) return residence.getParent() != null;
        throw new InvalidAreaException();
    }

    @Override
    public boolean isParent(@Nonnull Area area) {
        if (isSubdivision()) return area instanceof AreaResidence &&
                    residence.getParent().equals(((AreaResidence) area).residence);
        throw new InvalidSubdivisionException();
    }

    @Override
    public Area getParent() {
        if (isSubdivision()) return new AreaResidence(residence.getParent());
        throw new InvalidSubdivisionException();
    }

    @Override
    public void transformParent() {
        if (isSubdivision()) {
            this.residence = residence.getParent();
            return;
        }
        throw new InvalidSubdivisionException();
    }

    @Override
	public boolean isInherited() {
        if (isSubdivision()) return Flags.getDataStore().readInheritance(this);
        throw new InvalidSubdivisionException();
	}

    @Override
    public void setInherited(boolean value) {
        if (isSubdivision()) {
            Flags.getDataStore().writeInheritance(this, value);
            return;
        }
        throw new InvalidSubdivisionException();
    }

    @Override
    public Location getGreaterCorner() {
        if (isArea()) return residence.getHighLocation();
        throw new InvalidAreaException();
    }

    @Override
    public Location getLesserCorner() {
        if (isArea()) return residence.getLowLocation();
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

    static class Cleaner implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        private static void onResidenceDelete(ResidenceAreaDeletedEvent e) {
            // Cleanup the database, keep the file from growing too large.
            new AreaResidence(e.getResidenceArea()).remove();
        }
    }
}
