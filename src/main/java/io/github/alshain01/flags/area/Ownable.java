package io.github.alshain01.flags.area;

import java.util.Set;
import java.util.UUID;

public interface Ownable {
    /**
     * Gets a set of owners for the area.
     * On many systems, there will only be one.
     *
     * @return the player UUID of the area owners.
     * @throws io.github.alshain01.flags.exception.InvalidAreaException
     * @deprecated NOT READY YET! DO NOT USE!
     */
    @Deprecated
    public abstract Set<UUID> getOwnerUniqueId();

    /**
     * Gets a set of owner names for the area.
     * On many systems, there will only be one.
     *
     * @return the player name of the area owners.
     * @throws io.github.alshain01.flags.exception.InvalidAreaException
     */
    public abstract Set<String> getOwnerName();
}
