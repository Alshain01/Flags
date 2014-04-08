package io.github.alshain01.flags.api.area;

/**
 * Interface that defines if the land system allows cuboids to be given custom names.
 */
public interface Nameable extends Area {
    /**
     * Returns the name of the cuboid defined in the system.
     * If the system does not support naming, the ID will be returned.
     *
     * @return The LandSystem that created this object
     * @throws io.github.alshain01.flags.api.exception.InvalidAreaException
     */
    public String getName();
}
