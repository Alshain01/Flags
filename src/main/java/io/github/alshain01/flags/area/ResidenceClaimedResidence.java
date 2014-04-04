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
import io.github.alshain01.flags.exception.InvalidSubdivisionException;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import javax.annotation.Nonnull;

/**
 * Class for creating areas to manage a Residence Claimed Residences.
 */
final public class ResidenceClaimedResidence extends RemovableArea implements Subdivision {
	private final ClaimedResidence residence;

	/**
	 * Creates an instance of ResidenceClaimedResidence based on a Bukkit
	 * Location
	 * 
	 * @param location
	 *            The Bukkit location
	 */
	public ResidenceClaimedResidence(Location location) {
		residence = Residence.getResidenceManager().getByLoc(location);
	}


	/**
	 * Creates an instance of ResidenceClaimedResidence based on a residence
	 * name
	 * 
	 * @param name
	 *            The residence name
	 */
	public ResidenceClaimedResidence(String name) {
		residence = Residence.getResidenceManager().getByName(name);
	}

    /**
     * Creates an instance of ResidenceClaimedResidence based on a ClaimedResidence object
     *
     * @param residence
     *            The residence object
     */
    public ResidenceClaimedResidence(ClaimedResidence residence) {
        this.residence = residence;
    }

	/**
	 * Gets if there is a residence at the location.
	 * 
	 * @return True if a residence exists at the location.
	 */
	public static boolean hasResidence(Location location) {
		return Residence.getResidenceManager().getByLoc(location) != null;
	}

    /**
     * Gets the ClaimedResidence object embedded in the area class.
     *
     * @return The ClaimedResidence object
     */
    @SuppressWarnings("WeakerAccess") // API
    public ClaimedResidence getResidence() {
        return residence;
    }

    @Override
    public UUID getUniqueId() {
        if (isArea()) return null;
        throw new InvalidAreaException();
    }

    @Override
    public String getId() {
        if (isArea()) return residence.getName();
        throw new InvalidAreaException();
    }

    @Override
    public CuboidType getCuboidType() {
        return CuboidType.RESIDENCE;
    }

    @Override
    public String getName() {
        if (isArea()) return residence.getName();
        throw new InvalidAreaException();
    }

	@Override
	public Set<String> getOwnerNames() {
        if (isArea()) return new HashSet<String>(Arrays.asList(residence.getOwner()));
        throw new InvalidAreaException();
    }

    @Override
    public org.bukkit.World getWorld() {
        if (isArea()) return Bukkit.getServer().getWorld(residence.getWorld());
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
    public boolean isParent(Area area) {
        if (isSubdivision()) return area instanceof ResidenceClaimedResidence &&
                    residence.getParent().equals(((ResidenceClaimedResidence) area).getResidence());
        throw new InvalidSubdivisionException();
    }

    @Override
    public Area getParent() {
        if (isSubdivision()) return new ResidenceClaimedResidence(residence.getParent());
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
        }
        throw new InvalidSubdivisionException();
    }

    @Override
    @Deprecated
    public String getSystemID() {
        if (!isArea()) { throw new InvalidAreaException(); }
        return residence.getParent() != null ? residence.getParent().getName() : residence.getName();
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public System getSystemType() { return System.RESIDENCE; }

    @Override
    @Deprecated
    public String getSystemSubID() {
        if (!isSubdivision()) { throw new InvalidSubdivisionException(); }
        return residence.getName().split("\\.")[1];
    }
}
