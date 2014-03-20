package io.github.alshain01.flags.area;

import org.bukkit.Bukkit;
import org.bukkit.Location;

@SuppressWarnings("deprecation")
final public class Wilderness extends World {
    /**
     * Creates an instance of Wilderness based on a Bukkit Location
     *
     * @param location
     *            The Bukkit location
     */
    public Wilderness(Location location) {
        this(location.getWorld());
    }

    /**
     * Creates an instance of Wilderness based on a Bukkit World
     *
     * @param world
     *            The Bukkit world
     */
    public Wilderness(org.bukkit.World world) {
        super(world);
    }

    /**
     * Creates an instance of Wilderness based on a Bukkit World name
     *
     * @param worldName
     *            The Bukkit world
     */
    public Wilderness(String worldName) {
        this(Bukkit.getWorld(worldName));
    }
}
