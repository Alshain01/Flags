package io.github.alshain01.flags.api;

import io.github.alshain01.flags.*;
import io.github.alshain01.flags.api.area.Area;
import io.github.alshain01.flags.api.area.Subdividable;
import io.github.alshain01.flags.api.economy.EconomyTransactionType;
import io.github.alshain01.flags.api.sector.SectorManager;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Primary class for hooking into the API.
 */
@SuppressWarnings("unused")
final public class FlagsAPI {
    private static Registrar registrar = new Registrar();
    private static CachedArea areaCache = new CachedArea();
    private static AreaPlugin activeSystem;
    private static DataStore dataStore;
    private static SectorManager sectorManager;

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
    public static void initialize(@Nonnull Plugin plugin, @Nonnull AreaPlugin cuboidSystem, @Nullable SectorManager sectors, @Nonnull DataStore data) {
        Validate.notNull(data); // Prevents plugins from using this method since DataStore implementations are private.
        activeSystem = cuboidSystem;
        dataStore = data;
        sectorManager = sectors;
        AreaPlugin.loadNames();
        EconomyTransactionType.loadLocalized();

        ConfigurationSerialization.registerClass(Flag.class);
        new onServerEnabledTask().runTask(plugin);
    }

    /*
     * Tasks that must be run only after the entire sever has loaded.
     * Runs on first server tick.
     */
    private static class onServerEnabledTask extends BukkitRunnable {
        @Override
        public void run() {
            // Needs to run after server starts to prevent
            // plugin.yml from attempting to register flags.bundle twice.
            registerPermissions();
        }
    }

    /**
     * Closes the API.
     * Cannot be used externally.
     *
     * @param data The data store to close
     */
    public static void close(@Nonnull DataStore data) {
        Validate.notNull(data); // Prevents plugins from using this method since DataStore implementations are private.
        registrar = null;
        activeSystem = null;
        dataStore = null;
        sectorManager = null;
        areaCache = null;
    }

    /**
     * Gets the currently active cuboid system.
     *
     * @return The Cuboid System in use by Flags.
     */
    public static AreaPlugin getAreaPlugin() {
        return activeSystem;
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
     * Gets the sector manager for this instance of Flags.
     * Null if Flags is not configured to use sectors.
     *
     * @return The sector manager for Flags
     */
    public static SectorManager getSectorManager() {
        return sectorManager;
    }

    /**
     * Gets the default area for the world.
     *
     * @param world
     *            The world for which to request the default area.
     * @return The area for setting default settings for areas in the world.
     */
    public static Area getDefaultArea(@Nonnull World world) {
        return areaCache.getDefault(world);
    }

    /**
     * Gets the wilderness area for the world.
     *
     * @param world
     *            The world for which to request the wilderness area.
     * @return The area for setting wilderness settings in the world.
     */
    public static Area getWildernessArea(@Nonnull World world) {
        return areaCache.getWilderness(world);
    }

    /**
     * Gets if the configured cuboid system has an area at a specific location.
     *
     * @param location
     *            The location to request an area.
     * @return True if there is an area present at the location.
     */
    public static boolean hasArea(@Nonnull Location location) {
        return FactoryArea.hasArea(activeSystem, location);
    }

    /**
     * Gets an area by cuboid system specific ID. The name is formatted based on the
     * cuboid system. Warning, there is little data type validation in this method.
     * Incorrect usage may result in exceptions.
     *
     * ID Data Format:
     * Factiod, Flags, Grief Prevention, Residence = UUID as String
     * GriefPrevention = ClaimID (Long)
     * PreciousStones = WorldUUID.FieldID (Long)
     * Regios = Region name
     * InfinitePlots, PlotMe = WorldUUID.PlotID (X;Z)
     * WorldGuard = WorldName.RegionName
     *
     * @param id
     *            The cuboid system specific name of the area or world name
     * @return The Area requested, may be null in cases of invalid cuboid system
     *         selection.
     */
    public static Area getArea(@Nonnull String id) {
        return FactoryArea.getArea(activeSystem, id);
    }

    /**
     * Gets an area from at a specific location.
     * If an area is an inheriting subdivision, the parent is returned.
     *
     * @param location
     *            The location for which to request an area.
     * @return An area from the configured cuboid system or the wilderness area if no area is defined.
     */
    public static Area getAreaAt(@Nonnull Location location) {
        if(!FactoryArea.hasArea(activeSystem, location)) return getWildernessArea(location.getWorld());
        Area area = FactoryArea.getAreaAt(activeSystem, location);
        if(area instanceof Subdividable) {
            Subdividable sub = (Subdividable) area;
            while (sub.isInherited())
                sub.transformParent();
            return sub;
        }
        return area;
    }

    /**
     * Gets an area from at a specific location, ignoring inheritance.
     *
     * @param location
     *            The location for which to request an area.
     * @return An area from the configured cuboid system or the wilderness area if no area is defined.
     */
    public static Area getAbsoluteAreaAt(@Nonnull Location location) {
        if(!FactoryArea.hasArea(activeSystem, location)) return getWildernessArea(location.getWorld());
        return FactoryArea.getAreaAt(activeSystem, location);
    }

    /**
     * Gets a set of bundle names created on the server.
     *
     * @return A set of bundles names configured on the server.
     */
    public static Collection<String> getBundleNames() {
        return getDataStore().readBundles();
    }

    /**
     * Gets the total number of bundles defined on the server
     *
     * @return A count of the bundles on the server
     */
    public static int bundleCount() {
        return getBundleNames().size();
    }

    /**
     * Checks if a bundle name exists in the data store.
     *
     * @param bundle
     *            A string bundle name.
     * @return True if the string is a valid bundle name.
     */
    public static boolean isBundle(@Nonnull String bundle) {
        return getBundleNames().contains(bundle.toLowerCase());
    }

    /**
     * Gets a bundle from the data store.
     *
     * @param bundle
     *            The bundle name to retrieve
     * @return A list containing the bundle. Null if it doesn't exist.
     * @throws IllegalArgumentException
     */
    public static Collection<Flag> getBundle(@Nonnull String bundle) {
        if(!isBundle(bundle)) { throw new IllegalArgumentException("The provided bundle name does not exist."); }
        return getDataStore().readBundle(bundle.toLowerCase());
    }

    /**
     * Sets a bundle to the data file.
     *
     * @param name
     *            The bundle name
     * @param flags
     *            A list of flags in the bundle. May be null to remove the bundle but if not null, may not contain null elements.
     * @throws IllegalArgumentException
     */
    public static void setBundle(@Nonnull String name, @Nullable Collection<Flag> flags) {
        if(flags != null) {
            // The main variable may be null to remove but not the elements
            Validate.noNullElements(flags);
        }

        if(name.length() > 36) { name = name.substring(0, 35); }

        getDataStore().writeBundle(name, flags);
        String permName = "flags.bundle." + name.toLowerCase();
        if(flags == null || flags.size() == 0) {
            if(Bukkit.getPluginManager().getPermission(permName) != null) {
                Bukkit.getPluginManager().removePermission(permName);
            }
        } else {
            if(Bukkit.getPluginManager().getPermission(permName) == null) {
                addPermission(name);
            }
        }
    }

    private static void addPermission(@Nonnull String name) {
        Logger.debug("Registering Bundle Permissions: " + name);
        final Permission perm = new Permission("flags.bundle." + name.toLowerCase(),
                "Grants ability to use the bundle " + name, PermissionDefault.FALSE);
        perm.addParent("flags.bundle", true);
        Bukkit.getPluginManager().addPermission(perm);
    }

    // Used only on plugin enable
    private static void registerPermissions() {
        for(String b : getDataStore().readBundles()) {
            if (Bukkit.getPluginManager().getPermission(b) == null) {
                addPermission(b);
            }
        }
    }

    /**
     * Checks if a player is in Pvp combat that is being monitored by the cuboid system
     *
     * @param player
     *            The player to request information for
     * @return True if the player is in pvp combat, false is not or if cuboid system is
     *         unsupported.
     */
    public static boolean inPvpCombat(@Nonnull Player player) {
        return activeSystem == AreaPlugin.GRIEF_PREVENTION
                && GriefPrevention.instance.dataStore.getPlayerData(player.getName()).inPvpCombat();
    }

    /**
     * Utility method that checks if the provided string represents
     * a version number that is equal to or lower than the current Bukkit API version.
     *
     * String should be formatted with 3 numbers: x.y.z
     *
     * @return true if the version provided is compatible
     */
    public static boolean checkAPI(@Nonnull String version) {
        try {
            final String bukkitVersion = Bukkit.getServer().getBukkitVersion();
            final float apiVersion = Float.valueOf(bukkitVersion.substring(0, 3));
            final float CompareVersion = Float.valueOf(version.substring(0, 3));
            final int apiBuild = Integer.valueOf(bukkitVersion.substring(4, 5));
            final int CompareBuild = Integer.valueOf(version.substring(4, 5));

            return (apiVersion > CompareVersion
                    || apiVersion == CompareVersion	&& apiBuild >= CompareBuild);
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     * Gets a player by name from Flags' list of cached player names.
     * May not be up to date.
     *
     * @param name the name of the player to retrieve
     * @return the cached player
     */
    public OfflinePlayer getCachedOfflinePlayer(String name) {
        return CachedOfflinePlayer.getOfflinePlayer(name);
    }
}
