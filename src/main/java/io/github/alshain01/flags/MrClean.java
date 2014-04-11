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

import io.github.alshain01.flags.api.CuboidPlugin;
import io.github.alshain01.flags.api.event.SectorDeleteEvent;
import me.ryanhamshire.GriefPrevention.events.ClaimDeletedEvent;
import me.tabinol.factoid.event.LandDeleteEvent;
import net.jzx7.regiosapi.events.RegionDeleteEvent;

import net.t00thpick1.residence.api.events.ResidenceAreaDeletedEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.massivecraft.factions.event.FactionsEventDisband;

/*
 * Class for removing data from areas that are deleted.
 */
final class MrClean {
	private MrClean() { }

    /*
     * Database cleanup monitors
     */
    static void enable(Plugin plugin, boolean enable, CuboidPlugin cuboidPlugin) {
        if(!enable) { return; }
        PluginManager pm = plugin.getServer().getPluginManager();
        switch (cuboidPlugin) {
            case GRIEF_PREVENTION:
                pm.registerEvents(new GriefPreventionCleaner(),	plugin);
                break;/*
            case RESIDENCE:
                pm.registerEvents(new ResidenceCleaner(), plugin);
                break;*/
            case FACTIONS:
                pm.registerEvents(new FactionsCleaner(), plugin);
                break;
            case REGIOS:
                pm.registerEvents(new RegiosCleaner(), plugin);
                break;
            case FLAGS:
                pm.registerEvents(new FlagsCleaner(), plugin);
                break;
            case FACTOID:
                pm.registerEvents(new FactoidCleaner(), plugin);
                break;
            default:
                break;
        }
    }

   /*
    * Flags Cleaner
    */
    private static class FlagsCleaner implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        private void onSectorDelete(SectorDeleteEvent e) {
            new AreaFlags(e.getSector().getID()).remove();
        }
    }

    /*
     * Factions Cleaner
     */
	private static class FactionsCleaner implements Listener {
		@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
		private void onFactionDisband(FactionsEventDisband e) {
			for (final org.bukkit.World world : Bukkit.getWorlds()) {
				new AreaFactions(world, e.getFaction().getId()).remove();
			}
		}
	}

    /*
     * Grief Prevention Cleaner
     */
	private static class GriefPreventionCleaner implements Listener {
		@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
		private void onClaimDeleted(ClaimDeletedEvent e) {
			// Cleanup the database, keep the file from growing too large.
            new AreaGriefPrevention(e.getClaim().getID()).remove();
		}
	}

    /*
     * Residence Cleaner
     */
	private static class ResidenceCleaner implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        private void onResidenceDelete(ResidenceAreaDeletedEvent e) {
            // Cleanup the database, keep the file from growing too large.
            new AreaResidence(e.getResidenceArea().getName()).remove();
        }
    }


    /*
     * Regios Cleaner
     */
	private static class RegiosCleaner implements Listener {
		@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
		private void onRegionDelete(RegionDeleteEvent e) {
			// Cleanup the database, keep the file from growing too large.
			new AreaRegios(e.getRegion().getName()).remove();
		}
	}

    /*
     * Factoid Cleaner
     */
    private static class FactoidCleaner implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        private void onRegionDelete(LandDeleteEvent e) {
            // Cleanup the database, keep the file from growing too large.
            new AreaFactoid(e.getLand().getUUID()).remove();
        }
    }
}
