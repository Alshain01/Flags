package io.github.alshain01.flags;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

class PlayerCache implements Listener {
    private static File dataFile;
    private static YamlConfiguration cache;

    PlayerCache(Plugin plugin) {
        dataFile = new File(plugin.getDataFolder(), "PlayerCache.yml");
        cache = YamlConfiguration.loadConfiguration(dataFile);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    static void write() {
        try {
            cache.save(dataFile);
        } catch (IOException ex) {
            Logger.error("Failed to write player cache.");
        }
    }

    static void rebuild() {
        for(OfflinePlayer p : Bukkit.getOfflinePlayers()) {
            cache.set(p.getName(), p.getUniqueId().toString());
        }
    }

    static OfflinePlayer getOfflinePlayer(String name) {
        if(cache.getKeys(false).contains(name))
            return Bukkit.getOfflinePlayer((UUID.fromString(cache.getString(name))));
        return null;
    }

    static void cachePlayer(String name, UUID pID) {
        if(!cache.getKeys(false).contains(name) || !pID.equals(UUID.fromString(cache.getString(name)))) {
            // Remove any previous names associated with this UUID
            for(String pName : cache.getKeys(false)) {
                if(UUID.fromString(cache.getString(pName)).equals(pID)) {
                    cache.set(pName, null);
                }
            }

            cache.set(name, pID.toString());
        }
    }

    static void cachePlayers(Map<String, UUID> players) {
        for(String name : players.keySet()) {
            cachePlayer(name, players.get(name));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerJoin(PlayerJoinEvent e) {
        cachePlayer(e.getPlayer().getName(), e.getPlayer().getUniqueId());
    }

}
