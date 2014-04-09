package io.github.alshain01.flags.api.sector;

import io.github.alshain01.flags.api.event.SectorDeleteEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Collection;
import java.util.UUID;


public interface SectorManager {
    public void clear();

    /**
     * Adds a new top level sector
     *
     * @param corner1 The first corner
     * @param corner2 The diagonal opposite of corner 1
     * @return The new sector
     */
    public Sector add(Location corner1, Location corner2);

    /**
     * Adds a new subdivision sector
     *
     * @param corner1 The first corner
     * @param corner2 The diagonal opposite of corner 1
     * @param parent The UUID of the parent claim
     * @return The new sector
     */
    public Sector add(Location corner1, Location corner2, UUID parent);

    /**
     * Deletes a sector
     *
     * @param id The UUID of the sector to delete
     */
    public void delete(UUID id);

    /**
     * Deletes a sector by location
     *
     * @param location A location contained within the sector to delete
     * @return true if successfully removed.
     */
    public boolean delete(Location location);

    /**
     * Removes the top level csector and all subsectors
     *
     * @param location A location contained within the sector to delete
     * @return true if successfully removed
     */
    public boolean deleteTopLevel(Location location);

    /**
     * Gets sector by UUID
     *
     * @param uid The UUID of the sector to retrieve
     * @return the sector
     */
    public Sector get(UUID uid);

    /**
     * Gets a sector at a specified location
     *
     * @param location A location contained within the sector to retrieve
     * @return the sector
     */
    public Sector getAt(Location location);

    /**
     * Gets a collection of all sectors.
     *
     * @return A collection of all sectors.
     */
    public Collection<Sector> getAll();

    /**
     * Gets if a potential sector would overlap an existing sector.
     *
     * @param corner1 The first corner
     * @param corner2 The diagonal opposite of corner 1
     * @return true if the potential sector would overlap an existing sector
     */
    public boolean isOverlap(Location corner1, Location corner2);

    /**
     * Gets if a potential sector would be fully contained by an existing sector.
     *
     * @param corner1 The first corner
     * @param corner2 The diagonal opposite of corner 1
     * @return true if the potential sector would be fully contained by an existing sector.
     */
    public UUID isContained(Location corner1, Location corner2);
}

