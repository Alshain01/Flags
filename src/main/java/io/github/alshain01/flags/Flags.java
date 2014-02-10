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

import io.github.alshain01.flags.update.UpdateListener;
import io.github.alshain01.flags.update.UpdateScheduler;
import io.github.alshain01.flags.commands.Command;
import io.github.alshain01.flags.data.*;
import io.github.alshain01.flags.events.PlayerChangedAreaEvent;
import io.github.alshain01.flags.importer.GPFImport;
import io.github.alshain01.flags.metrics.MetricsManager;

import java.io.File;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Flags
 * 
 * @author Alshain01
 */
public class Flags extends JavaPlugin {
	protected static CustomYML messageStore;
	protected static System currentSystem = System.WORLD;
    private static DataStore dataStore;

	private static Economy economy = null;
	private static Registrar flagRegistrar = new Registrar();
    private boolean debugOn = false, sqlData = false;
    private static boolean borderPatrol = false;

	/**
	 * Called when this plug-in is enabled
	 */
	@Override
	public void onEnable() {
        // Cleanup old stuff
        File file = new File(this.getDataFolder(), "metrics.yml");
        if(file.exists()) { file.delete(); }

		// Create the configuration file if it doesn't exist
		saveDefaultConfig();
        PluginManager pm = getServer().getPluginManager();
        final ConfigurationSection pluginConfig = getConfig().getConfigurationSection("Flags");
        debugOn = pluginConfig.getBoolean("Debug");

        // Configure the updater
        ConfigurationSection updateConfig = pluginConfig.getConfigurationSection("Update");
		if (updateConfig.getBoolean("Check")) {
            UpdateScheduler updater = new UpdateScheduler(getFile(), updateConfig);
            updater.run();
			updater.runTaskTimer(this, 0, 1728000);
            pm.registerEvents(new UpdateListener(updater), this);
		}

		// Create the specific implementation of DataStore
		(messageStore = new CustomYML(this, "message.yml")).saveDefaultConfig();

		// Find the first available land management system
        currentSystem = System.find(pm, pluginConfig.getList("AreaPlugins"));
		this.getLogger().info(currentSystem == System.WORLD
                ? "No system detected. Only world flags will be available."
                : currentSystem.getDisplayName() + " detected. Enabling integrated support.");

		// Check for older database and import as necessary.
		if (currentSystem == System.GRIEF_PREVENTION
				&& !pm.isPluginEnabled("GriefPreventionFlags")) {
			GPFImport.importGPF();
		}

        dataStore = DataStoreType.getType(pluginConfig.getString("Database.Url")).getDataStore(this);

        // New installation
        if (!dataStore.create(this)) {
            this.getLogger().severe("Failed to create database schema. Shutting down Flags.");
            pm.disablePlugin(this);
            return;
        }
		dataStore.update(this);
        sqlData = dataStore instanceof SQLDataStore;

		// Enable Vault support
		economy = setupEconomy();

		// Load Mr. Clean
        if(pluginConfig.getBoolean("MrClean")) {
		    MrClean.enable(this);
        }

		// Load Border Patrol
        ConfigurationSection bpConfig = pluginConfig.getConfigurationSection("BorderPatrol");
		if (bpConfig.getBoolean("Enable")) {
            borderPatrol = true;
			BorderPatrol bp = new BorderPatrol(bpConfig.getInt("EventDivisor"), bpConfig.getInt("TimeDivisor"));
			pm.registerEvents(bp, this);
		}

 		// Schedule tasks to perform after server is running
		new onServerEnabledTask(this.getLogger(), pluginConfig.getBoolean("Metrics.Enabled")).runTask(this);
		this.getLogger().info("Flags Has Been Enabled.");
	}

    /**
     * Called when this plug-in is disabled
     */
    @Override
    public void onDisable() {
        if(sqlData) { ((SQLDataStore)dataStore).close(); }

        // Static cleanup
        economy = null;
        dataStore = null;
        messageStore = null;
        flagRegistrar = null;
        currentSystem = null;

        this.getLogger().info("Flags Has Been Disabled.");
    }

	/**
	 * Executes the given command, returning its success
	 * 
	 * @param sender
	 *            Source of the command
	 * @param cmd
	 *            Command which was executed
	 * @param label
	 *            Alias of the command which was used
	 * @param args
	 *            Passed command arguments
	 * @return true if a valid command, otherwise false
	 * 
	 */
	@Override
	public boolean onCommand(CommandSender sender,
			org.bukkit.command.Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("flags")) {
            if(args.length < 1) {
                return false;
            }

            if(args[0].equalsIgnoreCase("reload")) {
                this.reload();
            }

            if(args[0].equalsIgnoreCase("import")) {
                if(dataStore instanceof SQLDataStore) {
                    ((SQLDataStore)dataStore).importDB();
                    return true;
                }
                sender.sendMessage(Message.SQLDatabaseError.get());
                return true;
            }

            /*
            if(args[0].equalsIgnoreCase("export")) {
                if(dataStore instanceof SQLDataStore) {
                    ((SQLDataStore)dataStore).exportDB();
                    return true;
                }
                sender.sendMessage(Message.SQLDatabaseError.get());
                return true;
            }
            */

            return false;
        }
		if (cmd.getName().equalsIgnoreCase("flag")) {
			return Command.onFlagCommand(sender, args);
		}

		return (cmd.getName().equalsIgnoreCase("bundle") && Command.onBundleCommand(sender, args));
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
        final String bukkitVersion = Bukkit.getServer().getBukkitVersion();
        final float apiVersion = Float.valueOf(bukkitVersion.substring(0, 3));
        final float CompareVersion = Float.valueOf(version.substring(0, 3));
        final int apiBuild = Integer.valueOf(bukkitVersion.substring(4, 5));
        final int CompareBuild = Integer.valueOf(version.substring(4, 5));

        return (apiVersion > CompareVersion
                || apiVersion == CompareVersion	&& apiBuild >= CompareBuild);
    }

    /**
     * Sends a debug message through the Flags logger.
     *
     * @param message
     *            The message
     */
    //public static void debug(String message) {
//        if(debugOn) {
//            Bukkit.getServer().getPluginManager().getPlugin("Flags").getLogger().info("[DEBUG] " + message);
//        }
//    }

    /**
     * Gets the status of the border patrol event listener. (i.e
     * PlayerChangedAreaEvent)
     *
     * @return The status of the border patrol listener
     */
    public static boolean getBorderPatrolEnabled() {
        return borderPatrol;
    }

    /**
     * Gets the DataStore used by Flags. In most cases, plugins should not
     * attempt to access this directly.
     *
     * @return The dataStore object in Flags.
     */
    public static DataStore getDataStore() {
        return dataStore;
    }

    /**
     * Gets the vault economy for this instance of Flags.
     *
     * @return The vault economy.
     */
    public static Economy getEconomy() {
        return economy;
    }

    /**
     * Gets the registrar for this instance of Flags.
     *
     * @return The flag registrar.
     */
    public static Registrar getRegistrar() {
        return flagRegistrar;
    }

	/*
	 * Tasks that must be run only after the entire sever has loaded. Runs on
	 * first server tick.
	 */
	private class onServerEnabledTask extends BukkitRunnable {
        private onServerEnabledTask(Logger logger, boolean mcStats) {
            this.mcStats = mcStats;
            this.logger = logger;
        }

        private final Logger logger;
        private final boolean mcStats;

		@Override
		public void run() {
            // Tell Bukkit about the existing bundles
            Bundle.registerPermissions();

			if (mcStats && !debugOn && checkAPI("1.3.2")) {
				MetricsManager.StartMetrics(Bukkit.getServer().getPluginManager().getPlugin("Flags"));
			}

			// Check the handlers to see if anything is registered for Border
			// Patrol
			final RegisteredListener[] listeners = PlayerChangedAreaEvent.getHandlerList().getRegisteredListeners();
			if (borderPatrol && (listeners == null || listeners.length == 0)) {
                borderPatrol = false;
                PlayerMoveEvent.getHandlerList().unregister(Bukkit.getPluginManager().getPlugin("Flags"));
				logger.info("No plugins have registered for Flags' Border Patrol listener. Unregistering PlayerMoveEvent.");
			}
		}
	}

    /*
     * Reloads YAML files
     */
    private void reload() {
        this.reloadConfig();
        debugOn = getConfig().getBoolean("Flags.Debug");

        messageStore.reload();
        dataStore.reload();
        this.getLogger().info("Flag Database Reloaded");
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
