package fr.kyriog.rftd.listener;

import org.bukkit.Material;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import fr.kyriog.rftd.RftdController;

public class InventoryListener implements Listener {
	private RftdController controller;

	public InventoryListener(RftdController controller) {
		this.controller = controller;
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(controller.isPlaying()) {
			boolean move = e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY
					&& e.getCurrentItem().getType() == Material.EYE_OF_ENDER;
			boolean inventory = e.getRawSlot() < e.getInventory().getSize()
					&& e.getCursor().getType() == Material.EYE_OF_ENDER;

			if(move || inventory)
				e.setResult(Result.DENY);
		}
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent e) {
		if(controller.isPlaying()
				&& e.getOldCursor().getType() == Material.EYE_OF_ENDER) {
			int size = e.getInventory().getSize();
			for(int slot : e.getRawSlots()) {
				if(slot < size) {
					e.setResult(Result.DENY);
					break;
				}
			}
		}
	}
}
