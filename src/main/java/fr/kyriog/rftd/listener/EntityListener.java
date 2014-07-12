package fr.kyriog.rftd.listener;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

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
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if(controller.getEpisode() == 1
				&& e.getDamager().getType() == EntityType.PLAYER)
			e.setCancelled(true);
	}
}
