package io.github.alshain01.flags.api;

import io.github.alshain01.flags.AreaFactory;
import io.github.alshain01.flags.DataStore;
import io.github.alshain01.flags.api.area.Area;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;

/**
 * Primary class for hooking into the API.
 */
final public class FlagsAPI {
    private static Registrar registrar = new Registrar();
    private static CuboidPlugin activeSystem;
    private static DataStore dataStore;

    private FlagsAPI() { }

    static DataStore getDataStore() {
        return dataStore;
    }

    /**
     * Starts the API.
     * Cannot be used externally.
     *
     * @param cuboidSystem The cuboid system detected
     * @param data The datastore to be used
     */
    public static void initialize(CuboidPlugin cuboidSystem, DataStore data) {
        Validate.notNull(data); // Prevents plugins from using this method.
        activeSystem = cuboidSystem;
        dataStore = data;
        CuboidPlugin.loadNames();

        ConfigurationSerialization.registerClass(Flag.class);
        Bundle.registerPermissions();
    }

    /**
     * Closes the API.
     * Cannot be used externally.
     *
     * @param data The data store to close
     */
    public static void close(DataStore data) {
        Validate.notNull(data); // Prevents plugins from using this method.
        registrar = null;
        activeSystem = null;
        dataStore = null;
    }

    /**
     * Gets the registrar for this instance of Flags.
     *
     * @return The flag registrar.
     */
    public static Registrar getRegistrar() {
        return registrar;
    }

    /**
     * Gets if the configured cuboid system has an area at a specific location.
     *
     * @param location
     *            The location to request an area.
     * @return True if there is an area present at the location.
     */
    public static boolean hasArea(Location location) {
        return AreaFactory.hasArea(activeSystem, location);
    }

    /**
     * Gets an area from at a specific location.
     *
     * @param location
     *            The location for which to request an area.
     * @return An area from the configured cuboid system or the wilderness area if no area is defined.
     */
    public static Area getAreaAt(Location location) {
        Validate.notNull(location);
        Area area = AreaFactory.getAreaAt(activeSystem, location);
        return area.isArea() ? area : getWildernessArea(location.getWorld());
    }

    /**
     * Gets the wilderness area for the world.
     *
     * @param world
     *            The world for which to request the wilderness area.
     * @return The area for setting wilderness settings in the world.
     */
    public static Area getWildernessArea(World world) {
        return AreaFactory.getWildernessArea(world);
    }

    /**
     * Gets the default area for the world.
     *
     * @param world
     *            The world for which to request the default area.
     * @return The area for setting default settings for areas in the world.
     */
    public static Area getDefaultArea(World world) {
        return AreaFactory.getDefaultArea(world);
    }

    /**
     * Checks if a player is in Pvp combat that is being monitored by the cuboid system
     *
     * @param player
     *            The player to request information for
     * @return True if the player is in pvp combat, false is not or if cuboid system is
     *         unsupported.
     */
    public static boolean inPvpCombat(Player player) {
        Validate.notNull(player);
        return activeSystem == CuboidPlugin.GRIEF_PREVENTION
                && GriefPrevention.instance.dataStore.getPlayerData(player.getName()).inPvpCombat();
    }

    /**
     * Gets the currently active cuboid system.
     *
     * @return The Cuboid System in use by Flags.
     */
    public static CuboidPlugin getCuboidPlugin() {
        return activeSystem;
    }
}
