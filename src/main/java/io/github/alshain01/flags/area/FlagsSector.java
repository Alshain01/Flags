package io.github.alshain01.flags.area;

import io.github.alshain01.flags.CuboidType;
import io.github.alshain01.flags.Flags;
import io.github.alshain01.flags.System;
import io.github.alshain01.flags.exception.InvalidAreaException;
import io.github.alshain01.flags.exception.InvalidSubdivisionException;
import io.github.alshain01.flags.sector.Sector;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.World;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

final public class FlagsSector extends Area implements Subdivision, Removable {
    private final Sector sector;

    /**
     * Creates an instance of FlagsSector based on a Bukkit Location
     *
     * @param location
     *            The Bukkit location
     */
    public FlagsSector(Location location) {
        sector = Flags.getSectorManager().getAt(location);
    }

    /**
     * Creates an instance of FlagsSector based on a sector ID
     *
     * @param id
     *            The claim ID
     */
    public FlagsSector(UUID id) {
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

    /**
     * Gets the sector object embedded in the area class.
     *
     * @return The claim object
     */
    @SuppressWarnings("WeakerAccess") // API
    public Sector getSector() { return sector; }

    @Override
    public UUID getUniqueId() {
        if (!isArea()) { throw new InvalidAreaException(); }
        return sector.getID();
    }

    @Override
    public String getSystemID() {
        if(!isArea()) { throw new InvalidAreaException(); }
        if(sector.getParentID() != null) {
            return String.valueOf(sector.getParentID());
        } else {
            return String.valueOf(sector.getID());
        }
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public System getSystemType() { return System.FLAGS; }

    @Override
    public CuboidType getCuboidType() { return CuboidType.FLAGS; }

    @Override
    public String getName() {
        if (!isArea()) { throw new InvalidAreaException(); }
        return sector.getName();
    }

    @Override
    public Set<String> getOwnerNames() {
        if(!isArea()) { throw new InvalidAreaException(); }
        return new HashSet<String>(Arrays.asList("Administrator"));
    }

    @Override
    public World getWorld() {
        if(!isArea()) { throw new InvalidAreaException(); }
        return sector.getWorld();
    }

    @Override
    public boolean isArea() { return sector != null; }

    @Override
    public void remove() {
        if (!isArea()) { throw new InvalidAreaException(); }
        Flags.getDataStore().remove(this);
    }

    @Override
    public String getSystemSubID() {
        if (!isArea()) { throw new InvalidAreaException(); }
        if (!isSubdivision()) { throw new InvalidSubdivisionException(); }
        return String.valueOf(sector.getID());
    }

    @Override
    public boolean isSubdivision() {
        if (!isArea()) { throw new InvalidAreaException(); }
        return sector.getParentID() != null;
    }

    @Override
    public boolean isParent(Area area) {
        Validate.notNull(area);
        if (!area.isArea()) { throw new InvalidAreaException(); }
        if (!isSubdivision()) { throw new InvalidSubdivisionException(); }

        return area instanceof FlagsSector
                && getSystemSubID().equals(String.valueOf(area.getSystemID()));
    }

    @Override
    public Area getParent() {
        if (!isArea()) { throw new InvalidAreaException(); }
        if (!isSubdivision()) { throw new InvalidSubdivisionException(); }
        return sector.getParentID() == null ? null : new FlagsSector(sector.getParentID());
    }

    @Override
    public boolean isInherited() {
        if (!isArea()) { throw new InvalidAreaException(); }
        if (!isSubdivision()) { throw new InvalidSubdivisionException(); }
        return Flags.getDataStore().readInheritance(this);
    }

    @Override
    public void setInherited(boolean value) {
        if(!isArea()) { throw new InvalidAreaException(); }
        if (!isSubdivision()) { throw new InvalidSubdivisionException(); }
        Flags.getDataStore().writeInheritance(this, value);
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
        if (!(a instanceof FlagsSector)) { return 3; }
        Sector testSector = ((FlagsSector)a).getSector();
        return sector.compareTo(testSector);
    }
}
