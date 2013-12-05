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

package io.github.alshain01.Flags.events;

import io.github.alshain01.Flags.Flag;
import io.github.alshain01.Flags.area.Area;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event for that occurs when a message is set, changed or removed.
 * 
 * @author Alshain01
 */
public class MessageChangedEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	/**
	 * Static HandlerList for FlagSetEvent
	 * 
	 * @return A list of event handlers, stored per-event.
	 *         Based on lahwran's fevents
	 */
	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final Area area;
	private final Flag flag;
	private final String message;
	private final CommandSender sender;

	private boolean cancel = false;

	/**
	 * Class Constructor
	 * 
	 * @param area
	 *            The area the flag is being set for.
	 * @param message
	 *            The message the be set
	 * @param flag
	 *            The flag the message is being set for
	 * @param sender
	 *            The sender changing the trust.
	 * 
	 */
	public MessageChangedEvent(Area area, Flag flag, String message, CommandSender sender) {
		this.area = area;
		this.flag = flag;
		this.message = message;
		this.sender = sender;
	}

	/**
	 * @return The area associated with the event.
	 */
	public Area getArea() {
		return area;
	}

	/**
	 * @return The flag type associated with the event.
	 */
	public Flag getFlag() {
		return flag;
	}

	/**
	 * HandlerList for FlagSetEvent
	 * 
	 * @return A list of event handlers, stored per-event. Based on lahwran's
	 *         fevents
	 */
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	/**
	 * @return The new message (null if being removed).
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return The CommandSender associated with the event. Null if no sender
	 *         involved (caused by plug-in).
	 * @deprecated Ambiguous name. Use {@link #getSender()} instead.
	 */
	@Deprecated
	public CommandSender getPlayer() {
		return getSender();
	}

	/**
	 * @return The CommandSender associated with the event. Null if no sender
	 *         involved (caused by plug-in).
	 */
	public CommandSender getSender() {
		return sender;
	}

	/**
	 * Gets the cancellation state of this event. A cancelled event will not be
	 * executed in the server, but will still pass to other plugins
	 * 
	 * @return true if this event is cancelled
	 */
	@Override
	public boolean isCancelled() {
		return cancel;
	}

	/**
	 * Sets the cancellation state of this event. A cancelled event will not be
	 * executed in the server, but will still pass to other plugins.
	 * 
	 * @param cancel
	 *            - true if you wish to cancel this event
	 */
	@Override
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}
}
