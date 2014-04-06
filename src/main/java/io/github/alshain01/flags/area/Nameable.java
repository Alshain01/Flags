package io.github.alshain01.flags.area;

public interface Nameable {
    /**
     * Returns the name of the cuboid defined in the system.
     * If the system does not support naming, the ID will be returned.
     *
     * @return The LandSystem that created this object
     * @throws io.github.alshain01.flags.exception.InvalidAreaException
     */
    public String getName();
}
