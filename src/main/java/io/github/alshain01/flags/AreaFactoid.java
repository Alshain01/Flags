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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import io.github.alshain01.flags.api.AreaPlugin;
import io.github.alshain01.flags.api.area.*;
import io.github.alshain01.flags.api.exception.InvalidAreaException;
import io.github.alshain01.flags.api.exception.InvalidSubdivisionException;

import me.tabinol.factoid.Factoid;
import me.tabinol.factoid.event.LandDeleteEvent;
import me.tabinol.factoid.exceptions.FactoidLandException;
import me.tabinol.factoid.lands.Land;
import me.tabinol.factoid.playercontainer.PlayerContainerPlayer;
import me.tabinol.factoid.playercontainer.PlayerContainerType;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import javax.annotation.Nonnull;

/**
 * Class for creating areas to manage a Factoid Land.
 */
final class AreaFactoid extends AreaRemovable implements Identifiable, Renameable, Ownable, Subdividable {
    private Land land;

    /**
     * Creates an instance of AreaFactoid based on a Bukkit
     * Location
     *
     * @param location
     *            The Bukkit location
     */
    public AreaFactoid(Location location) {
        land = Factoid.getLands().getLand(location);
    }

    /**
     * Creates an instance of AreaFactoid based on a Land
     * name
     *
     * @param id
     *            The land UUID
     */
    public AreaFactoid(UUID id) {
        land = Factoid.getLands().getLand(id);
    }

    /**
     * Creates an instance of AreaFactoid based on a Land Object
     *
     * @param land
     *            The Land Object
     */
    private AreaFactoid(Land land) {
        this.land = land;
    }

    /**
     * Gets if there is a land at the location.
     *
     * @return True if a land exists at the location.
     */
    public static boolean hasLand(Location location) {
        return Factoid.getLands().getLand(location) != null;
    }

    @Override
    public AreaPlugin getAreaPlugin() {
        return AreaPlugin.FACTOID;
    }

    @Override
    public UUID getUniqueId() {
        if (isArea()) return land.getUUID();
        throw new InvalidAreaException();
    }

    @Override
    public String getId() {
        if (isArea()) return land.getUUID().toString();
        throw new InvalidAreaException();
    }

    @Override
    public String getName() {
        if (isArea()) return land.getName();
        throw new InvalidAreaException();
    }

    @Override
    public void setName(@Nonnull String name) {
        try {
            Factoid.getLands().renameLand(land.getName(), name);
        } catch (FactoidLandException ex) {
            Logger.warning("Invalid Factiod Land Rename Action Detected.");
        }
    }

    @Override
    public Collection<OfflinePlayer> getOwners() {
        if (isArea()) {
            Set<OfflinePlayer> owners = new HashSet<OfflinePlayer>();
            if (land.getOwner().getContainerType() == PlayerContainerType.PLAYER) {
                owners.add(Bukkit.getOfflinePlayer(((PlayerContainerPlayer) land.getOwner()).getMinecraftUUID()));
            }
            return owners;
        }
        throw new InvalidAreaException();
    }

    @Override
    public org.bukkit.World getWorld() {
        if (isArea()) return land.getWorld();
        throw new InvalidAreaException();
    }

    @Override
    public boolean isArea() {
        return land != null;
    }

    @Override
    public boolean isSubdivision() {
        return isArea() && land.getParent() != null;
    }

    @Override
    public boolean isParent(@Nonnull Area area) {
        return isSubdivision() && area instanceof AreaFactoid && land.getParent().getUUID().equals(((Identifiable)area).getUniqueId());
    }

    @Override
    public Area getParent() {
        if (isSubdivision()) return new AreaFactoid(land.getParent());
        throw new InvalidSubdivisionException();
    }

    @Override
    public void transformParent() {
        if (isSubdivision()) {
            this.land = land.getParent();
            return;
        }
        throw new InvalidSubdivisionException();
    }

    @Override
    public boolean isInherited() {
        return isSubdivision() && Flags.getDataStore().readInheritance(this);
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
        private static void onRegionDelete(LandDeleteEvent e) {
            // Cleanup the database, keep the file from growing too large.
            new AreaFactoid(e.getLand()).remove();
        }
    }
}
