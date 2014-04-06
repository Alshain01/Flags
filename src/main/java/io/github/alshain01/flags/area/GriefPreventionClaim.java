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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import io.github.alshain01.flags.exception.InvalidAreaException;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * Class for creating areas to manage a Grief Prevention Claim.
 */
public class GriefPreventionClaim extends RemovableArea implements Ownable, Siegeable, Administrator {
	final Claim claim;

	/**
	 * Creates an instance of GriefPreventionClaim based on a Bukkit Location
	 * 
	 * @param location
	 *            The Bukkit location
	 */
	public GriefPreventionClaim(Location location) {
		claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
	}

	/**
	 * Creates an instance of GriefPreventionClaim based on a claim object
	 *
	 * @param claim
	 *            The claim object
	 */
    @SuppressWarnings("WeakerAccess") // API
	public GriefPreventionClaim(Claim claim) {
		this.claim = claim;
	}

    /**
     * Creates an instance of GriefPreventionClaim based on a claim ID
     *
     * @param ID
     *            The claim ID
     */
    public GriefPreventionClaim(long ID) {
        claim = GriefPrevention.instance.dataStore.getClaim(ID);
    }

	/**
	 * Gets if there is a claim at the location.
	 * 
	 * @return True if a claim exists at the location.
	 */
	public static boolean hasClaim(Location location) {
		return GriefPrevention.instance.dataStore.getClaimAt(location, false, null) != null;
	}

    /**
     * Gets the claim object embedded in the area class.
     *
     * @return The claim object
     */
    @SuppressWarnings("WeakerAccess") // API
    public Claim getClaim() {
        return claim;
    }

    @Override
    public UUID getUniqueId() {
        if (isArea()) return null;
        throw new InvalidAreaException();
    }

    @Override
    public String getId() {
        if (isArea()) return String.valueOf(claim.getID());
        throw new InvalidAreaException();
    }

    @Override
    public CuboidType getCuboidType() {
        return CuboidType.GRIEF_PREVENTION;
    }

    @Override
    public Set<String> getOwnerName() {
        if (isArea()) return new HashSet<String>(Arrays.asList(claim.getOwnerName()));
        throw new InvalidAreaException();
    }

    @Override
    public Set<UUID> getOwnerUniqueId() {
        //TODO: Waiting on GriefPrevention
        return new HashSet<UUID>(Arrays.asList(UUID.randomUUID()));
    }

	@Override
	public World getWorld() {
        if (isArea()) return claim.getGreaterBoundaryCorner().getWorld();
        throw new InvalidAreaException();
    }

    @Override
    public boolean isArea() {
        return claim != null;
    }

	@Override
	public boolean isAdminArea() {
        if (isArea()) return claim.isAdminClaim();
        throw new InvalidAreaException();
    }

	@Override
	public boolean isUnderSiege() {
        if (isArea()) return claim.siegeData != null;
        throw new InvalidAreaException();
    }
}
