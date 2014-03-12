package io.github.alshain01.flags.sector;

import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Sector implements ConfigurationSerializable, Comparable<Sector> {
    private final UUID id;
    private final UUID parent;
    private final SectorLocation greater, lesser;
    private int depth;


    protected Sector(Location corner1, Location corner2, int depth) {
        id = UUID.randomUUID();
        parent = null;
        this.depth = depth;

        //Find the lesser/greater corners
        greater = getGreaterCorner(corner1, corner2);
        lesser = getLesserCorner(corner1, corner2);
    }


    protected Sector(Location corner1, Location corner2, int depth, UUID parentID) {
        id = UUID.randomUUID();
        parent = parentID;
        this.depth = depth;

        //Find the lesser/greater corners
        greater = getGreaterCorner(corner1, corner2);
        lesser = getLesserCorner(corner1, corner2);
    }

    protected Sector(UUID id, Map<String, Object> sector) {
        this.id = id;
        greater = new SectorLocation((String)sector.get("GreaterCorner"));
        lesser = new SectorLocation((String)sector.get("LesserCorner"));
        parent = String.valueOf(sector.get("Parent")).equals("null") ? null : UUID.fromString((String)sector.get("Parent"));
        depth = (Integer)sector.get("Depth");
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> sector = new HashMap<String, Object>();
        sector.put("Parent", parent != null ? parent.toString() : "null");
        sector.put("GreaterCorner", greater.toString());
        sector.put("LesserCorner", lesser.toString());
        sector.put("Depth", depth);
        return sector;
    }

    /**
     * Returns a unique identifier for this sector
     *
     * @return The id of the sector
     */
    public UUID getID() {
        return id;
    }

    /**
     * Gets the corner where X and Z are greater.
     *
     * @return The location of the corner block
     */
    public Location getGreaterCorner() {
        return greater.getLocation();
    }

    /**
     * Gets the corner where X is greater and Z is lesser.
     *
     * @return The location of the corner block
     */
    public Location getGreaterXCorner() {
        return new SectorLocation(greater.getWorld(), greater.getX(), greater.getY(), lesser.getZ()).getLocation();
    }

    /**
     * Gets the corner where Z is greater and X is lesser.
     *
     * @return The location of the corner block
     */
    public Location getGreaterZCorner() {
        return new SectorLocation(greater.getWorld(), lesser.getX(), greater.getY(), greater.getZ()).getLocation();
    }

    /**
     * Gets the corner where X and Z are lesser.
     *
     * @return The location of the corner block
     */
    public Location getLesserCorner() {
        return lesser.getLocation();
    }

    /**
     * Gets the world the sector is located in
     *
     * @return The world the sector is in
     */
    public World getWorld() {
        return greater.getLocation().getWorld();
    }

    /**
     * Gets the depth of the sector
     *
     * @return The depth of the sector
     */
    @SuppressWarnings("unused") // API
    public int getDepth() { return depth; }

    /**
     * Sets the depth of the sector
     *
     * @param depth The new depth of the sector
     */
    @SuppressWarnings("unused") // API
    public void setDepth(int depth) { this.depth = depth; }

    /**
     * Gets the Unique ID of the Parent of this sector
     *
     * @return The ID of the parent sector, null if it is a parent sector
     */
    public UUID getParentID() {
        return parent;
    }

    /**
     * Gets whether the sector contains the provided point
     *
     * @param location The location to test conatainent
     * @return True if the sector contains the point
     */
    public boolean contains(Location location) {
        int x = location.getBlockX(), z = location.getBlockZ();

        // Greater will always have a higher X and Y due to constructor
        if((x >= lesser.getX() && x <= greater.getX()) && (z >= lesser.getZ() && z <= greater.getZ())) {
            // Check the depth below both points
            if(getWorld().getHighestBlockYAt(greater.getLocation()) - depth < location.getBlockY()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets whether the sector fully contains a provided cuboid.
     *
     * @param corner1 One corner of the cuboid
     * @param corner2 The diagonal opposite of corner1
     * @return True if the cuboid lies completely within this sector
     */
    public boolean contains(Location corner1, Location corner2) {
        return !(!corner1.getWorld().equals(this.getWorld()) || !corner2.getWorld().equals(this.getWorld()))
                && isLesser(getGreaterCorner(corner1, corner2), greater) && isGreater (getLesserCorner(corner1, corner2), lesser);
    }

    /**
     * Gets whether the sector overlaps the provided cuboid in any way.
     * Includes partial overlapping or fully contained.
     *
     * @param corner1 One corner of the cuboid
     * @param corner2 The diagonal opposite of corner1
     * @return True if the cuboid overlaps this sector
     */
    public boolean overlaps(Location corner1, Location corner2) {
        //Find the lesser/greater corners
        Sector testSector = new Sector(corner1, corner2, 25);

        return testSector.contains(getGreaterCorner()) || testSector.contains(getLesserCorner())
                || testSector.contains(getGreaterXCorner()) || testSector.contains(getGreaterZCorner())
                || contains(testSector.getGreaterCorner()) || contains(testSector.getLesserCorner())
                || contains(testSector.getGreaterXCorner()) || contains(testSector.getGreaterZCorner());
    }

    @Override
    public int compareTo(@Nonnull Sector s) {
        if(this.getID().equals(s.getID())) {
            return 0;
        } else if (this.getParentID() != null && this.getParentID().equals(s.getID())) {
            return -1;
        } else if (s.getParentID() != null && s.getParentID().equals(this.getID())) {
            return 1;
        } else if (this.getParentID() != null && s.getParentID() != null && s.getParentID().equals(this.getParentID())) {
            return 2;
        }
        return 3;
    }

    /*
     * Returns true if loc1 <= loc2 on both x & z points
     */
    private boolean isLesser(SectorLocation loc1, SectorLocation loc2) {
        return (loc1.getX() <= loc2.getX() && loc1.getZ() <= loc2.getZ());
    }

    /*
     * Returns true if loc1 >= loc2 on both x & z points
     */
    private boolean isGreater(SectorLocation loc1, SectorLocation loc2) {
        return (loc1.getX() >= loc2.getX() && loc1.getZ() >= loc2.getZ());
    }

    private int getLesserPoint(int x1, int x2) {
        return x1 < x2 ? x1 : x2;
    }

    private int getGreaterPoint(int x1, int x2) {
        return x1 > x2 ? x1 : x2;
    }

    private SectorLocation getLesserCorner(Location loc1, Location loc2) {
        UUID world = loc1.getWorld().getUID();
        int y = getLesserPoint(loc1.getBlockY(), loc2.getBlockY());

        int x = getLesserPoint(loc1.getBlockX(), loc2.getBlockX());
        int z = getLesserPoint(loc1.getBlockZ(), loc2.getBlockZ());

        return new SectorLocation(world, x, y, z);
    }

    private SectorLocation getGreaterCorner(Location loc1, Location loc2) {
        UUID world = loc1.getWorld().getUID();
        int y = getLesserPoint(loc1.getBlockY(), loc2.getBlockY());

        int x = getGreaterPoint(loc1.getBlockX(), loc2.getBlockX());
        int z = getGreaterPoint(loc1.getBlockZ(), loc2.getBlockZ());

        return new SectorLocation(world, x, y, z);
    }
}
