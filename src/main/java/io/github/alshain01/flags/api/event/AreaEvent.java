package io.github.alshain01.flags.api.event;

import io.github.alshain01.flags.api.area.Area;
import org.bukkit.event.Event;

/**
 * Event that handles all area events.
 */
@SuppressWarnings("WeakerAccess")
public abstract class AreaEvent extends Event {
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
}
