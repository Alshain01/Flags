package io.github.alshain01.flags.api.event;

import io.github.alshain01.flags.api.area.Area;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event that handles all area events.
 */
@SuppressWarnings("unused, WeakerAccess")
public class AreaEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Area area;

    /**
     * Creates a new Area Event
     *
     * @param area
     *            The area involved in the event.
     */
    public AreaEvent(Area area) {
        this.area = area;
    }

    /**
     * Gets the area involved in the event
     *
     * @return The area associated with the event.
     */
    public Area getArea() {
        return area;
    }

    /**
     * Static HandlerList for AreaEvent
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
