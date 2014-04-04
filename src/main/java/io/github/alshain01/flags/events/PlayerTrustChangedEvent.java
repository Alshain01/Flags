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

package io.github.alshain01.flags.events;

import io.github.alshain01.flags.Flag;
import io.github.alshain01.flags.area.Area;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Event that occurs when a trustee is added or removed.
 */
public class PlayerTrustChangedEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final Area area;
	private final Flag flag;
	private final UUID trustee;
	private final CommandSender sender;
	private final boolean value;

	private boolean cancel = false;

	/**
	 * Creates a new PlayerTrustChangedEvent
	 * 
	 * @param area
	 *            The area the flag is being set for.
	 * @param trustee
	 *            The player the trust is changing for.
	 * @param isTrusted
	 *            True if the player is being added, false if being removed.
	 * @param sender
	 *            The sender changing the trust.
	 */
	public PlayerTrustChangedEvent(Area area, Flag flag, UUID trustee, boolean isTrusted, CommandSender sender) {
		this.area = area;
		this.flag = flag;
		this.trustee = trustee;
		this.sender = sender;
		value = isTrusted;
	}

    /**
     * Gets the area where the trust is changing
     *
     * @return The area associated with the event.
     */
	public Area getArea() {
		return area;
	}

    /**
     * Gets the flag that trust is changing for
     *
     * @return The flag associated with the event.
     */
	public Flag getFlag() {
		return flag;
	}


    /**
     * Gets the CommandSender requesting the trust change
     *
     * @return The CommandSender. Null if no sender involved (caused by plug-in).
     */
    @SuppressWarnings("unused") // API
	public CommandSender getSender() {
		return sender;
	}

	/**
     * Gets the player whos trust is changing
     *
	 * @return The UUID of the player
	 */
    @SuppressWarnings("unused") // API
	public UUID getTrustee() {
		return trustee;
	}

	/**
     * Gets whether the player is gaining or losing trust
     *
	 * @return True if the player is being added, false if being removed.
	 */
    @SuppressWarnings("unused") // API
	public boolean isTrusted() {
		return value;
	}

    /**
     * Static HandlerList for PlayerTrustChangedEvent
     *
     * @return A list of event handlers, stored per-event.
     */
    @SuppressWarnings("unused") // API
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