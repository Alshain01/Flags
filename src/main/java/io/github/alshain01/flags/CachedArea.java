package io.github.alshain01.flags;

import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Defines a self-caching mechanism for areas.  Objects are created and cached upon first usage.
 */
public class CachedArea {
    private final Map<UUID, AreaDefault> defaultCache = new HashMap<UUID, AreaDefault>();
    private final Map<UUID, AreaWilderness> wildernessCache = new HashMap<UUID, AreaWilderness>();

    public AreaWilderness getWilderness(World world) {
        AreaWilderness area = wildernessCache.get(world.getUID());
        if(area == null) {
            area = new AreaWilderness(world);
            wildernessCache.put(world.getUID(), area);
        }
        return area;
    }

    public AreaDefault getDefault(World world) {
        AreaDefault area = defaultCache.get(world.getUID());
        if(area == null) {
            area = new AreaDefault(world);
            defaultCache.put(world.getUID(), area);
        }
        return area;
    }
}
