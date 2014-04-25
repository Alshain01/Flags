package io.github.alshain01.flags.api.sector;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Defines a Sector cuboid.
 */
@SuppressWarnings("unused")
public interface Sector extends ConfigurationSerializable, Comparable<Sector> {
    /**
     * Gets a unique identifier for this sector
     *
     * @return the id of the sector
     */
    public UUID getID();

    /**
     * Gets the name of this sector.
     *
     * @return the name of the sector.
     */
    public String getName();

    /**
     * Sets the name of the sector.
     *
     * @param name the name of the sector.
     */
    public void setName(@Nullable String name);

    /**
     * Gets the corner where X and Z are greater.
     *
     * @return the location of the corner block
     */
    public SectorLocation getGreaterCorner();

    /**
     * Gets the corner where X is greater and Z is lesser.
     *
     * @return the location of the corner block
     */
    public SectorLocation getGreaterXCorner();

    /**
     * Gets the corner where Z is greater and X is lesser.
     *
     * @return the location of the corner block
     */
    public SectorLocation getGreaterZCorner();

    /**
     * Gets the corner where X and Z are lesser.
     *
     * @return The location of the corner block
     */
    public SectorLocation getLesserCorner();

    /**
     * Gets the world the sector is located in
     *
     * @return the world the sector is in
     */
    public World getWorld();

    /**
     * Gets the depth of the sector
     *
     * @return the depth of the sector
     */
    public int getDepth();

    /**
     * Sets the depth of the sector
     *
     * @param depth The new depth of the sector
     */
    public void setDepth(int depth);

    /**
     * Gets the parent of this sector
     *
     * @return the parent sector, null if it is a parent sector
     */
    public Sector getParent() ;

    /**
     * Gets if the sector contains the provided point
     *
     * @param location The location to test containment
     * @return true if the sector contains the point
     */
    public boolean contains(@Nonnull Location location);

    /**
     * Gets if the sector fully contains a provided cuboid.
     *
     * @param corner1 One corner of the cuboid
     * @param corner2 The diagonal opposite of corner1
     * @return true if the cuboid lies completely within this sector
     * @see #overlaps(org.bukkit.Location, org.bukkit.Location)
     */
    public boolean contains(@Nonnull Location corner1, @Nonnull Location corner2);

    /**
     * Gets if the sector overlaps the provided cuboid in any way.
     * Includes partial overlapping or fully contained.
     *
     * @param corner1 One corner of the cuboid
     * @param corner2 The diagonal opposite of corner1
     * @return true if the cuboid overlaps this sector
     * @see #contains(org.bukkit.Location, org.bukkit.Location)
     */
    public boolean overlaps(@Nonnull Location corner1, @Nonnull Location corner2);
}
