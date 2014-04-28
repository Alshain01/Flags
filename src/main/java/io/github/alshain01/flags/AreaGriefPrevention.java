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

import java.util.*;

import io.github.alshain01.flags.api.AreaPlugin;
import io.github.alshain01.flags.api.area.*;
import io.github.alshain01.flags.api.exception.InvalidAreaException;
import io.github.alshain01.flags.api.exception.InvalidSubdivisionException;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import me.ryanhamshire.GriefPrevention.events.ClaimDeletedEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import javax.annotation.Nonnull;

/**
 * Class for creating areas to manage a Grief Prevention Claim.
 */
final class AreaGriefPrevention extends AreaRemovable implements Administrator, Identifiable, Ownable, Cuboid, Siegeable, Subdividable  {
	private Claim claim;

	/**
	 * Creates an instance of AreaGriefPrevention based on a Bukkit Location
	 * 
	 * @param location
	 *            The Bukkit location
	 */
	public AreaGriefPrevention(Location location) {
		claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
	}

	/**
	 * Creates an instance of AreaGriefPrevention based on a claim object
	 *
	 * @param claim
	 *            The claim object
	 */
	private AreaGriefPrevention(Claim claim) {
		this.claim = claim;
	}

    /**
     * Creates an instance of AreaGriefPrevention based on a claim ID
     *
     * @param uniqueId
     *            The claim UUID
     */
    public AreaGriefPrevention(UUID uniqueId) {
        claim = GriefPrevention.instance.dataStore.getClaim(uniqueId);
    }

	/**
	 * Gets if there is a claim at the location.
	 * 
	 * @return True if a claim exists at the location.
	 */
	public static boolean hasClaim(Location location) {
		return GriefPrevention.instance.dataStore.getClaimAt(location, false, null) != null;
	}

    @Override
    public String getId() {
        if (isArea()) return String.valueOf(claim.getUUID());
        throw new InvalidAreaException();
    }

    @Override
    public UUID getUniqueId() {
        if (isArea()) return claim.getUUID();
        throw new InvalidAreaException();
    }

    @Override
    public AreaPlugin getAreaPlugin() {
        return AreaPlugin.GRIEF_PREVENTION;
    }

    @Override
    public boolean isArea() {
        return claim != null;
    }

    @Override
    public Location getGreaterCorner() {
        if (isArea()) return claim.getGreaterBoundaryCorner();
        throw new InvalidAreaException();
    }

    @Override
    public Location getLesserCorner() {
        if (isArea()) return claim.getLesserBoundaryCorner();
        throw new InvalidAreaException();
    }

    @Override
    public Location getAdjustedGreaterCorner() {
        if (isArea()) {
            Location corner = claim.getGreaterBoundaryCorner();
            corner.setY(corner.getY() + claim.getHeight());
            return corner;
        }
        throw new InvalidAreaException();
    }

    @Override
    public Location getAdjustedLesserCorner() {
        if (isArea()) return claim.getLesserBoundaryCorner();
        throw new InvalidAreaException();
    }

    @Override
    public Collection<OfflinePlayer> getOwners() {
        //TODO: Waiting on GriefPrevention to update to UUID
        if (isArea()) return new HashSet<OfflinePlayer>(Arrays.asList(PlayerCache.getOfflinePlayer(claim.getOwnerName())));
        throw new InvalidAreaException();
    }

	@Override
	public boolean isAdminArea() {
        if (isArea()) return claim.isAdminClaim();
        return false;
    }

	@Override
	public boolean isUnderSiege() {
        if (isArea()) return claim.siegeData != null;
        return false;
    }

    @Override
    public boolean canSiege(@Nonnull Player player) {
        if (isArea()) return claim.canSiege(player);
        return false;
    }

    @Override
    public org.bukkit.World getWorld() {
        if (isArea()) return Bukkit.getServer().getWorld(claim.getClaimWorldName());
        throw new InvalidAreaException();
    }

    @Override
    public boolean isSubdivision() {
        if (isArea()) return claim.parent != null;
        return false;
    }

    @Override
    public boolean isParent(@Nonnull Area area) {
        if (isSubdivision())
            return area instanceof AreaGriefPrevention && claim.parent.equals(((AreaGriefPrevention) area).claim);
        return false;
    }

    @Override
    public Area getParent() {
        if (isSubdivision()) return new AreaGriefPrevention(claim.parent);
        throw new InvalidSubdivisionException();
    }

    @Override
    public void transformParent() {
        if (isSubdivision()) {
            this.claim = claim.parent;
            return;
        }
        throw new InvalidSubdivisionException();
    }

    @Override
    public boolean isInherited() {
        if (isSubdivision()) return Flags.getDataStore().readInheritance(this);
        return false;
    }

    @Override
    public void setInherited(boolean value) {
        if (isSubdivision()) {
            Flags.getDataStore().writeInheritance(this, value);
            return;
        }
        throw new InvalidSubdivisionException();
    }

    static class Cleaner implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        private static void onClaimDeleted(ClaimDeletedEvent e) {
            // Cleanup the database, keep the file from growing too large.
            new AreaGriefPrevention(e.getClaim()).remove();
        }
    }
}
