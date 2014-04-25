package io.github.alshain01.flags.api.event;

import io.github.alshain01.flags.api.sector.Sector;
import org.bukkit.event.Event;

import javax.annotation.Nonnull;

/**
 * All Sector Events.
 */
public abstract class SectorEvent extends Event {
    private final Sector sector;

    /**
     * Creates a new SectorEvent
     *
     * @param sector
     *            The sector being changed.
     */
    public SectorEvent(@Nonnull Sector sector) {
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
}
