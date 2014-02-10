package io.github.alshain01.flags.update;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import io.github.alshain01.flags.update.Updater.UpdateResult;

public class UpdateListener implements Listener {
    UpdateScheduler scheduler;

    public UpdateListener(UpdateScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlayerJoin(PlayerJoinEvent e) {
        if(scheduler == null) { return; }
        if (e.getPlayer().hasPermission("flags.admin.notifyupdate")) {
            if(scheduler.getResult() == UpdateResult.UPDATE_AVAILABLE) {
                e.getPlayer().sendMessage(ChatColor.DARK_PURPLE
                        + "The version of Flags that this server is running is out of date. "
                        + "Please consider updating to the latest version at dev.bukkit.org/bukkit-plugins/flags/.");
            } else if(scheduler.getResult() == UpdateResult.SUCCESS) {
                e.getPlayer().sendMessage("[Flags] " + ChatColor.DARK_PURPLE
                        + "An update to Flags has been downloaded and will be installed when the server is reloaded.");
            }
        }
    }
}
