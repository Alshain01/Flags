package io.github.alshain01.flags.sector;

import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Sector implements ConfigurationSerializable, Comparable<Sector> {
    private final UUID id;
    //private final UUID parent = null;
    private SectorLocation greater, lesser;


    protected Sector(Location corner1, Location corner2) {
        id = UUID.randomUUID();
        greater = new SectorLocation(corner1.getBlockX() > corner2.getBlockX() ? corner1 : corner2);
        lesser = new SectorLocation(corner1.getBlockX() > corner2.getBlockX() ? corner2 : corner1);
    }

    protected Sector(UUID id, Map<String, Object> sector) {
        this.id = id;
        greater = new SectorLocation((String)sector.get("greaterCorner"));
        lesser = new SectorLocation((String)sector.get("lesserCorner"));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> sector = new HashMap<String, Object>();
        sector.put("id", id);
        sector.put("greaterCorner", greater.toString());
        sector.put("lesserCorder", lesser.toString());
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

    public boolean contains(Location location) {
        int x = location.getBlockX(), z = location.getBlockZ();

        if(x < greater.getX() && x > lesser.getX()) {
            if((greater.getZ() > lesser.getZ() && z < greater.getZ() && z > lesser.getZ())
                    || (greater.getZ() < lesser.getZ() && z > greater.getZ() && z < lesser.getZ())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int compareTo(Sector s) {
        return this.id == s.getID() ? 0 : 1;
    }
}
