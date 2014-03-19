package io.github.alshain01.flags.area;

import org.bukkit.Location;

public class Wilderness extends World {
    /**
     * Creates an instance of World based on a Bukkit Location
     *
     * @param location
     *            The Bukkit location
     */
    public Wilderness(Location location) {
        super(location.getWorld());
    }

    /**
     * Creates an instance of World based on a Bukkit World
     *
     * @param world
     *            The Bukkit world
     */
    public Wilderness(org.bukkit.World world) {
        super(world);
    }

    /**
     * Creates an instance of World based on a Bukkit World name
     *
     * @param worldName
     *            The Bukkit world
     */
    public Wilderness(String worldName) {
        super(worldName);
    }
}
