/* Copyright 2013 Kevin Seiden. All rights reserved.

 This works is licensed under the Creative Commons Attribution-NonCommercial 3.0

 You are Free to:
    to Share: to copy, distribute and transmit the work
    to Remix: to adapt the work

 Under the following conditions:
    Attribution: You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
    Non-commercial: You may not use this work for commercial purposes.

 With the understanding that:
    Waiver: Any of the above conditions can be waived if you get permission from the copyright holder.
    Public Domain: Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
    Other Rights: In no way are any of the following rights affected by the license:
        Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
        The author's moral rights;
        Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.

 Notice: For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
 http://creativecommons.org/licenses/by-nc/3.0/
 */

package io.github.alshain01.flags.api.event;

import io.github.alshain01.flags.api.Flag;
import io.github.alshain01.flags.api.area.Area;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Event for that occurs when a message is set, changed, or removed.
 */
@SuppressWarnings("unused")
public class FlagMessageChangedEvent extends FlagEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private final String message;
	private boolean cancel = false;

	/**
	 * Creates a new FlagMessageChangedEvent
	 * 
	 * @param area
	 *            The area the message is being set for.
	 * @param message
	 *            The message being set
	 * @param flag
	 *            The flag the message is being set for
	 * @param sender
	 *            The sender changing the trust.
	 * 
	 */
	public FlagMessageChangedEvent(@Nonnull Area area, @Nonnull Flag flag, @Nullable String message, @Nullable CommandSender sender) {
		super(area, flag, sender);
		this.message = message;
	}

	/**
     * Gets the new message being set
     *
	 * @return the new message (null if being removed).
	 */
	public String getMessage() {
		return message;
	}

    /**
     * Static HandlerList for FlagMessageChangedEvent
     *
     * @return a list of event handlers, stored per-event.
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

	@Override
	public boolean isCancelled() {
		return cancel;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}
}
