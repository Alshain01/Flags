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

package io.github.alshain01.flags;

import io.github.alshain01.flags.api.FlagsAPI;
import io.github.alshain01.flags.api.area.Area;
import io.github.alshain01.flags.api.area.Area.AreaRelationship;
import io.github.alshain01.flags.api.area.Subdividable;
import io.github.alshain01.flags.api.event.PlayerChangedAreaEvent;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.github.alshain01.flags.api.event.PlayerChangedUniqueAreaEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

/**
 * Listener for handling Player Movement
 */
final class BorderPatrol implements Listener {
	/*
	 * Storage for the player's last known location
	 */
	private class PreviousMove {
		private long time;
		private Location location;

		private PreviousMove(Player player) {
			location = player.getLocation();
			time = 0;
		}

		private void update() {
			time = new Date().getTime();
		}

		private void update(Location location) {
			this.location = location;
			this.update();
		}
	}

	BorderPatrol(int eDivisor, int tDivisor) {
		eventsDivisor = eDivisor;
		timeDivisor = tDivisor;

        for(Player p : Bukkit.getServer().getOnlinePlayers()) {
            moveStore.put(p.getUniqueId(), new PreviousMove(p));
        }
	}
	
	private final int eventsDivisor;
	private final int timeDivisor;
	private final Map<UUID, PreviousMove> moveStore = new HashMap<UUID, PreviousMove>();
	private int eventCalls = 0;

	/*
	 * Initialize for a new player
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onPlayerJoin(PlayerJoinEvent event) {
        moveStore.put(event.getPlayer().getUniqueId(), new PreviousMove(event.getPlayer()));
	}

	/*
	 * Remove the last location to keep memory usage low.
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onPlayerQuit(PlayerQuitEvent event) {
		if (moveStore.containsKey(event.getPlayer().getUniqueId())) {
			moveStore.remove(event.getPlayer().getUniqueId());
        }
	}

	/*
	 * Monitor the player's movement
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onPlayerMove(PlayerMoveEvent e) {
		// Divide the number of events to prevent heavy event timing
		if (eventCalls++ <= eventsDivisor) {
			return;
		}

		eventCalls = 0;
		PreviousMove playerPrevMove;
		boolean process;

        // Use the old player data
        playerPrevMove = moveStore.get(e.getPlayer().getUniqueId());

        // Check to see if we have processed this player recently.
        process = new Date().getTime() - playerPrevMove.time > timeDivisor;

		if (process) {
			// Acquire the area moving to and the area moving from.
			final Area areaTo = FlagsAPI.getAreaAt(e.getTo());
			final Area areaFrom = FlagsAPI.getAreaAt(playerPrevMove.location);

			// If they are the same area, don't bother.
			if (areaFrom.getRelationship(areaTo) != AreaRelationship.EQUAL) {
                PlayerChangedAreaEvent event;
                if((areaFrom.getRelationship(areaTo) == AreaRelationship.SUBDIVISION && ((Subdividable)areaFrom).isInherited()) //AreaFrom is a subdivision of AreaTo and is inheriting
                        || (areaFrom.getRelationship(areaTo) == AreaRelationship.PARENT && ((Subdividable)areaTo).isInherited())) { //AreaTo is a subdivision of AreaFrom and is inheriting
                    //If there is a subdivsion and it is inheriting, call the parent event only.
                    event = new PlayerChangedAreaEvent(e.getPlayer(), areaTo, areaFrom);
                } else {
				    // If it's not an inheriting subdivision, call the subclass event and the parent event
				    event = new PlayerChangedUniqueAreaEvent(e.getPlayer(), areaTo, areaFrom);
                }
                Bukkit.getServer().getPluginManager().callEvent(event);

				if (event.isCancelled()) {
					e.getPlayer().teleport(playerPrevMove.location,	TeleportCause.PLUGIN);
					playerPrevMove.update();
					return;
				}
			}

			// Update the class instance
			playerPrevMove.update(e.getPlayer().getLocation());
		}
	}
}