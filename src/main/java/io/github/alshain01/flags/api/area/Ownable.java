package io.github.alshain01.flags.api.area;

import org.bukkit.OfflinePlayer;

import java.util.Collection;
import io.github.alshain01.flags.api.exception.InvalidAreaException;
/**
 * Defines if the area plugin allows areas to be owned by players.
 */
@SuppressWarnings("unused")
public interface Ownable extends Area {
    /**
     * Gets a set of owners for the area.
     * On many systems, there will only be one.
     *
     * @return the area owners
     * @throws InvalidAreaException
     */
    public Collection<OfflinePlayer> getOwners();
}
