package io.github.alshain01.flags.api.event;

import io.github.alshain01.flags.api.area.Area;
import org.bukkit.event.Event;

import javax.annotation.Nonnull;

/**
 * Superclass for all area based events.
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
    public AreaEvent(@Nonnull Area area) {
        this.area = area;
    }

    /**
     * Gets the area involved in the event
     *
     * @return the area associated with the event.
     */
    public Area getArea() {
        return area;
    }
}
