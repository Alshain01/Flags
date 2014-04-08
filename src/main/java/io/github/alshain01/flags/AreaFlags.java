package io.github.alshain01.flags;

import io.github.alshain01.flags.api.CuboidPlugin;
import io.github.alshain01.flags.api.area.Area;
import io.github.alshain01.flags.api.area.Identifiable;
import io.github.alshain01.flags.api.area.Subdividable;
import io.github.alshain01.flags.api.exception.InvalidAreaException;
import io.github.alshain01.flags.api.exception.InvalidSubdivisionException;
import io.github.alshain01.flags.sector.Sector;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

final class AreaFlags extends AreaRemovable implements Identifiable, Subdividable {
    private final Sector sector;

    /**
     * Creates an instance of AreaFlags based on a Bukkit Location
     *
     * @param location
     *            The Bukkit location
     */
    public AreaFlags(Location location) {
        sector = Flags.getSectorManager().getAt(location);
    }

    /**
     * Creates an instance of AreaFlags based on a sector ID
     *
     * @param id
     *            The sector ID
     */
    public AreaFlags(UUID id) {
        sector = Flags.getSectorManager().get(id);
    }

    /**
     * Gets if there is a sector at the location.
     *
     * @return True if a claim exists at the location.
     */
    public static boolean hasSector(Location location) {
        return Flags.getSectorManager().getAt(location) != null;
    }

    @Override
    public CuboidPlugin getCuboidPlugin() {
        return CuboidPlugin.FLAGS;
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
    public World getWorld() {
        if (isArea()) return sector.getWorld();
        throw new InvalidAreaException();
    }

    @Override
    public boolean isArea() {
        return sector != null;
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
