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

import io.github.alshain01.flags.api.*;
import io.github.alshain01.flags.api.economy.EconomyBaseValue;
import io.github.alshain01.flags.sector.SectorManager;
import io.github.alshain01.flags.api.DataStore.DataStoreType;
import io.github.alshain01.flags.api.event.PlayerChangedAreaEvent;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Flags
 * 
 * @author Alshain01
 */
public class Flags extends JavaPlugin {
    private boolean sqlData = false;

    // Made static to access from enumerations and lower hefty method calls
    private static DataStore dataStore;
    private static Economy economy = null;
    private static boolean debugOn = false;

    // Made static for use by API
    private static SectorManager sectors;
    private static boolean borderPatrol = false;

	/**
	 * Called when this plug-in is enabled
	 */
	@Override
	public void onEnable() {
        PluginManager pm = getServer().getPluginManager();


        // Set up the plugin's configuration file
        saveDefaultConfig();

        // Initialize the static variables
        debugOn = getConfig().getBoolean("Debug.Enabled");

        // Acquire the messages from configuration
        Message.load(this);

        economy = setupEconomy();
        EconomyBaseValue.valueOf(getConfig().getString("Economy.BaseValue")).set();

        // Activate the API
        CuboidPlugin cuboidPlugin = findCuboidPlugin(pm, getConfig().getList("AreaPlugins"));
        dataStore = findDataStore();
        dataStore.create(this);
        dataStore.update(this);
        sqlData = dataStore instanceof DataStoreMySQL;
        FlagsAPI.initialize(cuboidPlugin, dataStore);

        // Load Mr. Clean
        MrClean.enable(this, getConfig().getBoolean("MrClean"));

        // Configure the updater
		if (getConfig().getBoolean("Update.Enabled")) {
            new Updater(this);
		}

		// Load Border Patrol
        ConfigurationSection bpConfig = getConfig().getConfigurationSection("BorderPatrol");
		if (bpConfig.getBoolean("Enable")) {
            borderPatrol = true;
			BorderPatrol bp = new BorderPatrol(bpConfig.getInt("EventDivisor"), bpConfig.getInt("TimeDivisor"));
			pm.registerEvents(bp, this);
		}

        // Load Sectors
        if(FlagsAPI.getCuboidPlugin() == CuboidPlugin.FLAGS) {
            sectors = new SectorManager(dataStore, getConfig().getConfigurationSection("Sector").getInt("DefaultDepth"));
        }

        // Set Command Executors
        CommandFlag executor = new CommandFlag(Material.valueOf(getConfig().getString("Tools.FlagQuery")));
        getCommand("flag").setExecutor(executor);
        pm.registerEvents(executor, this);
        getCommand("bundle").setExecutor(new CommandBundle());

 		// Schedule tasks to perform after server is running
		new onServerEnabledTask(this, getConfig().getBoolean("Metrics.Enabled")).runTask(this);
		Logger.info("Flags Has Been Enabled.");
	}

    /**
     * Called when this plug-in is disabled
     */
    @Override
    public void onDisable() {
        dataStore.close();
        FlagsAPI.close(dataStore);
        Logger.close();

        // Static cleanup
        dataStore = null;
        economy = null;
        sectors = null;
        this.getLogger().info("Flags Has Been Disabled.");
    }

	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if(!label.equalsIgnoreCase("flags")) { return false; }
        // Handle administration command

        if(args.length < 1) {
            return false;
        }

        if(args[0].equalsIgnoreCase("reload")) {
            this.reload();
        }

        if(args[0].equalsIgnoreCase("import")) {
            if(sqlData) {
                dataStore.importDataStore(new DataStoreYaml(this, false));
                return true;
            }
            sender.sendMessage(Message.SQLDatabaseError.get());
            return true;
        }

        /*
        if(args[0].equalsIgnoreCase("export")) {
            if(sqlData) {
                ((DataStoreMySQL)dataStore).exportDB();
                return true;
            }
            sender.sendMessage(Message.SQLDatabaseError.get());
            return true;
        }
        */
        return false;
	}

    /*
     * Reloads YAML files
     */
    private void reload() {
        this.reloadConfig();

        // Acquire the messages from configuration
        Message.load(this);

        EconomyBaseValue.valueOf(this.getConfig().getString("Economy.BaseValue")).set();

        debugOn = getConfig().getBoolean("Debug");
        dataStore.reload();
        Logger.info("Flag Database Reloaded");
    }

    private CuboidPlugin findCuboidPlugin(PluginManager pm, List<?> plugins) {
        if(plugins != null && plugins.size() > 0) {
            for(Object o : plugins) {
                if (pm.isPluginEnabled((String) o)) {
                    Logger.info(o + " detected. Enabling integrated support.");
                    return CuboidPlugin.getByName((String) o);
                }
            }
        }
        Logger.info("No cuboid system detected. Flags Sectors Enabled.");
        return CuboidPlugin.FLAGS;
    }

    private DataStore findDataStore() {
        DataStoreType dbType = DataStoreType.valueOf(getConfig().getString("Database"));
        switch(dbType) {
            case MYSQL:
                return new DataStoreMySQL(this);
            default:
                return new DataStoreYaml(this);
        }
    }

    /**
     * Checks if the provided string represents a version number that is equal
     * to or lower than the current Bukkit API version.
     *
     * String should be formatted with 3 numbers: x.y.z
     *
     * @return true if the version provided is compatible
     */
    public static boolean checkAPI(String version) {
        try {
            final String bukkitVersion = Bukkit.getServer().getBukkitVersion();
            final float apiVersion = Float.valueOf(bukkitVersion.substring(0, 3));
            final float CompareVersion = Float.valueOf(version.substring(0, 3));
            final int apiBuild = Integer.valueOf(bukkitVersion.substring(4, 5));
            final int CompareBuild = Integer.valueOf(version.substring(4, 5));

            return (apiVersion > CompareVersion
                    || apiVersion == CompareVersion	&& apiBuild >= CompareBuild);
        } catch (NumberFormatException ex) {
            Logger.warning("The API version could not be determined. Some compatible flags may be disabled.");
            return false;
        }
    }

    /**
     * Gets the status of the border patrol event listener. (i.e
     * PlayerChangedAreaEvent)
     *
     * @return The status of the border patrol listener
     */
    public static boolean getBorderPatrolEnabled() { return borderPatrol; }

    /**
     * Gets the DataStore used by Flags, this should not be used by plugins.
     *
     * @return The dataStore object in Flags.
     */
    static DataStore getDataStore() { return dataStore; }

    /**
     * Gets the vault economy for this instance of Flags.
     *
     * @return The vault economy.
     */
    static Economy getEconomy() { return economy; }

    /**
     * Gets the sector manager for this instance of Flags.
     *
     * @return The flag registrar. Null if disabled.
     */
    public static SectorManager getSectorManager() { return sectors; }


	/*
	 * Tasks that must be run only after the entire sever has loaded.
	 * Runs on first server tick.
	 */
	private class onServerEnabledTask extends BukkitRunnable {
        private onServerEnabledTask(JavaPlugin plugin, boolean mcStats) {
            this.plugin = plugin;
            this.mcStats = mcStats;
        }

        private final JavaPlugin plugin;
        private final boolean mcStats;

		@Override
		public void run() {
			// Check the handlers to see if anything is registered for Border Patrol
			final RegisteredListener[] listeners = PlayerChangedAreaEvent.getHandlerList().getRegisteredListeners();
			if (borderPatrol && (listeners == null || listeners.length == 0)) {
                borderPatrol = false;
                PlayerMoveEvent.getHandlerList().unregister(Bukkit.getPluginManager().getPlugin("Flags"));
				Logger.info("No plugins have registered for Flags' Border Patrol listener. Unregistering PlayerMoveEvent.");
			}

            if (mcStats && !debugOn && Flags.checkAPI("1.3.2")) {
                Metrics.StartFlagsMetrics(plugin);
            }
		}
	}

	/*
	 * Register with the Vault economy plugin.
	 * 
	 * @return True if the economy was successfully configured.
	 */
	private static Economy setupEconomy() {
		if (Bukkit.getServer().getPluginManager().isPluginEnabled("Vault")) {
            final RegisteredServiceProvider<Economy> economyProvider = Bukkit
				    .getServer().getServicesManager()
				    .getRegistration(net.milkbowl.vault.economy.Economy.class);
		    if (economyProvider != null) {
			    return economyProvider.getProvider();
		    }
        }
		return null;
	}
}
