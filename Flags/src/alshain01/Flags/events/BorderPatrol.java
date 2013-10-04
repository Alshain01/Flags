package alshain01.Flags.events;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

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

import alshain01.Flags.Director;
import alshain01.Flags.Flags;
import alshain01.Flags.area.Area;

/**
 * Listener for handling Player Movement
 * 
 * @author Alshain01
 */
public class BorderPatrol implements Listener {
	private static final int eventsDivisor = Flags.instance.getConfig().getInt("Flags.BorderPatrol.EventDivisor");
	private static final int timeDivisor = Flags.instance.getConfig().getInt("Flags.BorderPatrol.TimeDivisor");
	private int eventCalls = 0;

	ConcurrentHashMap<String, PreviousMove> moveStore = new ConcurrentHashMap<String, PreviousMove>();
	
	private class PreviousMove {
		private long time;
		private Location location;
		private boolean ignore = false;
		
		private PreviousMove(Player player) {
			location = player.getLocation();
			time = 0;
			ignore = true;
		}
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onPlayerJoin(PlayerJoinEvent event) {
		if (moveStore.containsKey(event.getPlayer().getName())) {
			// Remove any garbage entries that may have been left behind
			// Probably won't happen, but just in case.
			moveStore.remove(event.getPlayer().getName());
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onPlayerQuit(PlayerQuitEvent event) {
		if (moveStore.containsKey(event.getPlayer().getName())) {
			// Remove the last location to keep memory usage low.
			moveStore.remove(event.getPlayer().getName());
		}
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onPlayerMove(PlayerMoveEvent e) {
		// Divide the number of events to prevent heavy event timing
		if (eventCalls++ > eventsDivisor) {
			eventCalls = 0;
			
			PreviousMove playerPrevMove = null;
			boolean process = false;
			
			String player = e.getPlayer().getName();
			if (!moveStore.containsKey(player)) {
				// New player data, process it immediately.
				process = true;
				moveStore.put(player, playerPrevMove = (new PreviousMove(e.getPlayer())));
			} else {
				// Use the old player data
				playerPrevMove = moveStore.get(player);
				
				// Check to see if we have processed this player recently.
				long timeDifferential = new Date().getTime() - playerPrevMove.time;
				if (timeDifferential > timeDivisor) { process = true; }
			}

			if (playerPrevMove.ignore || process) {
				// Acquire the area moving to and the area moving from.
				Area areaTo = Director.getAreaAt(e.getTo());
				Area areaFrom = Director.getAreaAt(playerPrevMove.location);
				
				// If they are the same area, don't bother.
				if (areaFrom.compareTo(areaTo) != 0) {
					playerPrevMove.ignore = false;
	
					// Call the event
					PlayerChangedAreaEvent event = new PlayerChangedAreaEvent(e.getPlayer(), areaTo, areaFrom);
					Bukkit.getServer().getPluginManager().callEvent(event);
					
					if(event.isCancelled()) {
						e.getPlayer().teleport(playerPrevMove.location, TeleportCause.PLUGIN);
					}
				}
				
				// Update the class instance
				playerPrevMove.time = new Date().getTime();
				playerPrevMove.location = e.getPlayer().getLocation();
			}
		}
	}
}