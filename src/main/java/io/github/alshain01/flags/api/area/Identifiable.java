package io.github.alshain01.flags.api.area;

import java.util.UUID;
import io.github.alshain01.flags.api.exception.InvalidAreaException;

/**
 * Defines if the area plugin provides UUID for areas.
 */
public interface Identifiable extends Area {
    /**
     * Returns a unique id of the area, if supported by the cuboid system.
     *
     * @return the UUID for the area
     * @throws InvalidAreaException
     */
    public UUID getUniqueId();
}
