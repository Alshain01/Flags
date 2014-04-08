package io.github.alshain01.flags;

import io.github.alshain01.flags.api.sector.SectorLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Provides a "load safe" location for sector coordinates.
 */
final class SectorLocationBase implements SectorLocation {
    private final UUID world;
    private final int coords[] = new int[3];

    SectorLocationBase(UUID world, int x, int y, int z) {
        coords[0] = x;
        coords[1] = y;
        coords[2] = z;
        this.world = world;
    }

    private SectorLocationBase(Map<String, Object> location) {
        this.world = UUID.fromString((String)location.get("World"));
        coords[0] = (Integer)location.get("X");
        coords[1] = (Integer)location.get("Y");
        coords[2] = (Integer)location.get("Z");
    }

    public static SectorLocation valueOf(Map<String, Object> location) {
        return new SectorLocationBase(location);
    }

    public static SectorLocation deserialize(Map<String, Object> location) {
        return new SectorLocationBase(location);
    }

    public SectorLocationBase(String location) {
        String[] arg = location.split(",");

        world = Bukkit.getWorld(arg[0]).getUID();
        for (int a = 0; a < 3; a++) {
            coords[a] = Integer.parseInt(arg[a+1]);
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> location = new HashMap<String, Object>();
        location.put("World", world.toString());
        location.put("X", coords[0]);
        location.put("Y", coords[1]);
        location.put("Z", coords[2]);
        return location;
    }

    @Override
    public String toString() {
        return world + "," + coords[0] + "," + coords[1] + "," + coords[2];
    }

    @Override
    public Location getLocation() {
        return new Location(Bukkit.getWorld(world), coords[0], coords[1], coords[2]);
    }

    @Override
    public int getX() {
        return coords[0];
    }

    @Override
    public int getY() {
        return coords[1];
    }

    @Override
    public int getZ() {
        return coords[2];
    }

    @Override
    public UUID getWorld() {
        return world;
    }
}