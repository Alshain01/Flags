package io.github.alshain01.flags.sector;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;

public class SectorLocation {
    public UUID world;
    public int coords[] = new int[3];

    protected SectorLocation(Location location) {
        coords[0] = location.getBlockX();
        coords[1] = location.getBlockY();
        coords[2] = location.getBlockZ();
        world = location.getWorld().getUID();
    }

    protected SectorLocation(String location) {
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
}