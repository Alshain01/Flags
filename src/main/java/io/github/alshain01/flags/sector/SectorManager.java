package io.github.alshain01.flags.sector;

import io.github.alshain01.flags.events.SectorDeleteEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

final public class SectorManager {
    private final Map<UUID, Sector> sectors = new HashMap<UUID, Sector>();
    private final int defaultDepth;

    public SectorManager(JavaPlugin plugin, ConfigurationSection config, ConfigurationSection sectors) {
        this.defaultDepth = config.getInt("DefaultDepth");
        ConfigurationSerialization.registerClass(Sector.class);

        if(sectors != null) {
            for (String s : sectors.getKeys(false)) {
                UUID id = UUID.fromString(s);
                this.sectors.put(id, new Sector(id, sectors.getConfigurationSection(s).getValues(false)));
            }
        }

        plugin.getServer().getPluginManager().registerEvents(new SectorListener(Material.getMaterial(config.getString("Tool"))), plugin);
        plugin.getCommand("sector").setExecutor(new SectorCommand());
    }

    public void write(ConfigurationSection sectorConfig) {
        sectorConfig.set("Sectors", null);
        for(UUID u : sectors.keySet()) {
            sectorConfig.set("Sectors." + u.toString(), sectors.get(u).serialize());
        }
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
