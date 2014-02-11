package io.github.alshain01.flags.sector;

import io.github.alshain01.flags.events.SectorDeleteEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

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
        for(Sector s : sectors.values()) {
            Bukkit.getPluginManager().callEvent(new SectorDeleteEvent(s));
        }
        sectors.clear();
    }

    public Sector add(Location corner1, Location corner2, int depth) {
        Sector s = new Sector(corner1, corner2, depth);
        sectors.put(s.getID(), s);
        return s;
    }

    public void delete(UUID id) {
        Sector sector = get(id);
        if(sector.getParentID() == null) {
            // Removing parent shoudld remove subdivisions
            for(Sector s : sectors.values()) {
                if(s.getParentID().equals(id)) {
                    Bukkit.getPluginManager().callEvent(new SectorDeleteEvent(s));
                    sectors.remove(s.getID());
                }
            }
        }
        Bukkit.getPluginManager().callEvent(new SectorDeleteEvent(sector));
        sectors.remove(id);
    }

    public boolean delete(Location location) {
        Sector sector = getAt(location);
        if(sector == null) { return false; }
        delete(sector.getID());
        return true;
    }

    public boolean deleteTopLevel(Location location) {
        Sector sector = getAt(location);
        if(sector == null) { return false; }
        UUID id = sector.getParentID() != null ? sector.getParentID() : sector.getID();
        delete(id);
        return true;
    }

    public Sector get(UUID uid) {
        return sectors.get(uid);
    }

    public Sector getAt(Location location) {
        Sector foundParent = null;
        for(Sector s : sectors.values()) {
            if(s.contains(location)) {
                if(s.getParentID() == null) { // Check for subdivisions
                    foundParent = s;
                } else {
                    return s;
                }
            }
        }
        return foundParent;
    }
}
