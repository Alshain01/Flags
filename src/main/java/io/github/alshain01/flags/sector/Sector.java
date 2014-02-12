package io.github.alshain01.flags.sector;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Sector implements ConfigurationSerializable, Comparable<Sector> {
    private final UUID id;
    private final UUID parent;
    private SectorLocation greater, lesser;
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
        parent = sector.get("Parent") == null ? null : UUID.fromString((String)sector.get("parent"));
        depth = (Integer)sector.get("Depth");
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> sector = new HashMap<String, Object>();
        sector.put("Parent", parent.toString());
        sector.put("GreaterCorner", greater.toString());
        sector.put("LesserCorner", lesser.toString());
        sector.put("Depth", depth);
        return sector;
    }

    public UUID getID() {
        return id;
    }

    public Location getGreaterBoundaryCorner() {
        return greater.getLocation();
    }

    public Location getLesserBoundaryCorner() {
        return lesser.getLocation();
    }

    public World getWorld() {
        return greater.getLocation().getWorld();
    }

    public int getDepth() { return depth; }

    public void setDepth(int depth) { this.depth = depth; }

    public UUID getParentID() {
        return parent;
    }

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

    public boolean contains(Location corner1, Location corner2) {
        if(!corner1.getWorld().equals(this.getWorld()) || !corner2.getWorld().equals(this.getWorld())) { return false; }

        //Find the lesser/greater corners
        SectorLocation g = getGreaterCorner(corner1, corner2);
        SectorLocation l = getLesserCorner(corner1, corner2);
        return isLesser(g, greater) && isGreater (l, lesser);
    }

    public boolean overlaps(Location corner1, Location corner2) {
        //TODO
    }

    @Override
    public int compareTo(Sector s) {
        if(s == null) { return 3; }

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
