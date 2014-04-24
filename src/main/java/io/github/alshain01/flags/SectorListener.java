package io.github.alshain01.flags;

import io.github.alshain01.flags.api.FlagsAPI;
import io.github.alshain01.flags.api.event.SectorCreateEvent;
import io.github.alshain01.flags.api.sector.SectorManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class SectorListener implements Listener {
    private final Material tool;
    private final Map<UUID, Location> createQueue = new HashMap<UUID, Location>();

    SectorListener(Material tool) {
        this.tool = tool;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerInteractEvent(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        // Verify this is a sector tool creation
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK
                || e.getItem() == null
                || e.getItem().getType() != tool
                || !player.hasPermission("flags.sector.create"))
            return;

        Location corner1 = e.getClickedBlock().getLocation();

        if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(createQueue.containsKey(player.getUniqueId())) {
                // Process the second corner and final creation
                SectorManager sectors = FlagsAPI.getSectorManager();
                Location corner2 = createQueue.get(player.getUniqueId());

                if (sectors.isOverlap(corner1, corner2)) {
                    UUID parent = sectors.isContained(corner1, corner2);
                    if (parent == null || sectors.get(parent).getParentID() != null) {
                        // Sector is only partially inside another or is inside another subdivison
                        player.sendMessage(Message.SECTOR_OVERLAP_ERROR.get());
                        return;
                    }
                    // Create Subdivision
                    player.sendMessage(Message.SUBSECTOR_CREATED.get());
                    Bukkit.getPluginManager().callEvent(new SectorCreateEvent(sectors.add(corner1, corner2, parent)));
                } else {
                    // Create Parent Sector
                    player.sendMessage(Message.SECTOR_CREATED.get());
                    Bukkit.getPluginManager().callEvent(new SectorCreateEvent(sectors.add(corner1, corner2)));
                }
                createQueue.remove(player.getUniqueId());
                e.setCancelled(true);
            } else {
                //Process the first corner
                player.sendMessage(Message.SECTOR_STARTED.get());
                createQueue.put(player.getUniqueId(), corner1);
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerPlayerItemHeld(PlayerItemHeldEvent e) {
        Player player = e.getPlayer();
        ItemStack prevSlot = player.getInventory().getItem(e.getPreviousSlot());
        ItemStack newSlot = player.getInventory().getItem(e.getNewSlot());
        if(prevSlot != null && prevSlot.getType() == tool
                && (newSlot == null || newSlot.getType() != tool)
                && createQueue.containsKey(player.getUniqueId())) {
                    createQueue.remove(player.getUniqueId());
                    player.sendMessage(Message.CANCEL_CREATE_SECTOR.get());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerQuit(PlayerQuitEvent e) {
        createQueue.remove(e.getPlayer().getUniqueId());
    }
}
