package io.github.alshain01.flags.api.sector;

import io.github.alshain01.flags.api.event.SectorDeleteEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Collection;
import java.util.UUID;


public interface SectorManager {
    public void clear();

    public Sector add(Location corner1, Location corner2);

    public Sector add(Location corner1, Location corner2, UUID parent);

    public void delete(UUID id);

    public boolean delete(Location location);

    public boolean deleteTopLevel(Location location);

    public Sector get(UUID uid);

    public Sector getAt(Location location);

    public Collection<Sector> getAll();

    public boolean isOverlap(Location corner1, Location corner2);

    public UUID isContained(Location corner1, Location corner2);
}

