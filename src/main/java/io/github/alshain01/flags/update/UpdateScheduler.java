package io.github.alshain01.flags.update;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import io.github.alshain01.flags.update.Updater.UpdateResult;
import io.github.alshain01.flags.update.Updater.UpdateType;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public class UpdateScheduler extends BukkitRunnable{
    private final Updater.UpdateType type;
    private final File file, dataFolder;
    private final String version, updateFolder, key;
    private final List<String> authors;
    private final Logger logger;

    private Updater updater = null;

    public UpdateScheduler(Plugin plugin, File file, ConfigurationSection config) {
        // Move everything out of bukkit so we can run async
        this.file = file;
        this.type = config.getBoolean("Download") ? UpdateType.DEFAULT : UpdateType.NO_DOWNLOAD;
        this.key = config.getString("ServerModsAPIKey");
        this.dataFolder = plugin.getDataFolder();
        this.version = plugin.getDescription().getVersion();
        this.updateFolder = plugin.getServer().getUpdateFolder();
        this.authors = plugin.getDescription().getAuthors();
        this.logger = plugin.getLogger();
        new UpdateNotifier().runTaskTimer(plugin, 54000, 1728000);
    }

    protected UpdateResult getResult() {
        return updater.getResult();
    }

    @Override
    public void run() {
        final int PLUGIN_ID = 65024;
        updater = new Updater(authors, dataFolder, updateFolder, version, logger, PLUGIN_ID, file, type, key, true);
    }

    private class UpdateNotifier extends BukkitRunnable {
        @Override
        public void run() {
            if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
                Bukkit.getServer().getConsoleSender().sendMessage("[Flags] " + ChatColor.DARK_PURPLE
                        + "The version of Flags that this server is running is out of date. "
                        + "Please consider updating to the latest version at dev.bukkit.org/bukkit-plugins/flags/.");
            } else if (updater.getResult() == UpdateResult.SUCCESS) {
                Bukkit.getServer().getConsoleSender().sendMessage("[Flags] " + ChatColor.DARK_PURPLE
                        + "An update to Flags has been downloaded and will be installed when the server is reloaded.");
            }
        }
    }
}
