package fr.kyriog.rftd.listener;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;

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
			if(item.getItemStack().getType() == Material.DRAGON_EGG
				&& controller.shouldListenEntityDamage()) {
				controller.lockListenEntityDamage();
				controller.spawnTrappedEgg();
			}
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
	public void onItemSpawn(ItemSpawnEvent e) {
		if(controller.isPlaying()
				&& e.getEntity().getItemStack().getType() == Material.DRAGON_EGG
				&& controller.isTrappedEgg()) {
			e.setCancelled(true);
			controller.spawnDragon();
		}
	}

	@EventHandler
	public void onEntityPortalEnter(EntityPortalEnterEvent e) {
		if(controller.isPlaying()
				&& e.getEntityType() == EntityType.PLAYER
				&& e.getLocation().getBlock().getType() == Material.ENDER_PORTAL) {
			controller.annouceEndTeleport();
		}
	}
}
