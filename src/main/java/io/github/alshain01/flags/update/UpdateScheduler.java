package io.github.alshain01.flags.update;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import io.github.alshain01.flags.update.Updater.UpdateResult;
import io.github.alshain01.flags.update.Updater.UpdateType;

import java.io.File;

public class UpdateScheduler extends BukkitRunnable{
    private final int PLUGIN_ID = 65024;
    private final Updater.UpdateType type;
    private final String key;
    private final File file;

    private Updater updater = null;

    public UpdateScheduler(File file, ConfigurationSection config) {
        this.file = file;
        this.type = config.getBoolean("Download") ? UpdateType.DEFAULT : UpdateType.NO_DOWNLOAD;
        this.key = config.getString("ServerModsAPIKey");
    }

    protected UpdateResult getResult() {
        return updater.getResult();
    }

    @Override
    public void run() {
        // Update script
        final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Flags");
        updater = new Updater(plugin, PLUGIN_ID, file, type, key, type == UpdateType.DEFAULT);

        if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
            Bukkit.getServer().getConsoleSender()
                    .sendMessage("[Flags] "	+ ChatColor.DARK_PURPLE
                            + "The version of Flags that this server is running is out of date. "
                            + "Please consider updating to the latest version at dev.bukkit.org/bukkit-plugins/flags/.");
        } else if (updater.getResult() == UpdateResult.SUCCESS) {
            Bukkit.getServer().getConsoleSender()
                    .sendMessage("[Flags] "	+ ChatColor.DARK_PURPLE
                            + "An update to Flags has been downloaded and will be installed when the server is reloaded.");
        }
    }
}
