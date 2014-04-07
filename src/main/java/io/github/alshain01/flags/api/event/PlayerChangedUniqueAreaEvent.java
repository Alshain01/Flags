package io.github.alshain01.flags.api.event;

import io.github.alshain01.flags.api.area.Area;
import org.bukkit.entity.Player;

/**
 * Event that occurs when a player first enters a new area that is not an inheriting subdivision.
 */
public class PlayerChangedUniqueAreaEvent extends PlayerChangedAreaEvent{
    /**
     * Creates a new PlayerChangedUniqueAreaEvent
     *
     * @param player
     *            The player involved with the event
     * @param area
     *            The area the player is entering
     * @param areaLeft
     *            The area the player is leaving
     */
    public PlayerChangedUniqueAreaEvent(Player player, Area area, Area areaLeft) {
        super(player, area, areaLeft);
    }
}
