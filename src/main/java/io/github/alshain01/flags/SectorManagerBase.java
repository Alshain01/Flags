package io.github.alshain01.flags;

import io.github.alshain01.flags.api.event.SectorDeleteEvent;
import io.github.alshain01.flags.api.sector.Sector;
import io.github.alshain01.flags.api.sector.SectorLocation;
import io.github.alshain01.flags.api.sector.SectorManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import javax.annotation.Nonnull;
import java.util.*;

final class SectorManagerBase implements SectorManager {
    private Map<UUID, Sector> sectors;
    private final int defaultDepth;
    private final DataStore dataStore;

    public SectorManagerBase(Flags plugin, DataStore data, int defaultDepth) {
        ConfigurationSerialization.registerClass(Sector.class);
        ConfigurationSerialization.registerClass(SectorLocation.class);
        plugin.getCommand("sector").setExecutor(new CommandSector());
        Bukkit.getPluginManager().registerEvents(new SectorListener(Material.valueOf(plugin.getConfig().getString("Tools.Sector"))), plugin);
        this.defaultDepth = defaultDepth;
        this.dataStore = data;
    }

    void loadSectors() {
        this.sectors = dataStore.readSectors();
    }

    @Override
    public void clear() {
        for(Sector s : sectors.values()) {
            Bukkit.getPluginManager().callEvent(new SectorDeleteEvent(s));
        }
        sectors.clear();
    }

    @Override
    public Sector add(@Nonnull Location corner1, @Nonnull Location corner2) {
        UUID newID;
        do {
            newID = UUID.randomUUID();
        } while (sectors.containsKey(newID));

        Sector s = new SectorBase(newID, corner1, corner2, defaultDepth);
        sectors.put(s.getID(), s);
        dataStore.writeSector(s);
        return s;
    }

    @Override
    public Sector add(@Nonnull Location corner1, @Nonnull Location corner2, @Nonnull UUID parent) {
        UUID newID;
        do {
            newID = UUID.randomUUID();
        } while (sectors.containsKey(newID));
        Sector s = new SectorBase(newID, corner1, corner2, defaultDepth, parent);
        sectors.put(s.getID(), s);
        dataStore.writeSector(s);
        return s;
    }

    @Override
    public void delete(@Nonnull UUID id) {
        Sector sector = get(id);
        if(sector.getParent() == null) {
            // Removing parent shoudld remove subdivisions
            for(Sector s : sectors.values()) {
                if(s.getParent().getID().equals(id)) {
                    Bukkit.getPluginManager().callEvent(new SectorDeleteEvent(s));
                    sectors.remove(s.getID());
                }
            }
        }
        Bukkit.getPluginManager().callEvent(new SectorDeleteEvent(sector));
        dataStore.deleteSector(id);
        sectors.remove(id);
    }

    @Override
    public boolean delete(@Nonnull Location location) {
        Sector sector = getAt(location);
        if(sector == null) { return false; }
        delete(sector.getID());
        return true;
    }

    @Override
    public boolean deleteTopLevel(@Nonnull Location location) {
        Sector sector = getAt(location);
        if(sector == null) { return false; }
        UUID id = sector.getParent() != null ? sector.getParent().getID() : sector.getID();
        delete(id);
        return true;
    }

    @Override
    public Sector get(@Nonnull UUID uid) {
        return sectors.get(uid);
    }

    @Override
    public Sector getAt(@Nonnull Location location) {
        Sector foundParent = null;
        for(Sector s : sectors.values()) {
            if(s.contains(location)) {
                if(s.getParent() == null) { // Check for subdivisions
                    foundParent = s;
                } else {
                    return s;
                }
            }
        }
        return foundParent;
    }

    @Override
    public Collection<Sector> getAll() {
        return sectors.values();
    }

    @Override
    public boolean isOverlap(@Nonnull Location corner1, @Nonnull Location corner2) {
        for(Sector s : sectors.values()) {
            if(s.overlaps(corner1, corner2)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public UUID isContained(@Nonnull Location corner1, @Nonnull Location corner2) {
        for(Sector s : sectors.values()) {
            if(s.contains(corner1, corner2)) {
                return s.getID();
            }
        }
        return null;
    }
}
