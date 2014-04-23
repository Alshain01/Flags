package io.github.alshain01.flags;

import io.github.alshain01.flags.api.AreaPlugin;
import io.github.alshain01.flags.api.FlagsAPI;
import io.github.alshain01.flags.api.area.*;
import io.github.alshain01.flags.api.event.SectorDeleteEvent;
import io.github.alshain01.flags.api.exception.InvalidAreaException;
import io.github.alshain01.flags.api.exception.InvalidSubdivisionException;

import io.github.alshain01.flags.api.sector.Sector;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;

final class AreaFlags extends AreaRemovable implements Identifiable, Cuboid, Renameable, Subdividable {
    private Sector sector;

    /**
     * Creates an instance of AreaFlags based on a Bukkit Location
     *
     * @param location
     *            The Bukkit location
     */
    public AreaFlags(Location location) {
        sector = FlagsAPI.getSectorManager().getAt(location);
    }

    /**
     * Creates an instance of AreaFlags based on a sector ID
     *
     * @param id
     *            The sector ID
     */
    public AreaFlags(UUID id) {
        sector = FlagsAPI.getSectorManager().get(id);
    }

    /**
     * Creates an instance of AreaFlags using an exiting sector
     *
     * @param sector
     *            The sector to create the area for
     */
    public AreaFlags(Sector sector) {
        this.sector = sector;
    }

    /**
     * Gets if there is a sector at the location.
     *
     * @return True if a claim exists at the location.
     */
    public static boolean hasSector(Location location) {
        return FlagsAPI.getSectorManager().getAt(location) != null;
    }

    @Override
    public AreaPlugin getCuboidPlugin() {
        return AreaPlugin.FLAGS;
    }

    @Override
    public UUID getUniqueId() {
        if (isArea()) return sector.getID();
        throw new InvalidAreaException();
    }

    @Override
    public String getId() {
        if (isArea()) return sector.getID().toString();
        throw new InvalidAreaException();
    }

    @Override
    public String getName() {
        if (isArea()) return sector.getName();
        throw new InvalidAreaException();
    }

    @Override
    public void setName(String name) {
        if(isArea())
            sector.setName(name);
        else
            throw new InvalidAreaException();
    }

    @Override
    public World getWorld() {
        if (isArea()) return sector.getWorld();
        throw new InvalidAreaException();
    }

    @Override
    public boolean isArea() {
        return sector != null;
    }

    @Override
    public Location getGreaterCorner() {
        if (isArea()) return sector.getGreaterCorner().getLocation();
        throw new InvalidAreaException();
    }

    @Override
    public Location getLesserCorner() {
        if (isArea()) return sector.getLesserCorner().getLocation();
        throw new InvalidAreaException();
    }

    @Override
    public Location getAdjustedGreaterCorner() {
        if (isArea()) {
            Location corner = sector.getGreaterCorner().getLocation();
            corner.setY(256F);  // Maximum height
            return corner;
        }
        throw new InvalidAreaException();
    }

    @Override
    public Location getAdjustedLesserCorner() {
        if (isArea()) {
            Location corner = sector.getLesserCorner().getLocation();
            corner.setY(sector.getGreaterCorner().getX() - sector.getDepth());
            return corner;
        }
        throw new InvalidAreaException();
    }

    @Override
    public boolean isSubdivision() {
        if (isArea()) return sector.getParentID() != null;
        throw new InvalidAreaException();
    }

    @Override
    public boolean isParent(Area area) {
        Validate.notNull(area);
        if (isSubdivision()) return area instanceof AreaFlags && getParent().getId().equals(String.valueOf(area.getId()));
        throw new InvalidSubdivisionException();
    }

    @Override
    public Area getParent() {
        if (isSubdivision()) return new AreaFlags(sector.getParentID());
        throw new InvalidSubdivisionException();
    }

    @Override
    public void transformParent() {
        if (isSubdivision()) {
            this.sector = FlagsAPI.getSectorManager().get(sector.getParentID());
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

    static class Cleaner implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        private static void onSectorDelete(SectorDeleteEvent e) {
            new AreaFlags(e.getSector()).remove();
        }
    }
}
