package fr.kyriog.rftd.listener;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;

import fr.kyriog.rftd.RftdController;

public class EntityListener implements Listener {
	private RftdController controller;

	public EntityListener(RftdController controller) {
		this.controller = controller;
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if(!controller.isPlaying() || controller.isStarting())
			e.setCancelled(true);
		else if(e.getEntityType() == EntityType.DROPPED_ITEM
				&& controller.isPlaying()) {
			Item item = (Item) e.getEntity();
			if(item.getItemStack().getType() == Material.DRAGON_EGG)
				controller.spawnTrappedEgg();
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if(controller.getEpisode() == 1
				&& e.getDamager().getType() == EntityType.PLAYER
				&& e.getEntity().getType() == EntityType.PLAYER)
			e.setCancelled(true);
	}

	@EventHandler
	public void onItemDespawn(ItemDespawnEvent e) {
		if(e.getEntity().getItemStack().getType() == Material.DRAGON_EGG
				&& controller.isPlaying())
			controller.spawnTrappedEgg();
	}

	@EventHandler
	public void onEntityCombust(EntityCombustEvent e) {
		if(e.getEntityType() == EntityType.DROPPED_ITEM
				&& controller.isPlaying()) {
			Item item = (Item) e.getEntity();
			if(item.getItemStack().getType() == Material.DRAGON_EGG)
				controller.spawnTrappedEgg();
		}
	}
}
