package io.github.alshain01.flags.sector;

import io.github.alshain01.flags.data.CustomYML;
import io.github.alshain01.flags.events.SectorDeleteEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class SectorManager {
    private final Map<UUID, Sector> sectors = new HashMap<UUID, Sector>();
    private final int defaultDepth;

    public SectorManager(CustomYML cYml, int defaultDepth) {
        this.defaultDepth = defaultDepth;
        if(!cYml.getConfig().isConfigurationSection("Sectors")) { return; }
        ConfigurationSection config = cYml.getConfig().getConfigurationSection("Sectors");

        for(String s : config.getKeys(false)) {
            UUID id = UUID.fromString(s);
            sectors.put(id, new Sector(id, config.getConfigurationSection(s).getValues(false)));
        }

    }

    public void write(CustomYML cYml) {
        cYml.getConfig().set("Sectors", null);
        for(UUID u : sectors.keySet()) {
            cYml.getConfig().set("Sectors." + u.toString(), sectors.get(u).serialize());
        }
        cYml.saveConfig();
    }

    public void clear() {
        for(Sector s : sectors.values()) {
            Bukkit.getPluginManager().callEvent(new SectorDeleteEvent(s));
        }
        sectors.clear();
    }
    Sector add(Location corner1, Location corner2) {
        UUID newID;
        do {
            newID = UUID.randomUUID();
        } while (!sectors.containsKey(newID));

        Sector s = new Sector(newID, corner1, corner2, defaultDepth);
        sectors.put(s.getID(), s);
        return s;
    }

    Sector add(Location corner1, Location corner2, UUID parent) {
        UUID newID;
        do {
            newID = UUID.randomUUID();
        } while (!sectors.containsKey(newID));
        Sector s = new Sector(newID, corner1, corner2, defaultDepth, parent);
        sectors.put(s.getID(), s);
        return s;
    }

/*
    Sector add(Location corner1, Location corner2, int depth) {
        Sector s = new Sector(corner1, corner2, depth);
        sectors.put(s.getID(), s);
        return s;
    }

    Sector add(Location corner1, Location corner2, int depth, UUID parent) {
        Sector s = new Sector(corner1, corner2, depth, parent);
        sectors.put(s.getID(), s);
        return s;
    }
*/
    @SuppressWarnings("WeakerAccess") // API
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

    public boolean isOverlap(Location corner1, Location corner2) {
        for(Sector s : sectors.values()) {
            if(s.overlaps(corner1, corner2)) {
                return true;
            }
        }
        return false;
    }

    public UUID isContained(Location corner1, Location corner2) {
        for(Sector s : sectors.values()) {
            if(s.contains(corner1, corner2)) {
                return s.getID();
            }
        }
        return null;
    }
}
