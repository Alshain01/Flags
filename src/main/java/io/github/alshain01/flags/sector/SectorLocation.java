package io.github.alshain01.flags.sector;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("WeakerAccess") // API
final public class SectorLocation implements ConfigurationSerializable {
    private final UUID world;
    private final int coords[] = new int[3];

    SectorLocation(UUID world, int x, int y, int z) {
        coords[0] = x;
        coords[1] = y;
        coords[2] = z;
        this.world = world;
    }

    SectorLocation(Map<String, Object> location) {
        this.world = UUID.fromString((String)location.get("World"));
        coords[0] = (Integer)location.get("X");
        coords[1] = (Integer)location.get("Y");
        coords[2] = (Integer)location.get("Z");
    }

    public static SectorLocation valueOf(Map<String, Object> location) {
        return new SectorLocation(location);
    }

    public static SectorLocation deserialize(Map<String, Object> location) {
        return new SectorLocation(location);
    }

    public SectorLocation(String location) {
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

    public Location getLocation() {
        return new Location(Bukkit.getWorld(world), coords[0], coords[1], coords[2]);
    }

    public int getX() {
        return coords[0];
    }

    public int getY() {
        return coords[1];
    }

    public int getZ() {
        return coords[2];
    }

    /**
     * Gets the unique ID of the world associated with this location
     *
     * @return The unique ID of the world
     */
    public UUID getWorld() {
        return world;
    }
}