package io.github.alshain01.flags.api.sector;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.UUID;

/**
 * Defines a Sector cuboid.
 */
@SuppressWarnings("unused")
public interface Sector extends ConfigurationSerializable, Comparable<Sector> {
    /**
     * Returns a unique identifier for this sector
     *
     * @return The id of the sector
     */
    public UUID getID();

    /**
     * Gets the corner where X and Z are greater.
     *
     * @return The location of the corner block
     */
    public SectorLocation getGreaterCorner();

    /**
     * Gets the corner where X is greater and Z is lesser.
     *
     * @return The location of the corner block
     */
    public SectorLocation getGreaterXCorner();

    /**
     * Gets the corner where Z is greater and X is lesser.
     *
     * @return The location of the corner block
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
     * @return The world the sector is in
     */
    public World getWorld();

    /**
     * Gets the depth of the sector
     *
     * @return The depth of the sector
     */
    public int getDepth();

    /**
     * Sets the depth of the sector
     *
     * @param depth The new depth of the sector
     */
    public void setDepth(int depth);

    /**
     * Gets the Unique ID of the Parent of this sector
     *
     * @return The ID of the parent sector, null if it is a parent sector
     */
    public UUID getParentID() ;

    /**
     * Gets whether the sector contains the provided point
     *
     * @param location The location to test conatainent
     * @return True if the sector contains the point
     */
    public boolean contains(Location location);

    /**
     * Gets whether the sector fully contains a provided cuboid.
     *
     * @param corner1 One corner of the cuboid
     * @param corner2 The diagonal opposite of corner1
     * @return True if the cuboid lies completely within this sector
     */
    public boolean contains(Location corner1, Location corner2);

    /**
     * Gets whether the sector overlaps the provided cuboid in any way.
     * Includes partial overlapping or fully contained.
     *
     * @param corner1 One corner of the cuboid
     * @param corner2 The diagonal opposite of corner1
     * @return True if the cuboid overlaps this sector
     */
    public boolean overlaps(Location corner1, Location corner2);
}
