package io.github.alshain01.flags.api.event;

import io.github.alshain01.flags.api.Flag;
import io.github.alshain01.flags.api.area.Area;
import org.bukkit.event.HandlerList;

public class FlagEvent extends AreaEvent {
    private static final HandlerList handlers = new HandlerList();
    private final Flag flag;

    /**
     * Creates a new Area Event
     *
     * @param area
     *            The area involved in the event.
     * @param flag
     *            The flag involved in the event.
     */
    public FlagEvent(Area area, Flag flag) {
        super(area);
        this.flag = flag;
    }

    /**
     * Gets the flag involved in the event
     *
     * @return The flag associated with the event.
     */
    public Flag getFlag() {
        return flag;
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
