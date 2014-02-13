package io.github.alshain01.flags.sector;

import io.github.alshain01.flags.Flags;
import io.github.alshain01.flags.Message;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SectorListener implements Listener {
    final Material tool;
    final Map<UUID, Location> createQueue = new HashMap<UUID, Location>();

    public SectorListener(Material tool) {
        this.tool = tool;
    }

    @EventHandler
    private void onPlayerInteractEvent(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK
                || e.getItem().getType() != tool
                || !player.hasPermission("flags.sector.create")) { return; }

        Location corner1 = e.getClickedBlock().getLocation();
        if(createQueue.containsKey(player.getUniqueId())) {
            // Process the second corner and final creation

            SectorManager sectors = Flags.getSectorManager();
            Location corner2 = createQueue.get(player.getUniqueId());

            if(sectors.isOverlap(createQueue.get(player.getUniqueId()), e.getClickedBlock().getLocation())) {
                UUID parent = sectors.isContained(createQueue.get(player.getUniqueId()), e.getClickedBlock().getLocation());
                if(parent == null || sectors.get(parent).getParentID() != null) {
                    // Sector is only partially inside another or is inside another subdivison
                    player.sendMessage(Message.SectorOverlapError.get());
                    return;
                }
                // Create Subdivision
                player.sendMessage(Message.SubsectorCreated.get());
                createQueue.remove(player.getUniqueId());
                sectors.add(corner1, corner2, parent);
                return;
            }
            // Create Parent Sector
            player.sendMessage(Message.SectorCreated.get());
            createQueue.remove(player.getUniqueId());
            sectors.add(corner1, corner2);
            return;
        }

        //Process the first corner
        player.sendMessage(Message.SectorStarted.get());
        createQueue.put(player.getUniqueId(), corner1);
    }

    @EventHandler
    private void onPlayerPlayerItemHeld(PlayerItemHeldEvent e) {
        Player player = e.getPlayer();
        ItemStack prevSlot = player.getInventory().getItem(e.getPreviousSlot());
        ItemStack newSlot = player.getInventory().getItem(e.getNewSlot());
        if(prevSlot != null && prevSlot.getType() == tool
                && (newSlot == null || newSlot.getType() != tool)
                && createQueue.containsKey(player.getUniqueId())) {
                    createQueue.remove(player.getUniqueId());
                    player.sendMessage(Message.CancelCreateSector.get());
        }
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent e) {
        createQueue.remove(e.getPlayer().getUniqueId());
    }
}
