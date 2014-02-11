package io.github.alshain01.flags.sector;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SectorListener implements Listener {
    final Material tool;

    public SectorListener(Material tool) {
        this.tool = tool;
    }

    @EventHandler
    private void onPlayerInteractEvent(PlayerInteractEvent e) {
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem().getType() != tool) { return; }


    }
}
