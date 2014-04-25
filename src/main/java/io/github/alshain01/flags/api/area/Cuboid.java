package io.github.alshain01.flags.api.area;

import org.bukkit.Location;
import io.github.alshain01.flags.api.exception.InvalidAreaException;

/**
 * Defines if the area plugin uses cuboids (x, y, and z) or squares (x and z only) and it's corners can be identified.
 */
@SuppressWarnings("unused")
public interface Cuboid extends Area{
    /**
     * Returns the location of the cuboid where x, y, and z are greatest.
     *
     * @return the greater corner location
     * @throws InvalidAreaException
     */
    public Location getGreaterCorner();

    /**
     * Returns the location of the cuboid where x, y, and z are least.
     *
     * @return the lesser corner location
     * @throws InvalidAreaException
     */
    public Location getLesserCorner();

    /**
     * For fixed-height systems, this get the greater corner with y adjusted for height.
     * For true cuboids this returns the same as getGreaterCorner.
     *
     * @return the greater corner location
     * @throws InvalidAreaException
     */
    public Location getAdjustedGreaterCorner();

    /**
     * For fixed-depth systems, this get the lesser corner with y adjusted for depth.
     * For true cuboids this returns the same as getLesserCorner.
     *
     * @return the lesser corner location
     * @throws InvalidAreaException
     */
    public Location getAdjustedLesserCorner();
}
