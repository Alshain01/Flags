package io.github.alshain01.flags.update;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import io.github.alshain01.flags.update.Updater.UpdateResult;

import java.util.logging.Logger;

public class UpdateScheduler extends BukkitRunnable{
    private final String version, key;
    private final Logger logger;

    private Updater updater = null;

    public UpdateScheduler(Plugin plugin, ConfigurationSection config) {
        // Move everything out of bukkit so we can run async
        this.key = config.getString("ServerModsAPIKey");
        this.version = plugin.getDescription().getVersion();
        this.logger = plugin.getLogger();
    }

    UpdateResult getResult() {
        return updater.getResult();
    }

    @Override
    public void run() {
        updater = new Updater(version, logger, key);

        if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
            logger.info("The version of Flags that this server is running is out of date. "
                    + "Please consider updating to the latest version at dev.bukkit.org/bukkit-plugins/flags/.");
        }
    }
}
