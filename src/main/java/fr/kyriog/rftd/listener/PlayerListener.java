package fr.kyriog.rftd.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import fr.kyriog.rftd.RftdController;
import fr.kyriog.rftd.RftdHelper;
import fr.kyriog.rftd.RftdLogger;
import fr.kyriog.rftd.RftdLogger.Level;

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
				e.getClickedBlock() != null &&
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

	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
		String[] command = e.getMessage().split(" ");
		if("/give".equalsIgnoreCase(command[0])) {
			Player sender = e.getPlayer();
			String player = command[1];
			String item = command[2].replace("minecraft:", "");
			int qty = command.length >= 4 ? Integer.valueOf(command[3]) : 1;

			StringBuilder msg = new StringBuilder();
			msg.append(sender.getName() + " effectue un give de ");
			if(qty > 1)
				msg.append(qty + "x ");
			msg.append(item + " à ");
			if(player.equalsIgnoreCase(sender.getName()))
				msg.append("lui-même");
			else
				msg.append(player);
			RftdLogger.broadcast(Level.INFO, msg.toString());
		} else if("/spawnpoint".equalsIgnoreCase(command[0])) {
			e.setCancelled(true);

			String error = "Vous ne pouvez pas utiliser /spawnpoint !";
			e.getPlayer().sendMessage(RftdLogger.generateMessage(Level.ERROR, error));
			String info = "Utilisez /setworldpoint en dehors d'une partie pour définir le point de spawn du monde.";
			e.getPlayer().sendMessage(RftdLogger.generateMessage(Level.INFO, info));
		}
	}
}
