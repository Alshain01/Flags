package io.github.alshain01.flags.api.area;

import java.util.UUID;

/**
 * Interface that defines if the land system provides UUID for cuboids.
 */
public interface Identifiable extends Area {
    /**
     * Returns a unique id of the cuboid area,
     * if supported by the cuboid system.
     * Otherwise null.
     *
     * @return The UUID for the area or null.
     * @throws io.github.alshain01.flags.api.exception.InvalidAreaException
     */
    public UUID getUniqueId();
}
