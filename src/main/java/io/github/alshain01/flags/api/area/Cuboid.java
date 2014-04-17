package io.github.alshain01.flags.api.area;

import org.bukkit.Location;

/**
 * Interface that defines if the cuboid plugin uses "true"
 * cubical cuboids and it's corners can be identified.
 */
public interface Cuboid extends Area{
    /**
     * Returns the location of the cuboid where x and z are greatest.
     *
     * @return the greater corner location
     */
    public Location getGreaterCorner();

    /**
     * Returns the location of the cuboid where x and z are least.
     *
     * @return the lesser corner location
     */
    public Location getLesserCorner();
}
