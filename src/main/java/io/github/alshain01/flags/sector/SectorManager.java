package io.github.alshain01.flags.sector;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SectorManager {
    private Map<UUID, Sector> sectors = new HashMap<UUID, Sector>();

    public Sector addSector(Location corner1, Location corner2) {
        Sector s = new Sector(corner1, corner2);
        sectors.put(s.getID(), s);
        return s;
    }

    public void deleteSector(UUID id) {
        sectors.remove(id);
    }

    public void loadSectors(ConfigurationSection config) {
        for(String s : config.getKeys(false)) {
            UUID id = UUID.fromString(s);
            sectors.put(id, new Sector(id, config.getConfigurationSection(s).getValues(false)));
        }
    }

    public Sector getSector(UUID uid) {
        return sectors.get(uid);
    }

    public Sector getSectorAt(Location location) {
        for(Sector s : sectors.values()) {
            if(s.contains(location)) {
                return s;
            }
        }
        return null;
    }
}
