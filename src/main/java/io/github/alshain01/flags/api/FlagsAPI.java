package io.github.alshain01.flags.api;

import io.github.alshain01.flags.Flags;
import io.github.alshain01.flags.api.area.Area;
import io.github.alshain01.flags.area.AreaDefault;
import io.github.alshain01.flags.area.AreaWilderness;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class FlagsAPI {
    static Registrar registrar = new Registrar();

    /**
     * Gets the registrar for this instance of Flags.
     *
     * @return The flag registrar.
     */
    public static Registrar getRegistrar() {
        return registrar;
    }

    /*
     * Gets if the configured cuboid system has an area at a specific location.
     *
     * @param location
     *            The location to request an area.
     * @return True if there is an area present at the location.
     */
    public static boolean hasArea(Location location) {
        return CuboidType.getActive().hasArea(location);
    }

    /*
     * Gets an area from at a specific location.
     *
     * @param location
     *            The location to request an area.
     * @return An area from the configured cuboid system or the wilderness area if no area is defined.
     */
    public static Area getAreaAt(Location location) {
        Validate.notNull(location);
        Area area = CuboidType.getActive().getCuboidAt(location);
        return area.isArea() ? area : getWildernessArea(location.getWorld());
    }

    /*
     * Gets the wilderness area for the world.
     *
     * @param location
     *            The location to request an area.
     * @return The area for setting wilderness settings in the world.
     */
    public static Area getWildernessArea(World world) {
        return new AreaWilderness(world);
    }

    /*
     * Gets the default area for the world.
     *
     * @param location
     *            The location to request an area.
     * @return The area for setting default settings for areas in the world.
     */
    public static Area getDefaultArea(World world) {
        return new AreaDefault(world);
    }

    /**
     * Checks if a player is in Pvp combat that is being monitored by the cuboid system
     *
     * @param player
     *            The player to request information for
     * @return True if the player is in pvp combat, false is not or if cuboid system is
     *         unsupported.
     */
    public boolean inPvpCombat(Player player) {
        Validate.notNull(player);
        return CuboidType.getActive() == CuboidType.GRIEF_PREVENTION
                && GriefPrevention.instance.dataStore.getPlayerData(player.getName()).inPvpCombat();
    }
}
