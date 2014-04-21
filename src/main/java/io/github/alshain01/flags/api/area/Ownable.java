package io.github.alshain01.flags.api.area;

import org.bukkit.OfflinePlayer;

import java.util.Collection;
import java.util.UUID;

/**
 * Interface that defines if the land system allows cuboids to be owned by players.
 */
@SuppressWarnings("unused")
public interface Ownable extends Area {
    /**
     * Gets a set of owners for the area.
     * On many systems, there will only be one.
     *
     * @return the area owners.
     * @throws io.github.alshain01.flags.api.exception.InvalidAreaException
     */
    public Collection<OfflinePlayer> getOwners();
}
