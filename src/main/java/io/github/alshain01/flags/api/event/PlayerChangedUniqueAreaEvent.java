package io.github.alshain01.flags.api.event;

import io.github.alshain01.flags.api.area.Area;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * Event that occurs when a player first enters a new area that is not an inheriting subdivision.
 */
@SuppressWarnings("unused")
public class PlayerChangedUniqueAreaEvent extends PlayerChangedAreaEvent{
    private static final HandlerList handlers = new HandlerList();

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

    /**
     * Static HandlerList for PlayerChangedAreaEvent
     *
     * @return A list of event handlers, stored per-event.
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
