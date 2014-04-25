package io.github.alshain01.flags.api.sector;

import org.bukkit.Location;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.UUID;

@SuppressWarnings("unused")
public interface SectorManager {
    public void clear();

    /**
     * Adds a new top level sector
     *
     * @param corner1 The first corner
     * @param corner2 The diagonal opposite of corner 1
     * @return The new sector
     */
    public Sector add(@Nonnull Location corner1, @Nonnull Location corner2);

    /**
     * Adds a new subdivision sector
     *
     * @param corner1 The first corner
     * @param corner2 The diagonal opposite of corner 1
     * @param parent The UUID of the parent claim
     * @return The new sector
     */
    public Sector add(@Nonnull Location corner1, @Nonnull Location corner2, @Nonnull UUID parent);

    /**
     * Deletes a sector
     *
     * @param id The UUID of the sector to delete
     */
    public void delete(@Nonnull UUID id);

    /**
     * Deletes a sector by location
     *
     * @param location A location contained within the sector to delete
     * @return true if successfully removed.
     */
    public boolean delete(@Nonnull Location location);

    /**
     * Removes the top level csector and all subsectors
     *
     * @param location A location contained within the sector to delete
     * @return true if successfully removed
     */
    public boolean deleteTopLevel(@Nonnull Location location);

    /**
     * Gets sector by UUID
     *
     * @param uid The UUID of the sector to retrieve
     * @return the sector
     */
    public Sector get(@Nonnull UUID uid);

    /**
     * Gets a sector at a specified location
     *
     * @param location A location contained within the sector to retrieve
     * @return the sector
     */
    public Sector getAt(@Nonnull Location location);

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
    public boolean isOverlap(@Nonnull Location corner1, @Nonnull Location corner2);

    /**
     * Gets if a potential sector would be fully contained by an existing sector.
     *
     * @param corner1 The first corner
     * @param corner2 The diagonal opposite of corner 1
     * @return true if the potential sector would be fully contained by an existing sector.
     */
    public UUID isContained(@Nonnull Location corner1, @Nonnull Location corner2);
}

