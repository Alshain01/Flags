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

import io.github.alshain01.flags.api.area.Area;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

/**
 * Event that occurs when a player first enters a new area.
 */
@SuppressWarnings("unused")
public class PlayerChangedAreaEvent extends AreaEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final Area exitArea;

	private boolean cancel = false;

	/**
	 * Creates a new PlayerChangedAreaEvent
	 * 
	 * @param player
	 *            The player involved with the event
	 * @param area
	 *            The area the player is entering
	 * @param areaLeft
	 *            The area the player is leaving
	 */
	public PlayerChangedAreaEvent(@Nonnull Player player, @Nonnull Area area, @Nonnull Area areaLeft) {
		super(area);
		this.player = player;
		exitArea = areaLeft;
	}

    /**
     * Gets the player changing areas
     *
     * @return the Player associated with the event.
     */
    public Player getPlayer() {
        return player;
    }

	/**
     * Gets the area the player is leaving.
     *
	 * @return the area the player left to enter this area.
	 */
	public Area getAreaLeft() {
		return exitArea;
	}

    /**
     * Static HandlerList for PlayerChangedAreaEvent
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
