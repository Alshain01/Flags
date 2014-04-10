/* Copyright 2013 Kevin Seiden. All rights reserved.

 This works is licensed under the Creative Commons Attribution-NonCommercial 3.0

 You are Free to:
    to Share: to copy, distribute and transmit the work
    to Remix: to adapt the work

 Under the following conditions:
    Attribution: You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
    Non-commercial: You may not use this work for commercial purposes.

 With the understanding that:
    Waiver: Any of the above conditions can be waived if you get permission from the copyright holder.
    Public Domain: Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
    Other Rights: In no way are any of the following rights affected by the license:
        Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
        The author's moral rights;
        Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.

 Notice: For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
 http://creativecommons.org/licenses/by-nc/3.0/
 */

package io.github.alshain01.flags;

import io.github.alshain01.flags.api.CuboidPlugin;
import io.github.alshain01.flags.api.Flag;
import io.github.alshain01.flags.api.FlagsAPI;
import io.github.alshain01.flags.api.area.Area;
//import io.github.alshain01.flags.api.area.Subdividable;
import io.github.alshain01.flags.api.area.Subdividable;
import io.github.alshain01.flags.api.economy.EconomyPurchaseType;

import java.io.File;
import java.io.IOException;
import java.util.*;

import io.github.alshain01.flags.api.sector.Sector;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * Class for managing YAML Database Storage
 */
final class DataStoreYaml extends DataStore {
    private final static String DATA_FILE = "data.yml";
    private final static String WILDERNESS_FILE = "wilderness.yml";
    private final static String DEFAULT_FILE = "default.yml";
    private final static String BUNDLE_FILE = "bundle.yml";
    private final static String PRICE_FILE = "price.yml";
    private final static String SECTOR_FILE = "sector.yml";
    private final static String CONFIG_FILE = "yamlConfig.yml";

    private final static String AUTO_SAVE_PATH = "AutoSaveInterval";
    private final static String DATABASE_VERSION_PATH = "Default.Database.Version";
    private final static String PLAYER_TRUST_PATH = "PlayerTrust";
    private final static String PERM_TRUST_PATH = "PermissionTrust";
    private final static String VALUE_PATH = "Value";
    private final static String MESSAGE_PATH = "Message";
    private final static String INHERIT_PATH = "InheritParent";
    private final static String DELIMETER = ".";

    private final File dataFolder;
    private final Plugin plugin;

    // Auto-Save manager
    private BukkitTask as;

	private YamlConfiguration data;
	private YamlConfiguration def;
	private YamlConfiguration wilderness;
	private YamlConfiguration bundle;
	private YamlConfiguration price;
    private YamlConfiguration sectors;

    private boolean saveData = false;
    private final CuboidPlugin cuboidPlugin;

    /*
     * Constructor
     */
    DataStoreYaml(Plugin plugin, CuboidPlugin cuboidPlugin, boolean enableAutoSave) {
        this.cuboidPlugin = cuboidPlugin;
        this.dataFolder = plugin.getDataFolder();
        this.plugin = plugin;
        updateWorldFile();
        reload(enableAutoSave);
    }

	DataStoreYaml(Plugin plugin, CuboidPlugin cuboidPlugin) {
        this.cuboidPlugin = cuboidPlugin;
        this.dataFolder = plugin.getDataFolder();
        this.plugin = plugin;
        updateWorldFile();
        reload();

	}

    private void updateWorldFile() {
        // Upgrade sequence from older versions
        File worldFile = new File(dataFolder, "world.yml");
        File wildFile = new File(dataFolder, WILDERNESS_FILE);
        if(worldFile.exists() && !wildFile.exists()) {
            if(!worldFile.renameTo(wildFile)) {
                Logger.error("Failed to rename world.yml to wilderness.yml");
            }
        }
    }

    private class AutoSave extends BukkitRunnable {
        @Override
        public void run() {
            save();
        }
    }

    /*
     * Interface Methods
     */
    @Override
    public void create(JavaPlugin plugin) {
        // Don't change the version here, not needed (will change in update)
        if (notExists(plugin)) {
            writeVersion(new DataStoreVersion(2, 0, 0));
        }
    }

    @Override
    public void reload() {
        reload(true);
    }

    void reload(boolean enableAutoSave) {
        File yamlConfigFile = new File(dataFolder, CONFIG_FILE);
        YamlConfiguration yamlConfig =YamlConfiguration.loadConfiguration(yamlConfigFile);
        if(!yamlConfig.isInt(AUTO_SAVE_PATH)) {
            yamlConfig.set(AUTO_SAVE_PATH, 60);
            try {
                yamlConfig.save(yamlConfigFile);
            } catch (IOException ex) {
                Logger.warning("Failed to write default Yaml Configuration file.");
            }
        }
        int saveInterval = yamlConfig.getInt(AUTO_SAVE_PATH);

        wilderness = YamlConfiguration.loadConfiguration(new File(dataFolder, WILDERNESS_FILE));
        def = YamlConfiguration.loadConfiguration(new File(dataFolder, DEFAULT_FILE));
        data = YamlConfiguration.loadConfiguration(new File(dataFolder, DATA_FILE));

        // Check to see if the file exists and if not, write the defaults
        File bundleFile = new File(dataFolder, BUNDLE_FILE);
        if (!bundleFile.exists()) {
            plugin.saveResource(BUNDLE_FILE, false);
        }
        bundle = YamlConfiguration.loadConfiguration(bundleFile);

        // Check to see if the file exists and if not, write the defaults
        File priceFile = new File(dataFolder, PRICE_FILE);
        if (!priceFile.exists()) {
            plugin.saveResource(PRICE_FILE, false);
        }
        price = YamlConfiguration.loadConfiguration(priceFile);

        if(cuboidPlugin == CuboidPlugin.FLAGS) {
            sectors = YamlConfiguration.loadConfiguration(new File(dataFolder, SECTOR_FILE));
        }

        // Remove old auto-saves
        if(as != null ) {
            as.cancel();
            as = null;
        }

        // Set up autosave
        if (saveInterval > 0 && enableAutoSave) {
            as = new AutoSave().runTaskTimer(plugin, saveInterval * 1200, saveInterval * 1200);
        }
    }

    void save() {
        if(!saveData) { return; }
        try {
            wilderness.save(new File(dataFolder, WILDERNESS_FILE));
            def.save(new File(dataFolder, DEFAULT_FILE));
            data.save(new File(dataFolder, DATA_FILE));
            bundle.save(new File(dataFolder, BUNDLE_FILE));
            price.save(new File(dataFolder, PRICE_FILE));
            if(sectors != null) {
                sectors.save(new File(dataFolder, SECTOR_FILE));
            }
            saveData = false;
        } catch (IOException ex) {
            Logger.error("Faled to write to data files. " + ex.getMessage());
        }
    }

    @Override
    public void close() {
        save();
    }

    @Override
    public DataStoreVersion readVersion() {
        final YamlConfiguration versionConfig = getYml(DATABASE_VERSION_PATH);
        if (!versionConfig.isSet(DATABASE_VERSION_PATH)) {
            return new DataStoreVersion(0, 0, 0);
        }

        final String[] ver = versionConfig.getString(DATABASE_VERSION_PATH).split("\\.");
        return new DataStoreVersion(Integer.valueOf(ver[0]), Integer.valueOf(ver[1]),
                Integer.valueOf(ver[2]));
    }

    @Override
    public DataStoreType getType() {
        return DataStoreType.YAML;
    }


    @Override
    public void update(JavaPlugin plugin) {
        final DataStoreVersion ver = readVersion();
        if (ver.getMajor() == 1 && ver.getMajor() <= 4 && ver.getBuild() < 1) {
            Logger.error("FAILED TO UPDATE DATABASE SCHEMA.  DATABASE VERSIONS PRIOR TO 1.4 CANNOT BE UPGRADED.");
            Bukkit.getPluginManager().disablePlugin(Bukkit.getServer().getPluginManager().getPlugin("Flags"));
            return;
        }
        if (ver.getMajor() < 2) {
            Logger.info("Beginning YAML Database Update");
            // Rename World to Wilderness
            Logger.info("Converting World to Wilderness");
            if (wilderness.isConfigurationSection("World")) {
                wilderness.set("Wilderness", wilderness.getConfigurationSection("World").getValues(true));
            }
            wilderness.set("World", null);

            ConfigurationSection[] dataconfigs = { data, def, wilderness };
            // Remove old trust lists for offline servers (there is no way to update them)
            if(!Bukkit.getServer().getOnlineMode()) {
                Logger.info("Removing Trust Lists for Offline Server");
                for(ConfigurationSection data : dataconfigs) {
                    for(String s : data.getKeys(true)) {
                        if(s.contains("Trust")) {
                            data.set(s, null);
                        }
                    }
                }
            }

            Logger.info("Scrubbing the Database");
            for(ConfigurationSection data : dataconfigs) {
                // Condition the data
                // Remove excess cuboid systems
                for (String s : data.getKeys(false)) {
                    if (!cuboidPlugin.getName().equals(s)) {
                        data.set(s, null);
                    }
                }

                //Remove invalid data and empty lists
                for (String s : data.getKeys(true)) {
                    if (((s.contains("Value") || s.contains("InheritParent")) && !data.isBoolean(s))
                            || (s.contains("Trust") && (!data.isList(s) || data.getList(s).isEmpty()))
                            || (s.contains("Message") && !data.isString(s))) {
                        data.set(s, null);
                    }
                }

                // Rmove empty configuration sections
                for (String s : data.getKeys(true)) {
                    if (data.isConfigurationSection(s) && data.getConfigurationSection(s).getKeys(false).isEmpty()) {
                        data.set(s, null);
                    }
                }
            }



            // Convert Bundles
            if (bundle.isConfigurationSection("Bundle")) {
                Logger.info("Scrubbing the Databse");
                for(String s : bundle.getConfigurationSection("Bundle").getKeys(false)) {
                    bundle.set(s, bundle.getList("Bundle." + s));
                }
                bundle.set("Bundle", null);
            }

            // Convert Prices
            if (price.isConfigurationSection("Price")) {
                Logger.info("Converting Prices");
                for(String s : price.getConfigurationSection("Price").getKeys(true)) {
                    price.set(s, price.getDouble("Price." + s));
                }
                price.set("Price", null);
            }

            // Convert Sectors
            if(sectors != null) {
                // Convert old sector location to serialized form.
                Logger.info("Serializing Sector Location Coordinates");
                for (String s : sectors.getConfigurationSection("Sectors").getKeys(true)) {
                        Logger.debug("Checking SectorLocation " + s);
                    if (s.contains("Corner")) {
                        Logger.debug("Converting SectorLocation " + s);
                        sectors.set("Sectors." + s, new SectorLocationBase(sectors.getString("Sectors." + s)).serialize());
                    }
                }

                // Remove old header
                if (sectors.isConfigurationSection("Sectors")) {
                    Logger.info("Converting Sectors");
                    for(String s : sectors.getConfigurationSection("Sectors").getKeys(false)) {
                        Logger.debug("Converting Sector " + s);
                        sectors.set(s, sectors.getConfigurationSection("Sectors." + s).getValues(true));
                    }
                    sectors.set("Sectors", null);
                    Logger.debug("Sectors Converted");
                }
            }

            // Convert Subdivision Data To the upper level.
            if(data.isConfigurationSection(cuboidPlugin.getName())) {
                Logger.info("Converting Subdivisions");
                Logger.debug("Converting for " + cuboidPlugin.getName());
                ConfigurationSection config = data.getConfigurationSection(cuboidPlugin.getName());
                for (World w : Bukkit.getWorlds()) {
                    if (config.isConfigurationSection(w.getName())) {
                        Logger.debug("Converting for " + w.getName());
                        ConfigurationSection wConfig = config.getConfigurationSection(w.getName());
                        for (String p : wConfig.getKeys(false)) { // Parent claims
                            Logger.debug("Converting for Parent " + p);
                            for (String s : wConfig.getConfigurationSection(p).getKeys(false)) { // Subdivision Claims
                                Logger.debug("Converting for Possible Subdivision " + s);
                                if (wConfig.isBoolean(p + DELIMETER + s + ".InheritParent")) { // If it's a subdivision, it will have an InheritParent key
                                    Logger.debug("Subdivision Found!");
                                    if (cuboidPlugin == CuboidPlugin.RESIDENCE) { // Residence uses the nested format by design, we will string replace it with a "_"
                                        wConfig.set(p + "_" + s, wConfig.getConfigurationSection(p + "." + s).getValues(true)); // Move the whole thing up one level
                                    } else {
                                        wConfig.set(s, wConfig.getConfigurationSection(p + DELIMETER + s).getValues(true)); // Move the whole thing up one level
                                    }
                                    wConfig.set(p + "." + s, null); // Erase it
                                }
                            }
                        }
                    }
                }
            }

            saveData = true;
            save();

            //Convert World Names to UUID
            Logger.info("Converting World Names to UUID.");
            if(data.isConfigurationSection(cuboidPlugin.getName())) {
                ConfigurationSection config = data.getConfigurationSection(cuboidPlugin.getName());
                for (String w : config.getKeys(false)) {
                    Logger.debug("Converting for World " + w);
                    ConfigurationSection wConfig = config.getConfigurationSection(w);
                    World world = Bukkit.getWorld(w);
                    if (world != null) {
                        for(String k : wConfig.getKeys(false)) {
                            Logger.debug("Converting for Key " + k);
                            ConfigurationSection kConfig = wConfig.getConfigurationSection(k);
                            config.set(world.getUID().toString() + DELIMETER + k, kConfig.getValues(true));
                        }
                    }
                    Logger.debug("Deleting old world data");
                    data.set(cuboidPlugin.getName() + DELIMETER + w, null);
                }
            }

            // Because the CuboidSystems sometimes need a world id,
            // AreaWilderness and AreaDefault will be converted to use world id and system id
            // In order to be universal.  This will result in the same key twice, nested.
            // But it's necessary to reuse code later in the class.
            if(wilderness.isConfigurationSection("Wilderness")) {
                for (String s : wilderness.getConfigurationSection("Wilderness").getKeys(false)) {
                    World world = Bukkit.getWorld(s);
                    if (world != null) {
                        wilderness.set("Wilderness." + world.getUID().toString() + DELIMETER + world.getUID().toString(),
                                wilderness.getConfigurationSection("Wilderness." + s).getValues(true));
                        wilderness.set("Wilderness." + s, null);
                    }
                }
            }

            if(def.isConfigurationSection("Default")) {
                for (String s : def.getConfigurationSection("Default").getKeys(false)) {
                    World world = Bukkit.getWorld(s);
                    if (world != null) {
                        def.set("Default." + world.getUID().toString() + DELIMETER + world.getUID().toString(),
                                def.getConfigurationSection("Default." + s).getValues(true));
                        def.set("Default." + s, null);
                    }
                }
            }

            // Convert Trust List to UUID
            Logger.info("Converting Player Names to UUID.");
            for(ConfigurationSection config : dataconfigs) {
                for (String k : config.getKeys(true)) {
                    if (k.contains("Trust") && config.isList(k)) {
                        List<?> trustList = config.getList(k);
                        List<String> permissionTrustList = new ArrayList<String>();
                        for (Object o : trustList) {
                            if (((String) o).contains(".")) {
                                permissionTrustList.add((String) o);
                            } else {
                                config.set(k.replace("Trust", "PlayerTrust." + Bukkit.getOfflinePlayer((String) o).getUniqueId()), o);
                            }
                        }
                        config.set(k.replace("Trust", "PermissionTrust"), permissionTrustList);
                        config.set(k, null);
                    }
                }
            }
            Logger.info("Writing Updated Database.");
            //writeVersion(new DataStoreVersion(2, 0, 0));
            //saveData = true;
            //save();
            Logger.info("Database Update Complete.");
        }
    }

    @Override
    public final Collection<String> readBundles() {
            return bundle.getKeys(false);
    }

    @Override
	public final Collection<Flag> readBundle(String bundleName) {
		final HashSet<Flag> flags = new HashSet<Flag>();
		final List<?> list = bundle.getList(bundleName, new ArrayList<String>());

		for (final Object o : list) {
			if (FlagsAPI.getRegistrar().isFlag((String) o)) {
				flags.add(FlagsAPI.getRegistrar().getFlag((String) o));
			}
		}
		return flags;
	}

    @Override
    public final void writeBundle(String name, Collection<Flag> flags) {
        if (flags == null || flags.size() == 0) {
            // Delete the bundle
            bundle.set(name, null);
            return;
        }

        final List<String> list = new ArrayList<String>();
        for (final Flag f : flags) {
            list.add(f.getName());
        }

        bundle.set(name, list);
        saveData = true;
    }

    @Override
    public double readPrice(Flag flag, EconomyPurchaseType type) {
        if(!price.isConfigurationSection(type.toString())) { return 0; }
        final ConfigurationSection priceConfig = price.getConfigurationSection(type.toString());
        return priceConfig.isSet(flag.getName()) ? priceConfig.getDouble(flag.getName()) : 0;
    }

    @Override
    public void writePrice(Flag flag, EconomyPurchaseType type, double newPrice) {
        final ConfigurationSection priceConfig = getCreatedSection(price, type.toString());
        priceConfig.set(flag.getName(), newPrice);
        saveData = true;
    }

    @Override
    public Map<UUID, Sector> readSectors() {
        Map<UUID, Sector> sectorMap = new HashMap<UUID, Sector>();

        for(String s : sectors.getKeys(false)) {
            UUID sID = UUID.fromString(s);
            sectorMap.put(sID, new SectorBase(sID, sectors.getConfigurationSection(s).getValues(false)));
        }

        return sectorMap;
    }

    @Override
    public void writeSector(Sector sector) {
        sectors.set(sector.getID().toString(), sector.serialize());
        saveData = true;
    }

    @Override
    public void deleteSector(UUID sID) {
        sectors.set(sID.toString(), null);
        saveData = true;
    }


    @Override
	public Boolean readFlag(Area area, Flag flag) {
        final String path = getAreaPath(area) + DELIMETER + flag.getName();
        if(getYml(path).isConfigurationSection(path)) {
            final ConfigurationSection flagConfig = getYml(path).getConfigurationSection(path);
            return flagConfig.isSet(VALUE_PATH) ? flagConfig.getBoolean(VALUE_PATH) : null;
        }
        return null;
	}

    @Override
    public void writeFlag(Area area, Flag flag, Boolean value) {
        final String path = getAreaPath(area) + DELIMETER + flag.getName();
        final ConfigurationSection flagConfig = getCreatedSection(getYml(path), path);
        flagConfig.set(VALUE_PATH, value);
        saveData = true;
    }

	@Override
	public String readMessage(Area area, Flag flag) {
        final String path = getAreaPath(area) + DELIMETER + flag.getName();
        if(getYml(path).isConfigurationSection(path)) {
            return getYml(path).getConfigurationSection(path).getString(MESSAGE_PATH);
        }
        return null;
	}

    @Override
    public void writeMessage(Area area, Flag flag, String message) {
        final String path = getAreaPath(area) + DELIMETER + flag.getName();
        final ConfigurationSection dataConfig = getCreatedSection(getYml(path), path);
        dataConfig.set(MESSAGE_PATH, message);
        saveData = true;
    }

    @Override
	public Map<UUID, String> readPlayerTrust(Area area, Flag flag) {
		final String path = getAreaPath(area) + DELIMETER + flag.getName() + DELIMETER + PLAYER_TRUST_PATH;
        final Map<UUID, String> playerData = new HashMap<UUID, String>();
        if(getYml(path).isConfigurationSection(path)) {
            for(String player : getYml(path).getConfigurationSection(path).getKeys(false)) {
                playerData.put(UUID.fromString(player), getYml(path).getString(path + DELIMETER + player));
            }
        }
		return playerData;
	}

    @Override
    public void writePlayerTrust(Area area, Flag flag, Map<UUID, String> players) {
        final String path = getAreaPath(area) + DELIMETER + flag.getName() + DELIMETER + PLAYER_TRUST_PATH;
        final ConfigurationSection trustConfig = getCreatedSection(getYml(path), path);

        // Remove players
        for(UUID player : readPlayerTrust(area, flag).keySet()) {
            if(!players.containsKey(player)) {
                trustConfig.set(player.toString(), null);
            }
        }

        // Add new players
        for(UUID player : players.keySet()) {
            trustConfig.set(player.toString(), players.get(player));
        }
        saveData = true;
    }

    @Override
    public Collection<Permission> readPermissionTrust(Area area, Flag flag) {
        final String path = getAreaPath(area) + DELIMETER + flag.getName() + DELIMETER + PERM_TRUST_PATH;
        final Set<Permission> permData = new HashSet<Permission>();
        if(getYml(path).isList(path)) {
            final List<?> setData = getYml(path).getList(path, new ArrayList<String>());

            for (final Object o : setData) {
                permData.add(new Permission((String) o));
            }
        }
        return permData;
    }

    @Override
    public void writePermissionTrust(Area area, Flag flag, Collection<Permission> permissions) {
        final String path = getAreaPath(area) + DELIMETER + flag.getName();
        ConfigurationSection permConfig = getCreatedSection(getYml(path), path);

        List<String> permList = new ArrayList<String>();
        for(Permission p : permissions) {
            permList.add(p.getName());
        }

        permConfig.set(PERM_TRUST_PATH, permList);
    }

    @Override
    public boolean readInheritance(Area area) {
        if (!(area instanceof Subdividable) || !((Subdividable)area).isSubdivision()) {
            return true;
        }

        String path = area.getCuboidPlugin().getName() + DELIMETER + area.getWorld().getUID().toString() + DELIMETER + area.getId().replace('.', '_');

        if(!getYml(path).isConfigurationSection(path)) { return true; }
        ConfigurationSection inheritConfig = getYml(path).getConfigurationSection(path);
        return !inheritConfig.isSet(INHERIT_PATH) || inheritConfig.getBoolean(INHERIT_PATH);
    }

    @Override
    public void writeInheritance(Area area, boolean value) {
        if ((area instanceof Subdividable) && ((Subdividable) area).isSubdivision()) {
            String path = area.getCuboidPlugin().getName() + DELIMETER + area.getWorld().getUID().toString() + DELIMETER + area.getId().replace('.', '_');

            ConfigurationSection inheritConfig = getCreatedSection(getYml(path), path);
            inheritConfig.set(path, value);
            saveData = true;
        }
    }

	@Override
	public void remove(Area area) {
        String path = getAreaPath(area);
        getYml(path).set(path, null);
	}

    @Override
    Set<String> getAllAreaIds(World world) {
        Set<String> areas = new HashSet<String>();
        Set<String> preformattedAreas = new HashSet<String>();
        if(data.isConfigurationSection(cuboidPlugin.getName() + DELIMETER + world.getUID().toString())) {
            preformattedAreas = data.getConfigurationSection(cuboidPlugin.getName() + DELIMETER + world.getUID().toString()).getKeys(false);
        }
        for(String s : preformattedAreas) {
            areas.add(s.replace("_", "."));
        }
        return areas;
    }

    /*
     * Private
     */
    private void writeVersion(DataStoreVersion version) {
        final YamlConfiguration cYml = getYml(DATABASE_VERSION_PATH);
        cYml.set(DATABASE_VERSION_PATH, version.getMajor() + "." + version.getMinor() + "." + version.getBuild());
    }

    private boolean notExists(JavaPlugin plugin) {
        final File fileObject = new File(plugin.getDataFolder(), "default.yml");
        return !fileObject.exists();
    }

    private String getAreaPath(Area area) {
        return area.getCuboidPlugin().getName() + "." + area.getWorld().getUID().toString() + "." + area.getId().replace('.', '_');
    }

    private ConfigurationSection getCreatedSection(YamlConfiguration config, String path) {
        if(config.isConfigurationSection(path)) {
            return config.getConfigurationSection(path);
        } else {
            return config.createSection(path);
        }
    }

    private YamlConfiguration getYml(String path) {
        final String[] pathList = path.split("\\.");

        if (pathList[0].equalsIgnoreCase("wilderness")) {
            return wilderness;
        } else if (pathList[0].equalsIgnoreCase("default")) {
            return def;
        } else {
            return data;
        }
    }
}
