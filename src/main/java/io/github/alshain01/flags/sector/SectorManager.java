package io.github.alshain01.flags.sector;

import io.github.alshain01.flags.DataStore;
import io.github.alshain01.flags.api.event.SectorDeleteEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

final public class SectorManager {
    private final Map<UUID, Sector> sectors;
    private final int defaultDepth;
    private final DataStore dataStore;

    public SectorManager(JavaPlugin plugin, DataStore data, int defaultDepth) {
        ConfigurationSerialization.registerClass(Sector.class);
        ConfigurationSerialization.registerClass(SectorLocation.class);
        plugin.getCommand("sector").setExecutor(new SectorCommand());
        this.defaultDepth = defaultDepth;
        this.sectors = data.readSectors();
        this.dataStore = data;
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
        } while (sectors.containsKey(newID));

        Sector s = new Sector(newID, corner1, corner2, defaultDepth);
        sectors.put(s.getID(), s);
        dataStore.writeSector(s);
        return s;
    }

    Sector add(Location corner1, Location corner2, UUID parent) {
        UUID newID;
        do {
            newID = UUID.randomUUID();
        } while (sectors.containsKey(newID));
        Sector s = new Sector(newID, corner1, corner2, defaultDepth, parent);
        sectors.put(s.getID(), s);
        dataStore.writeSector(s);
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
        dataStore.deleteSector(id);
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

    public Collection<Sector> getAll() {
        return sectors.values();
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
