package fr.kyriog.rftd.listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import fr.kyriog.rftd.RftdController;

public class BlockListener implements Listener {
	private RftdController controller;

	public BlockListener(RftdController controller) {
		this.controller = controller;
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if(controller.isStarting())
			e.setCancelled(true);
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		Block placedBlock = e.getBlockPlaced();
		if(placedBlock.getType() == Material.DRAGON_EGG
				&& controller.isPlaying()) {
			Location eggLocation = placedBlock.getLocation();
			Location winLocation = controller.getEggLocation();
			if(eggLocation.getWorld() == winLocation.getWorld()
					&& eggLocation.getBlockX() == winLocation.getBlockX()
					&& eggLocation.getBlockY() == winLocation.getBlockY()
					&& eggLocation.getBlockZ() == winLocation.getBlockZ()) {
				controller.end(e.getPlayer());
			}
		}
	}

	@EventHandler
	public void onBlockFromTo(BlockFromToEvent e) {
		if(controller.isPlaying()
				&& e.getBlock().getType() == Material.DRAGON_EGG
				&& controller.isTrappedEgg()) {
			e.setCancelled(true);
			e.getBlock().setType(Material.AIR);
			controller.spawnDragon();
		}
	}
}
