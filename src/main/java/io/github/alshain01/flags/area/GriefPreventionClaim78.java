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
import io.github.alshain01.flags.exception.InvalidAreaException;
import io.github.alshain01.flags.exception.InvalidSubdivisionException;
import me.ryanhamshire.GriefPrevention.Claim;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * Class for creating areas to manage a Grief Prevention Claim.
 */
final public class GriefPreventionClaim78 extends GriefPreventionClaim implements Subdivision {

	/**
	 * Creates an instance of GriefPreventionClaim78 based on a Bukkit Location
	 * 
	 * @param location
	 *            The Bukkit location
	 */
	public GriefPreventionClaim78(Location location) {
		super(location);
	}

	/**
	 * Creates an instance of GriefPreventionClaim78 based on a claim ID
	 * 
	 * @param id
	 *            The claim id
	 */
	public GriefPreventionClaim78(long id) {
		super(id);
	}

    /**
     * Creates an instance of GriefPreventionClaim based on a claim object
     *
     * @param claim
     *            The claim object
     */
    @SuppressWarnings("WeakerAccess") // API
    public GriefPreventionClaim78(Claim claim) {
        super(claim);
    }

    @Override
    public org.bukkit.World getWorld() {
        if (isArea()) return Bukkit.getServer().getWorld(claim.getClaimWorldName());
        throw new InvalidAreaException();
    }


    @Override
    public boolean isSubdivision() {
        if (isArea()) return claim.parent != null;
        throw new InvalidAreaException();
    }

    @Override
    public boolean isParent(Area area) {
        if (isSubdivision()) {
            Validate.notNull(area);
            return area instanceof GriefPreventionClaim78 && claim.parent.equals(((GriefPreventionClaim78) area).getClaim());
        }
        throw new InvalidSubdivisionException();
    }

    @Override
    public Area getParent() {
        if (isSubdivision()) return new GriefPreventionClaim78(claim);
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
}
