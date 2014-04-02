package io.github.alshain01.flags.sector;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;

@SuppressWarnings("WeakerAccess") // API
final public class SectorLocation {
    private final UUID world;
    private final int coords[] = new int[3];

    SectorLocation(UUID world, int x, int y, int z) {
        coords[0] = x;
        coords[1] = y;
        coords[2] = z;
        this.world = world;
    }

    public SectorLocation(String location) {
        String[] arg = location.split(",");

        world = UUID.fromString(arg[0]);
        for (int a = 0; a < 3; a++) {
            coords[a] = Integer.parseInt(arg[a+1]);
        }
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