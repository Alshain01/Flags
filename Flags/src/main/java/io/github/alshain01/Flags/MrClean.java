package io.github.alshain01.Flags;

import io.github.alshain01.Flags.area.FactionsTerritory;
import io.github.alshain01.Flags.area.GriefPreventionClaim78;
import io.github.alshain01.Flags.area.RegiosRegion;
import io.github.alshain01.Flags.area.ResidenceClaimedResidence;
import me.ryanhamshire.GriefPrevention.events.ClaimDeletedEvent;
import net.jzx7.regiosapi.events.RegionDeleteEvent;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent;
import com.massivecraft.factions.event.FactionsEventDisband;

public class MrClean {
	private MrClean() { }
	
	private static class FactionsCleaner implements Listener {
		@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
		private void onFactionDisband(FactionsEventDisband e) {
			for (final org.bukkit.World world : Bukkit.getWorlds()) {
				new FactionsTerritory(world, e.getFaction().getId()).remove();
			}
		}
	}

	private static class GriefPreventionCleaner implements Listener {
		@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
		private void onClaimDeleted(ClaimDeletedEvent e) {
			// Cleanup the database, keep the file from growing too large.
			new GriefPreventionClaim78(e.getClaim().getID()).remove();
		}
	}

	private static class ResidenceCleaner implements Listener {
		@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
		private void onResidenceDelete(ResidenceDeleteEvent e) {
			// Cleanup the database, keep the file from growing too large.
			new ResidenceClaimedResidence(e.getResidence().getName()).remove();
		}
	}

	private static class RegiosCleaner implements Listener {
		@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
		private void onRegionDelete(RegionDeleteEvent e) {
			// Cleanup the database, keep the file from growing too large.
			new RegiosRegion(e.getRegion().getName()).remove();
		}
	}
	
	/*
	 * Database cleanup monitors
	 */
	protected static void enable(PluginManager pm) {
		switch (SystemType.getActive()) {
		case GRIEF_PREVENTION:
			if (Float.valueOf(pm.getPlugin(SystemType.getActive().toString())
					.getDescription().getVersion().substring(0, 3)) >= 7.8) {

				pm.registerEvents(new GriefPreventionCleaner(),	Flags.getInstance());
			}
			break;
		case RESIDENCE:
			pm.registerEvents(new ResidenceCleaner(), Flags.getInstance());
			break;
		case FACTIONS:
			pm.registerEvents(new FactionsCleaner(), Flags.getInstance());
			break;
		case REGIOS:
			pm.registerEvents(new RegiosCleaner(), Flags.getInstance());
		default:
			break;
		}
	}
}
