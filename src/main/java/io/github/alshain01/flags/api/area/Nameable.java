package io.github.alshain01.flags.api.area;

import io.github.alshain01.flags.api.exception.InvalidAreaException;

/**
 * Interface that defines if the area plugin allows areas to be given custom names.
 */
public interface Nameable extends Area {
    /**
     * Returns the name of the area defined in the area plugin.
     *
     * @return The name of the area
     * @throws InvalidAreaException
     */
    public String getName();
}
