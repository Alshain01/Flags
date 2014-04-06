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

import io.github.alshain01.flags.area.*;
import io.github.alshain01.flags.economy.EconomyPurchaseType;

import java.io.File;
import java.io.IOException;
import java.util.*;

import io.github.alshain01.flags.sector.Sector;
import io.github.alshain01.flags.sector.SectorLocation;

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

    private final static String DATABASE_VERSION_PATH = "Default.Database.Version";
    private final static String PLAYER_TRUST_PATH = "PlayerTrust";
    private final static String PERM_TRUST_PATH = "PermissionTrust";
    private final static String VALUE_PATH = "Value";
    private final static String MESSAGE_PATH = "Message";
    private final static String INHERIT_PATH = "InheritParent";
    private final static String DELIMETER = ".";

    private final File dataFolder;
    private final Plugin plugin;
    private final int saveInterval;

    // Auto-Save manager
    private BukkitTask as;

	private YamlConfiguration data;
	private YamlConfiguration def;
	private YamlConfiguration wilderness;
	private YamlConfiguration bundle;
	private YamlConfiguration price;
    private YamlConfiguration sectors;

    private boolean saveData = false;

    /*
     * Constructor
     */
	DataStoreYaml(Plugin plugin, int autoSaveInterval) {
        this.dataFolder = plugin.getDataFolder();
        this.plugin = plugin;
        this.saveInterval = autoSaveInterval;

        // Upgrade sequence from older versions
        File worldFile = new File(dataFolder, "world.yml");
        File wildFile = new File(dataFolder, WILDERNESS_FILE);
        if(worldFile.exists() && !wildFile.exists()) {
            if(!worldFile.renameTo(wildFile)) {
                Logger.error("Failed to rename world.yml to wilderness.yml");
            }
        }

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

        if(CuboidType.getActive() == CuboidType.FLAGS) {
            sectors = YamlConfiguration.loadConfiguration(new File(dataFolder, SECTOR_FILE));
        }

        // Remove old auto-saves
        if(as != null ) {
            as.cancel();
            as = null;
        }

        // Set up autosave
        if (saveInterval > 0) {
            as = new AutoSave().runTaskTimer(plugin, saveInterval * 1200, saveInterval * 1200);
        }
    }

    public void save() {
        if(!saveData) { return; }
        try {
            wilderness.save(new File(dataFolder, WILDERNESS_FILE));
            def.save(new File(dataFolder, DEFAULT_FILE));
            data.save(new File(dataFolder, DATA_FILE));
            bundle.save(new File(dataFolder, BUNDLE_FILE));
            price.save(new File(dataFolder, PRICE_FILE));
            if(CuboidType.getActive() == CuboidType.FLAGS) {
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
        if (ver.getMajor() <= 2) {
            // Rename World to Wilderness
            YamlConfiguration woldConfig = getYml("wilderness");
            if (woldConfig.isConfigurationSection("World")) {
                ConfigurationSection cSec = woldConfig.getConfigurationSection("World");
                ConfigurationSection newCSec = woldConfig.createSection("Wilderness");
                for (final String k : cSec.getKeys(true)) {
                    if (k.contains(VALUE_PATH) || k.contains(MESSAGE_PATH)
                            || k.contains("Trust") || k.contains(INHERIT_PATH)) {
                        newCSec.set(k, cSec.get(k));
                    }
                }
            }
            woldConfig.set("World", null);

            // Convert Bundles
            if (bundle.isConfigurationSection("Bundle")) {
                if (bundle.isConfigurationSection("Bundle")) {
                    bundle.set("", price.getConfigurationSection("Bundle").getValues(true));
                    bundle.set("Bundle", null);
                }
                bundle.set("Bundle", null);
            }

            // Convert Prices
            if (price.isConfigurationSection("Price")) {
                if (price.isConfigurationSection("Price")) {
                    price.set("", price.getConfigurationSection("Price").getValues(true));
                    price.set("Price", null);
                }
                price.set("Price", null);
            }

            // Convert Sectors
            if(CuboidType.getActive() == CuboidType.FLAGS) {
                // Remove old header
                if (sectors.isConfigurationSection("Sectors")) {
                    sectors.set("", sectors.getConfigurationSection("Sectors").getValues(true));
                    sectors.set("Sectors", null);
                }

                // Convert old sector location to serialized form.
                for (String s : sectors.getKeys(true)) {
                    if(s.contains("Corner") && sectors.isString(s)) {
                        sectors.set(s, new SectorLocation(sectors.getString(s)).serialize());
                    }
                }
            }

            // Convert Subdivision Data To the upper level.
            for(World w : Bukkit.getWorlds()) {
                if (data.isConfigurationSection(w.getName() + DELIMETER + CuboidType.getActive().toString())) {
                    ConfigurationSection config = data.getConfigurationSection(w.getName() + DELIMETER + CuboidType.getActive().toString());
                    for (String p : config.getKeys(false)) { // Parent claims
                        for (String s : config.getConfigurationSection(p).getKeys(false)) { // Subdivision Claims
                            if (Flags.getRegistrar().getFlag(s) == null) { // If it's not a flag, it's a subdivision
                                if(CuboidType.getActive() == CuboidType.RESIDENCE) { // Residence uses the nested format by design, we will string replace it with a "-"
                                    config.set(p + "-" + s, config.getConfigurationSection(p + DELIMETER + s).getValues(true)); // Move the whole thing up one level
                                } else {
                                    config.set(s, config.getConfigurationSection(p + DELIMETER + s).getValues(true)); // Move the whole thing up one level
                                }
                                config.set(p + DELIMETER + s, null); // Erase it
                            }
                        }
                    }
                }
            }

            //Convert World Names to UUID
            for(String s : data.getConfigurationSection(CuboidType.getActive().toString()).getKeys(false)) {
                World world = Bukkit.getWorld(s);
                if(world != null) {
                    data.set(CuboidType.getActive().toString() + world.getUID().toString(),
                                data.getConfigurationSection(CuboidType.getActive().toString() + DELIMETER + s).getValues(true));
                    data.set(CuboidType.getActive().toString() + DELIMETER + s, null);
                }
            }

            // Because the CuboidSystems sometimes need a world id,
            // Wilderness and Default will be converted to use world id and system id
            // In order to be universal.  This will result in the same key twice, nested.
            // But it's necessary to reuse code later in the class.
            for(String s : wilderness.getConfigurationSection("Wilderness").getKeys(false)) {
                World world = Bukkit.getWorld(s);
                if(world != null) {
                    wilderness.set("Wilderness." + world.getUID().toString() + world.getUID().toString(),
                            wilderness.getConfigurationSection("Wilderness." + s).getValues(true));
                    wilderness.set("Wilderness." + s, null);
                }
            }

            for(String s : def.getConfigurationSection("Default").getKeys(false)) {
                World world = Bukkit.getWorld(s);
                if(world != null) {
                    def.set("Default." + world.getUID().toString() + world.getUID().toString(),
                            def.getConfigurationSection("Default." + s).getValues(true));
                    def.set("Default." + s, null);
                }
            }

            writeVersion(new DataStoreVersion(2, 0, 0));
            saveData = true;
        }
    }

    @Override
    public final Set<String> readBundles() {
            return bundle.getKeys(false);
    }

    @Override
	public final Set<Flag> readBundle(String bundleName) {
		final HashSet<Flag> flags = new HashSet<Flag>();
		final List<?> list = bundle.getList(bundleName, new ArrayList<String>());

		for (final Object o : list) {
			if (Flags.getRegistrar().isFlag((String) o)) {
				flags.add(Flags.getRegistrar().getFlag((String) o));
			}
		}
		return flags;
	}

    @Override
    public final void writeBundle(String name, Set<Flag> flags) {
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

        for(String s : this.sectors.getKeys(false)) {
            UUID sID = UUID.fromString(s);
            sectorMap.put(sID, new Sector(sID, sectors.getConfigurationSection(s).getValues(false)));
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
    public Set<Permission> readPermissionTrust(Area area, Flag flag) {
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
    public void writePermissionTrust(Area area, Flag flag, Set<Permission> permissions) {
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

        String path = area.getCuboidType().toString() + DELIMETER + area.getWorld().getName() + DELIMETER + area.getId();

        if(!getYml(path).isConfigurationSection(path)) { return true; }
        ConfigurationSection inheritConfig = getYml(path).getConfigurationSection(path);
        return !inheritConfig.isSet(INHERIT_PATH) || inheritConfig.getBoolean(INHERIT_PATH);
    }

    @Override
    public void writeInheritance(Area area, boolean value) {
        if ((area instanceof Subdividable) && ((Subdividable) area).isSubdivision()) {
            String path = area.getCuboidType().toString() + DELIMETER + area.getWorld().getName() + DELIMETER + area.getId();

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
        return area.getCuboidType().toString() + "." + area.getWorld().getName() + "." + area.getId();
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
