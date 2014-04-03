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
final public class ResidenceClaimedResidence extends Area implements Removable, Subdivision {
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
        if (!isArea()) { throw new InvalidAreaException(); }
        return null;
    }

    @Override
    public String getId() {
        if (!isArea()) { throw new InvalidAreaException(); }
        return residence.getName();
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
    public CuboidType getCuboidType() {
        return CuboidType.RESIDENCE;
    }

    @Override
    public String getName() {
        if (!isArea()) { throw new InvalidAreaException(); }
        return residence.getName();
    }

	@Override
	public Set<String> getOwnerNames() {
        if (!isArea()) { throw new InvalidAreaException(); }
        return new HashSet<String>(Arrays.asList(residence.getOwner()));
    }

    @Override
    public org.bukkit.World getWorld() {
        if (!isArea()) { throw new InvalidAreaException(); }
        return Bukkit.getServer().getWorld(residence.getWorld()); }

    @Override
    public boolean isArea() { return residence != null; }

	@Override
	public String getSystemSubID() {
        if (!isArea()) { throw new InvalidAreaException(); }
        if (!isSubdivision()) { throw new InvalidSubdivisionException(); }
		return residence.getName().split("\\.")[1];
	}

    @Override
    public boolean isSubdivision() {
        if (!isArea()) { throw new InvalidAreaException(); }
        return residence.getParent() != null;
    }

    @Override
    public boolean isParent(Area area) {
        if (!isArea()) { throw new InvalidAreaException(); }
        if (!isSubdivision()) { throw new InvalidSubdivisionException(); }
        return area instanceof ResidenceClaimedResidence &&
                residence.getParent().equals(((ResidenceClaimedResidence)area).getResidence());
    }

    @Override
    public Area getParent() {
        if (!isArea()) { throw new InvalidAreaException(); }
        if (!isSubdivision()) { throw new InvalidSubdivisionException(); }
        return new ResidenceClaimedResidence(residence.getParent().getName());
    }

    @Override
	public boolean isInherited() {
        if (!isArea()) { throw new InvalidAreaException(); }
        if (!isSubdivision()) { throw new InvalidSubdivisionException(); }
		return Flags.getDataStore().readInheritance(this);
	}

    @Override
    public void setInherited(boolean value) {
        if (!isArea()) { throw new InvalidAreaException(); }
        if (!isSubdivision()) { throw new InvalidSubdivisionException(); }
        Flags.getDataStore().writeInheritance(this, value);
    }

	/**
	 * Permanently removes the area from the data store USE CAUTION!
	 */
	@Override
	public void remove() {
        if (!isArea()) { throw new InvalidAreaException(); }
        Flags.getDataStore().remove(this);
    }

    /**
     * 0 if the the claims are the same
     * -1 if the claim is a subdivision of the provided claim.
     * 1 if the claim is a parent of the provided claim.
     * 2 if they are "sister" subdivisions. 3 if they are completely unrelated.
     *
     * @return The value of the comparison.
     */
    @Override
    public int compareTo(@Nonnull Area a) {
        Validate.notNull(a);
        if (!(a instanceof ResidenceClaimedResidence)) {
            return 3;
        }

        ClaimedResidence testRes = ((ResidenceClaimedResidence)a).getResidence();
        if (residence.equals(testRes)) {
            return 0;
        } else if (residence.getParent() != null && residence.getParent().equals(testRes)) {
            return -1;
        } else if (testRes.getParent() != null && testRes.getParent().equals(residence)) {
            return 1;
        } else if (residence.getParent() != null && residence.getParent().equals(testRes.getParent())) {
            return 2;
        }
        return 3;
    }
}
