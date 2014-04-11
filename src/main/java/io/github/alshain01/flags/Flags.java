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
import io.github.alshain01.flags.DataStore.DataStoreType;
import io.github.alshain01.flags.api.event.PlayerChangedAreaEvent;

import io.github.alshain01.flags.api.sector.SectorManager;
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
final public class Flags extends JavaPlugin {
    // Made static to access from enumerations and lower hefty method calls
    private static DataStore dataStore;
    private static Economy economy = null;

    // Made static for use by API
    private static boolean borderPatrol = false;

	/**
	 * Called when this plug-in is enabled
	 */
	@Override
	public void onEnable() {
        PluginManager pm = getServer().getPluginManager();


        // Set up the plugin's configuration file
        saveDefaultConfig();

        // Acquire the messages from configuration
        Message.load(this);

        economy = setupEconomy();
        EconomyBaseValue.valueOf(getConfig().getString("Economy.BaseValue")).set();

        // Create the database
        CuboidPlugin cuboidPlugin = findCuboidPlugin(pm, getConfig().getList("AreaPlugins"));
        dataStore = findDataStore(cuboidPlugin);
        dataStore.create(this);
        if(!dataStore.update(this)) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Load Sectors
        SectorManager sectors = null;
        if(cuboidPlugin == CuboidPlugin.FLAGS) {
            sectors = new SectorManagerBase(this, dataStore, getConfig().getConfigurationSection("Sector").getInt("DefaultDepth"));
        }

        // Start the API
        FlagsAPI.initialize(this, cuboidPlugin, sectors, dataStore);

        if(cuboidPlugin == CuboidPlugin.FLAGS) {
            ((SectorManagerBase)sectors).loadSectors();
        }

        // Load Cleanable Listener
        AreaFactory.registerCleaner(cuboidPlugin, this);

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
        FlagsAPI.close(dataStore);
        dataStore.close();
        Logger.close();

        // Static cleanup
        dataStore = null;
        economy = null;
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
            if(dataStore instanceof DataStoreMySQL) {
                dataStore.importDataStore(new DataStoreYaml(this, FlagsAPI.getCuboidPlugin(), false));
                return true;
            } else {
                dataStore.importDataStore(new DataStoreMySQL(this));
            }
            return true;
        }
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

    private DataStore findDataStore(CuboidPlugin cuboidPlugin) {
        DataStoreType dbType = DataStoreType.valueOf(getConfig().getString("Database"));
        switch(dbType) {
            case MYSQL:
                return new DataStoreMySQL(this);
            default:
                return new DataStoreYaml(this, cuboidPlugin);
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
            Metrics.StartFlagsMetrics(plugin);
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
