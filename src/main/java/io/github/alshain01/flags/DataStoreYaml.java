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
import io.github.alshain01.flags.api.area.Subdividable;
import io.github.alshain01.flags.api.economy.EconomyPurchaseType;

import java.io.File;
import java.io.IOException;
import java.util.*;

import io.github.alshain01.flags.api.sector.Sector;

import net.t00thpick1.residence.api.ResidenceAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
    private final static String CONFIG_FILE = "dataConfig.yml";

    private final static String AUTO_SAVE_PATH = "YAML.AutoSaveInterval";
    private final static String DATABASE_VERSION_PATH = "YAML.DatabaseVersion";
    private final static String PLAYER_TRUST_PATH = "FlagPlayerTrust";
    private final static String PERM_TRUST_PATH = "FlagPermissionTrust";
    private final static String VALUE_PATH = "FlagState";
    private final static String MESSAGE_PATH = "FlagMessage";
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
        File yamlConfigFile = new File(dataFolder, CONFIG_FILE);
        YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(yamlConfigFile);


        if (!yamlConfig.isConfigurationSection(DATABASE_VERSION_PATH)) {
            return new DataStoreVersion(0, 0, 0);
        }
        ConfigurationSection versionConfig = yamlConfig.getConfigurationSection(DATABASE_VERSION_PATH);
        return new DataStoreVersion(versionConfig.getInt("Major"), versionConfig.getInt("Minor"),
                versionConfig.getInt("Build"));
    }

    private DataStoreVersion readOldVersion() {
        final YamlConfiguration versionConfig = getYml("Default.Database.Version");
        if (!versionConfig.isSet("Default.Database.Version")) {
            return new DataStoreVersion(0, 0, 0);
        }

        final String[] ver = versionConfig.getString("Default.Database.Version").split("\\.");
        return new DataStoreVersion(Integer.valueOf(ver[0]), Integer.valueOf(ver[1]),
                Integer.valueOf(ver[2]));
    }

    @Override
    public DataStoreType getType() {
        return DataStoreType.YAML;
    }


    @Override
    public boolean update(JavaPlugin plugin) {
        DataStoreVersion ver = readOldVersion();
        if (ver.getMajor() == 1 && ver.getMinor() < 4) {
            Logger.error("FAILED TO UPDATE DATABASE SCHEMA. DATABASE VERSIONS PRIOR TO 1.4 CANNOT BE UPGRADED.");
            Bukkit.getPluginManager().disablePlugin(Bukkit.getServer().getPluginManager().getPlugin("Flags"));
            return false;
        }

        ver = readVersion();
        if (ver.getMajor() < 2) {
            def.set("Default.Database.Version", null);
            Logger.info("Beginning YAML Database Update");
            updateWorld2Wilderness();
            updateMigrateBundles();
            updateMigratePrices();

           // Convert Sectors
            if(sectors != null) {
                // Convert old sector location to serialized form.
                updateSerializeSectorLocations();
                updateMigrateSectors();
            }

            ConfigurationSection[] dataconfigs = { data, def, wilderness };

            // Remove old trust lists for offline servers (there is no way to update them)
            if(!Bukkit.getServer().getOnlineMode()) {
                updateRemoveOfflineTrust(dataconfigs);
            }

            updateScrubDatabase(dataconfigs);
            updateConvertWorlds(dataconfigs);

            // Convert Subdivision Data To the upper level.
            if(data.isConfigurationSection(cuboidPlugin.getName())) {
                updateMigrateSubdivisions();
            }

            // Convert Trust List to UUID
            updateMigratePermissions(dataconfigs);
            if(!updateConvertPlayers(dataconfigs)) {
                Bukkit.getPluginManager().disablePlugin(Bukkit.getServer().getPluginManager().getPlugin("Flags"));
                return false;
            }

            updateScrubDatabase(dataconfigs);
            updateScrubDatabase(dataconfigs); // Double pass to make sure we catch them all

            updateConvertFlags(dataconfigs);

            Logger.info("Writing Updated Database.");
            writeVersion(new DataStoreVersion(2, 0, 0));
            saveData = true;
            save();
            reload();
            Logger.info("Database Update Complete.");
        }
        return true;
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
            String key = s + DELIMETER;
            Logger.debug("Read Sectors: " + key);
            Map<String, Object> sector = new HashMap<String, Object>();
            UUID sID = UUID.fromString(s);

            sector.put("Depth", sectors.getInt(key + "Depth"));
            sector.put("Parent", sectors.getString(key + "Parent"));
            sector.put("Name", sectors.getString(key + "Name"));
            sector.put("GreaterCorner", sectors.getConfigurationSection(key + "GreaterCorner").getValues(false));
            sector.put("LesserCorner", sectors.getConfigurationSection(key + "LesserCorner").getValues(false));
            sectorMap.put(sID, new SectorBase(sID, sector));
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
        ConfigurationSection flagConfig = getCreatedSection(getYml(path), path);
        flagConfig.set(VALUE_PATH, value);

        if(value == null) {
            cleanConfigurationSection(flagConfig);
        }
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

        if(message == null) {
            cleanConfigurationSection(dataConfig);
        }
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

        if(players.isEmpty()) {
            cleanConfigurationSection(trustConfig);
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
        if(permList.isEmpty()) { permList = null; }

        permConfig.set(PERM_TRUST_PATH, permList);

        if(permList == null) {
            cleanConfigurationSection(permConfig);
        }
        saveData = true;
    }

    @Override
    public boolean readInheritance(Subdividable area) {
        if (!area.isSubdivision()) {
            return false;
        }

        String path = area.getCuboidPlugin().getName() + DELIMETER + area.getWorld().getUID().toString() + DELIMETER + area.getId();

        if(!getYml(path).isConfigurationSection(path)) { return true; }
        ConfigurationSection inheritConfig = getYml(path).getConfigurationSection(path);
        return !inheritConfig.isSet(INHERIT_PATH) || inheritConfig.getBoolean(INHERIT_PATH);
    }

    @Override
    public void writeInheritance(Subdividable area, boolean value) {
        if (area.isSubdivision()) {
            String path = area.getCuboidPlugin().getName() + DELIMETER + area.getWorld().getUID().toString() + DELIMETER + area.getId();

            ConfigurationSection inheritConfig = getCreatedSection(getYml(path), path);
            inheritConfig.set(path, value);
            saveData = true;
        }
    }

	@Override
	public void remove(AreaRemovable area) {
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
            areas.add(s);
        }
        return areas;
    }

    /*
     * Private
     */
    private void cleanConfigurationSection(ConfigurationSection config) {
        boolean finished = false;
        while(config.getParent() != null && !finished) {
            ConfigurationSection parent = config.getParent();
            if(config.getKeys(true).isEmpty()) {
                parent.set(config.getCurrentPath(), null);
                config = parent;
            } else {
                finished = true;
            }
        }
    }

    private void writeVersion(DataStoreVersion version) {
        File yamlConfigFile = new File(dataFolder, CONFIG_FILE);
        YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(yamlConfigFile);
        yamlConfig.set(DATABASE_VERSION_PATH + DELIMETER + "Major", version.getMajor());
        yamlConfig.set(DATABASE_VERSION_PATH + DELIMETER + "Minor", version.getMinor());
        yamlConfig.set(DATABASE_VERSION_PATH + DELIMETER + "Build", version.getBuild());
        try {
            yamlConfig.save(yamlConfigFile);
        } catch (IOException ex) {
            Logger.error("Failed to write new DataStore version.");
        }
    }

    private boolean notExists(JavaPlugin plugin) {
        final File fileObject = new File(plugin.getDataFolder(), "default.yml");
        return !fileObject.exists();
    }

    private String getAreaPath(Area area) {
        return area.getCuboidPlugin().getName() + "." + area.getWorld().getUID().toString() + "." + area.getId();
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

    private void updateWorldFile() {
        // Upgrade sequence from older versions
        File worldFile = new File(dataFolder, "world.yml");
        File wildFile = new File(dataFolder, WILDERNESS_FILE);
        if(worldFile.exists() && !wildFile.exists()) {
            Logger.info("Converting world.yml to wilderness.yml");
            if(!worldFile.renameTo(wildFile)) {
                Logger.error("Failed to rename world.yml to wilderness.yml");
            }
        }
    }

    private void updateWorld2Wilderness() {
        // Rename World to Wilderness
        Logger.info("Converting World to Wilderness");
        if (wilderness.isConfigurationSection("World")) {
            wilderness.set("Wilderness", wilderness.getConfigurationSection("World").getValues(true));
        }
        wilderness.set("World", null);
    }

    private void updateRemoveOfflineTrust(ConfigurationSection[] dataconfigs) {
        Logger.info("Removing Trust Lists for Offline Server");
        for(ConfigurationSection data : dataconfigs) {
            for(String s : data.getKeys(true)) {
                if(s.contains("Trust")) {
                    data.set(s, null);
                }
            }
        }
    }

    private void updateScrubDatabase(ConfigurationSection[] dataconfigs) {
        Logger.info("Scrubbing the Database");
        for(ConfigurationSection data : dataconfigs) {
            // Condition the data
            // Remove excess cuboid systems
            for (String s : data.getKeys(false)) {
                if (!cuboidPlugin.getName().equals(s)  && !"Wilderness".equals(s) && !"Default".equals(s)) {
                    data.set(s, null);
                }
            }

            //Remove invalid data and empty lists
            for (String s : data.getKeys(true)) {
                if (((s.contains("Value") || s.contains("InheritParent")) && !data.isBoolean(s))
                        || ((s.contains("Trust") || s.contains("FlagPlayerTrust") || s.contains("FlagPermissionTrust")) && (!data.isList(s) || data.getList(s).isEmpty()))
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
    }

    private void updateMigrateBundles(){
        // Convert Bundles
        if (bundle.isConfigurationSection("Bundle")) {
            Logger.info("Migrating Bundles");
            for(String s : bundle.getConfigurationSection("Bundle").getKeys(false)) {
                bundle.set(s, bundle.getList("Bundle." + s));
            }
            bundle.set("Bundle", null);
        }
    }

    private void updateMigratePrices() {
        // Convert Prices
        if (price.isConfigurationSection("Price")) {
            Logger.info("Migrating Prices");
            for(String s : price.getConfigurationSection("Price").getKeys(true)) {
                if(s.contains("Flag") || s.contains("Message")) {
                    price.set(s, price.getDouble("Price." + s));
                }
            }
            price.set("Price", null);
        }
    }

    private void updateMigrateSectors() {
        // Remove old header
        if (sectors.isConfigurationSection("Sectors")) {
            Logger.info("Migrating Sectors");
            for(String s : sectors.getConfigurationSection("Sectors").getKeys(true)) {
                sectors.set(s, sectors.get("Sectors." + s));
            }
            sectors.set("Sectors", null);
        }
    }

    private void updateSerializeSectorLocations() {
        Logger.info("Serializing Sector Location Coordinates");
        for (String s : sectors.getKeys(true)) {
            if (s.contains("Corner")) {
                sectors.set(s, new SectorLocationBase(sectors.getString(s)).serialize());
            }
        }
    }

    private void updateConvertWorlds(ConfigurationSection[] dataConfigs) {
        //Convert World Names to UUID

        // Because the CuboidSystems sometimes need a world id,
        // AreaWilderness and AreaDefault will be converted to use world id and system id
        // In order to be universal.  This will result in the same key twice, nested.
        // But it's necessary to reuse code and have a unified architecture.
        Logger.info("Converting World Names to UUID.");
        for (ConfigurationSection config : dataConfigs)
            for (String path : config.getKeys(true)) {
                if (path.contains("Trust") || path.contains("Value") || path.contains("Message") || path.contains("InheritParent")) {
                    String[] paths = path.split("\\.");
                    World world = Bukkit.getWorld(paths[1]);
                    if (world != null) {
                        paths[1] = world.getUID().toString();
                    }
                    StringBuilder newPath = new StringBuilder(paths[0]);
                    for (int x = 1; x < paths.length; x++) {
                        newPath.append(".").append(paths[x]);
                    }
                    config.set(newPath.toString(), config.get(path));
                    config.set(path, null);
                }
            }

        // Remove keys based on names, UUID's will return null
        for (World w : Bukkit.getWorlds()) {
            data.set(cuboidPlugin.getName() + DELIMETER + w.getName(), null);
        }
    }

    private void updateMigrateSubdivisions() {
        Logger.info("Migrating Subdivisions");
        for(String key : data.getKeys(true)) {
            String[] nodes = key.split("\\.");
            if(nodes.length > 5 || key.contains("InheritParent")) {
                StringBuilder newKey = new StringBuilder(nodes[0]);

                if (cuboidPlugin == CuboidPlugin.RESIDENCE) {
                    for(int x = 1; x < nodes.length; x++) {
                        if (x == 2) {
                            newKey.append(ResidenceAPI.getResidenceManager().getByName(nodes[2] + "." + nodes[3]).getResidenceUUID());
                            continue;
                        }
                        if (x == 3) continue;
                        newKey.append(nodes[x]);
                    }
                } else {
                    for (int x = 1; x < nodes.length; x++) {
                        if (x == 2) continue; // Location of parent ID
                        newKey.append(DELIMETER).append(nodes[x]);
                    }
                }
                data.set(newKey.toString(), data.get(key));
                data.set(key, null);
            }
        }
    }

    private void updateMigratePermissions(ConfigurationSection[] dataconfigs) {
        Logger.info("Migrating Permission Trusts.");
        for(ConfigurationSection config : dataconfigs) {
            for (String key : config.getKeys(true)) {
                if (key.contains("Trust") && config.isList(key)) {
                    Logger.debug("Converting Trust List for " + key);
                    List<String> permissions = new ArrayList<String>();
                    List<String> players = new ArrayList<String>();
                    for(String trust : config.getStringList(key)) {
                        if(trust.contains(".")) {
                            Logger.debug("Converting Permission " + trust);
                            permissions.add(trust);
                        } else {
                            Logger.debug("Ignoring Player " + trust);
                            players.add(trust);
                        }
                    }
                    config.set(key.replace("Trust", "FlagPermissionTrust"), permissions);
                    config.set(key, players);
                }
            }
        }
    }

    private boolean updateConvertPlayers(ConfigurationSection[] dataconfigs) {
        Logger.info("Converting Player Names to UUID.");
        Set<String> players = new HashSet<String>(); // Use a set to prevent duplicates
        for (ConfigurationSection config : dataconfigs) {
            for (String key : config.getKeys(true)) {
                if (key.contains("Trust") && !key.contains("FlagPermissionTrust") && config.isList(key)) {
                    for (String p : config.getStringList(key)) {
                        OfflinePlayer player = Bukkit.getOfflinePlayer(p);
                        players.add(player.getName()); // Restores original casing
                    }
                }
            }
        }

        Map<String, UUID> playerMap = new HashMap<String, UUID>();
        boolean imported = false;
        int count = 0;
        while (!imported && count < 5) {
            try {
                Logger.debug("Fetching Player UUID");
                playerMap = new UUIDFetcher(new ArrayList<String>(players)).call();
                imported = true;
            } catch (Exception ex) {
                count++;
                if (count < 5)
                    Logger.warning("Failed to import UUID list for player trust. Retrying.");
                else {
                    Logger.error("Failed to import UUID list after 5 attempts." + ex.getMessage());
                    return false;
                }
            }
        }

        for (ConfigurationSection config : dataconfigs) {
            for (String key : config.getKeys(true)) {
                if (key.contains("Trust") && !key.contains("FlagPermissionTrust") && config.isList(key)) {
                    for (String p : config.getStringList(key)) {
                        Logger.debug("Writing Player UUID for " + p);
                        UUID u = playerMap.get(p);
                        if(u == null) continue;
                        Logger.debug("UUID: " + u.toString());
                        Logger.debug("Writing To: " + key.replace("Trust", "FlagPlayerTrust") + DELIMETER + u.toString());
                        config.set(key.replace("Trust", "FlagPlayerTrust") + DELIMETER + u.toString(), p);
                    }
                    config.set(key, null);
                }
            }
        }
        return true;
    }

    private void updateConvertFlags(ConfigurationSection[] dataconfigs) {
        for(ConfigurationSection config : dataconfigs) {
            for(String key : config.getKeys(true)) {
                if(key.contains("Value")) {
                    config.set(key.replace("Value", "FlagState"), config.get(key));
                    config.set(key, null);
                } else if (key.contains("Message")) {
                    config.set(key.replace("Message", "FlagMessage"), config.get(key));
                    config.set(key, null);
                }
            }
        }
    }

}
