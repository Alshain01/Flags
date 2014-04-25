package io.github.alshain01.flags.api.event;

import io.github.alshain01.flags.api.Flag;
import io.github.alshain01.flags.api.area.Area;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Superclass for all flag based events.
 */
public abstract class FlagEvent extends AreaEvent {
    private final Flag flag;
    private final CommandSender sender;

    /**
     * Creates a new Area Event
     *
     * @param area
     *            The area involved in the event.
     * @param flag
     *            The flag involved in the event.
     */
    public FlagEvent(@Nonnull Area area, @Nonnull Flag flag, @Nullable CommandSender sender) {
        super(area);
        this.flag = flag;
        this.sender = sender;
    }

    /**
     * Gets the flag involved in the event
     *
     * @return the flag associated with the event.
     */
    public Flag getFlag() {
        return flag;
    }

    /**
     * Gets the CommandSender requesting the flag change
     *
     * @return the CommandSender. Null if no sender involved (caused by plug-in).
     */
    public CommandSender getSender() {
        return sender;
    }
}
