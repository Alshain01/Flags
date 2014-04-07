package io.github.alshain01.flags;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

class NewUpdateFoundEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public NewUpdateFoundEvent() {
        super(true);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
