package io.github.alshain01.flags.api.area;

import java.util.Set;
import java.util.UUID;

public interface Ownable extends Area {
    /**
     * Gets a set of owners for the area.
     * On many systems, there will only be one.
     *
     * @return the player UUID of the area owners.
     * @throws io.github.alshain01.flags.api.exception.InvalidAreaException
     * @deprecated NOT READY YET! DO NOT USE!
     */
    @Deprecated
    public Set<UUID> getOwnerUniqueId();

    /**
     * Gets a set of owner names for the area.
     * On many systems, there will only be one.
     *
     * @return the player name of the area owners.
     * @throws io.github.alshain01.flags.api.exception.InvalidAreaException
     */
    public Set<String> getOwnerName();
}
