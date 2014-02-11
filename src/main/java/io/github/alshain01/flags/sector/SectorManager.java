package io.github.alshain01.flags.sector;

import io.github.alshain01.flags.events.SectorDeleteEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SectorManager {
    private Map<UUID, Sector> sectors = new HashMap<UUID, Sector>();
    final int defaultDepth;

    public SectorManager(ConfigurationSection config, int defaultDepth) {
        for(String s : config.getKeys(false)) {
            UUID id = UUID.fromString(s);
            sectors.put(id, new Sector(id, config.getConfigurationSection(s).getValues(false)));
        }
        this.defaultDepth = defaultDepth;
    }

    public void write(ConfigurationSection config) {
        for(UUID u : sectors.keySet()) {
            config.set(u.toString(), sectors.get(u).serialize());
        }
    }

    public void clear() {
        sectors.clear();
    }

    public Sector add(Location corner1, Location corner2, int depth) {
        Sector s = new Sector(corner1, corner2, depth);
        sectors.put(s.getID(), s);
        return s;
    }

    public void delete(UUID id) {
        Bukkit.getPluginManager().callEvent(new SectorDeleteEvent(sectors.get(id)));
        sectors.remove(id);
    }

    public boolean delete(Location location) {
        for(Sector s : sectors.values()) {
            if(s.contains(location)) {
                Bukkit.getPluginManager().callEvent(new SectorDeleteEvent(sectors.get(s.getID())));
                sectors.remove(s.getID());
                return true;
            }
        }
        return false;
    }

    public Sector get(UUID uid) {
        return sectors.get(uid);
    }

    public Sector getAt(Location location) {
        for(Sector s : sectors.values()) {
            if(s.contains(location)) {
                return s;
            }
        }
        return null;
    }
}
