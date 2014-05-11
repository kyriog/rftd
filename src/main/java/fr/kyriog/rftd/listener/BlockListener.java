package fr.kyriog.rftd.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

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
}
