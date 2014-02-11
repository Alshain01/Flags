package io.github.alshain01.flags.sector;

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
        greater = new SectorLocation(corner1.getBlockX() > corner2.getBlockX() ? corner1 : corner2);
        lesser = new SectorLocation(corner1.getBlockX() > corner2.getBlockX() ? corner2 : corner1);
        this.depth = depth;
    }

    protected Sector(Location corner1, Location corner2, int depth, UUID parentID) {
        id = UUID.randomUUID();
        parent = parentID;
        greater = new SectorLocation(corner1.getBlockX() > corner2.getBlockX() ? corner1 : corner2);
        lesser = new SectorLocation(corner1.getBlockX() > corner2.getBlockX() ? corner2 : corner1);
        this.depth = depth;
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

        // Greater will always have a higher X due to constructor
        if(x < greater.getX() && x > lesser.getX()) {

            // Check that the Z position is between the two points.
            if((greater.getZ() > lesser.getZ() && z < greater.getZ() && z > lesser.getZ())
                    || (greater.getZ() < lesser.getZ() && z > greater.getZ() && z < lesser.getZ())) {

                // Check the depth below both points
                if(getWorld().getHighestBlockYAt(greater.getLocation()) - depth < greater.getY()
                        && getWorld().getHighestBlockYAt(lesser.getLocation()) - depth < lesser.getY()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int compareTo(Sector s) {
        if(s != null) {
            return this.id == s.getID() ? 0 : 1;
        }
        return 1;
    }
}
