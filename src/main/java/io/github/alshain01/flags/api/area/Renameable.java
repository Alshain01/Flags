package io.github.alshain01.flags.api.area;

import javax.annotation.Nonnull;
import io.github.alshain01.flags.api.exception.InvalidAreaException;

/**
 * Defines if the land system allows cuboids names to be changed after creation.
 */
public interface Renameable extends Nameable {
    /**
     * Sets the name of the cuboid.
     *
     * @param name The name to set to the cuboid.
     * @throws InvalidAreaException
     */
    public void setName(@Nonnull String name);
}
