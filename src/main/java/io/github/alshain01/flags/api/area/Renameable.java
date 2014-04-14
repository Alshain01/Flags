package io.github.alshain01.flags.api.area;

/**
 * Interface that defines if the land system allows cuboids names to be changed after creation.
 */
public interface Renameable extends Nameable {
    /**
     * Sets the name of the cuboid.
     *
     * @param name The name to set to the cuboid.
     */
    public void setName(String name);
}
