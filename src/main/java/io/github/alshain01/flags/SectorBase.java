package io.github.alshain01.flags;

import io.github.alshain01.flags.api.sector.Sector;
import io.github.alshain01.flags.api.sector.SectorLocation;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Defines a Sector cuboid.
 */
final class SectorBase implements Sector {
    private final UUID id;
    private final UUID parent;
    private String name;
    private final SectorLocation greater, lesser;
    private int depth;

    SectorBase(UUID id, Location corner1, Location corner2, int depth) {
        this(id, corner1, corner2, depth, null);
    }

    private SectorBase(Location corner1, Location corner2, int depth) {
        this(UUID.randomUUID(), corner1, corner2, depth);
    }

    SectorBase(UUID id, Location corner1, Location corner2, int depth, UUID parentID) {
        this.id = id;
        parent = parentID;
        this.depth = depth;

        //Find the lesser/greater corners
        greater = getGreaterCorner(corner1, corner2);
        lesser = getLesserCorner(corner1, corner2);
    }

    public SectorBase(UUID id, Map<String, Object> sector) {
        this.id = id;
        greater = (SectorLocation)sector.get("GreaterCorner");
        lesser = new SectorLocationBase(((ConfigurationSection)sector.get("LesserCorner")).getValues(false));
        parent = String.valueOf(sector.get("Parent")).equals("null") ? null : UUID.fromString((String)sector.get("Parent"));
        depth = (Integer)sector.get("Depth");
        name = (String)sector.get("Name");
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> sector = new HashMap<String, Object>();
        sector.put("Parent", parent != null ? parent.toString() : "null");
        sector.put("Name", name);
        sector.put("GreaterCorner", greater.serialize());
        sector.put("LesserCorner", lesser.serialize());
        sector.put("Depth", depth);
        return sector;
    }

    /**
     * Returns a unique identifier for this sector
     *
     * @return The id of the sector
     */
    @Override
    public UUID getID() {
        return id;
    }

    @Override
    public String getName() {
        return name != null ? name : "unnamed sector";
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the corner where X and Z are greater.
     *
     * @return The location of the corner block
     */
    @Override
    public SectorLocation getGreaterCorner() {
        return greater;
    }

    /**
     * Gets the corner where X is greater and Z is lesser.
     *
     * @return The location of the corner block
     */
    @Override
    public SectorLocation getGreaterXCorner() {
        return new SectorLocationBase(greater.getWorld(), greater.getX(), greater.getY(), lesser.getZ());
    }

    /**
     * Gets the corner where Z is greater and X is lesser.
     *
     * @return The location of the corner block
     */
    @Override
    public SectorLocation getGreaterZCorner() {
        return new SectorLocationBase(greater.getWorld(), lesser.getX(), greater.getY(), greater.getZ());
    }

    /**
     * Gets the corner where X and Z are lesser.
     *
     * @return The location of the corner block
     */
    public SectorLocation getLesserCorner() {
        return lesser;
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
    public int getDepth() { return depth; }

    /**
     * Sets the depth of the sector
     *
     * @param depth The new depth of the sector
     */
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
        Sector testSector = new SectorBase(corner1, corner2, 25);

        return testSector.contains(getGreaterCorner().getLocation()) || testSector.contains(getLesserCorner().getLocation())
                || testSector.contains(getGreaterXCorner().getLocation()) || testSector.contains(getGreaterZCorner().getLocation())
                || contains(testSector.getGreaterCorner().getLocation()) || contains(testSector.getLesserCorner().getLocation())
                || contains(testSector.getGreaterXCorner().getLocation()) || contains(testSector.getGreaterZCorner().getLocation());
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

        return new SectorLocationBase(world, x, y, z);
    }

    private SectorLocation getGreaterCorner(Location loc1, Location loc2) {
        UUID world = loc1.getWorld().getUID();
        int y = getLesserPoint(loc1.getBlockY(), loc2.getBlockY());

        int x = getGreaterPoint(loc1.getBlockX(), loc2.getBlockX());
        int z = getGreaterPoint(loc1.getBlockZ(), loc2.getBlockZ());

        return new SectorLocationBase(world, x, y, z);
    }
}
