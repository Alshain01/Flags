package io.github.alshain01.flags.api.event;

import io.github.alshain01.flags.api.sector.Sector;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * All Sector Events.
 */
public class SectorEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Sector sector;

    /**
     * Creates a new SectorEvent
     *
     * @param sector
     *            The sector being changed.
     */
    public SectorEvent(Sector sector) {
        this.sector = sector;
    }

    /**
     * Gets the sector being changed
     *
     * @return The sector associated with the event.
     */
    public Sector getSector() {
        return sector;
    }

    /**
     * Static HandlerList for SectorEvent
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
