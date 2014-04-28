package io.github.alshain01.flags.api.sector;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

/**
 * Provides a "load safe" location for sector coordinates.
 */
public interface SectorLocation extends ConfigurationSerializable {
    /**
     * Gets the Bukkit Location for this SectorLocation
     *
     * @return the Location
     */
    public Location getLocation();

    /**
     * Gets the X Block coordinate for this SectorLocation
     *
     * @return the X coordinate
     */
    public int getX();

    /**
     * Gets the Y Block coordinate for this SectorLocation
     *
     * @return the Y coordinate
     */
    public int getY();

    /**
     * Gets the Z Block coordinate for this SectorLocation
     *
     * @return the Z coordinate
     */
    public int getZ();

    /**
     * Gets the unique ID of the world associated with this location
     *
     * @return The unique ID of the world
     */
    public World getWorld();
}
