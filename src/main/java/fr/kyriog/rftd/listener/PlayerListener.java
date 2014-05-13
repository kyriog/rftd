package fr.kyriog.rftd.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import fr.kyriog.rftd.RftdController;
import fr.kyriog.rftd.RftdHelper;

public class PlayerListener implements Listener {
	private RftdController controller;

	public PlayerListener(RftdController controller) {
		this.controller = controller;
	}

	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
		String format = "<%1$s" + ChatColor.WHITE + "> %2$s";
		e.setFormat(format);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();

		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Team team = scoreboard.getPlayerTeam(player);
		if(team != null)
			player.setDisplayName(team.getPrefix() + player.getName());

		float walkSpeed = controller.isStarting() ? 0 : RftdHelper.DEFAULT_WALKSPEED;
		player.setWalkSpeed(walkSpeed);

		GameMode gamemode;
		if(!controller.isPlaying())
			gamemode = GameMode.CREATIVE;
		else if(controller.isStarting())
			gamemode = GameMode.ADVENTURE;
		else
			gamemode = GameMode.SURVIVAL;
		player.setGameMode(gamemode);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if(controller.isStarting()) {
			boolean testX = e.getFrom().getX() != e.getTo().getX();
			boolean testZ = e.getFrom().getZ() != e.getTo().getZ();
			if(testX || testZ) {
				Location newLocation = e.getFrom();
				newLocation.setY(e.getTo().getY());
				newLocation.setDirection(e.getTo().getDirection());
				e.getPlayer().teleport(newLocation);
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if(!controller.isPlaying() &&
				e.getPlayer().getGameMode() != GameMode.CREATIVE &&
				e.getClickedBlock().getType() == Material.DRAGON_EGG) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		if(controller.isPlaying() &&
				e.getItemDrop().getItemStack().getType() == Material.EYE_OF_ENDER) {
			e.setCancelled(true);
		}
	}
}
