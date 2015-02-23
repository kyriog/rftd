package fr.kyriog.rftd.command;

import fr.kyriog.rftd.RftdController;
import fr.kyriog.rftd.RftdLogger;
import fr.kyriog.rftd.RftdLogger.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnpointExecutor extends BaseAdminExecutor {
	private RftdController controller;

	public SpawnpointExecutor(RftdController controller) {
		this.controller = controller;
	}

	@Override
	public boolean onCommand(CommandSender commandSender, String[] args) {
		if(controller.isPlaying()) {
			String msg = "Vous ne pouvez pas redéfinir le point de spawn en cours de partie.";
			commandSender.sendMessage(ChatColor.RED + msg);
			return true;
		}

		int x = 0, y = 0, z = 0;
		World world = null;
		if(args.length == 0) {
			if(commandSender instanceof Player) {
				Player player = (Player) commandSender;
				world = player.getWorld();
				Location location = player.getLocation();
				x = location.getBlockX();
				y = location.getBlockY() + 1;
				z = location.getBlockZ();
				world.setSpawnLocation(x, y, z);

			} else {
				String msg = "Vous devez être un joueur pour exécuter cette commande sans préciser de coordonnées.";
				commandSender.sendMessage(ChatColor.RED + msg);
			}
		} else if(args.length == 3) {
			try {
				x = Integer.valueOf(args[0]);
				y = Integer.valueOf(args[1]) + 1;
				z = Integer.valueOf(args[2]);

				if(commandSender instanceof Player) {
					Player player = (Player) commandSender;
					world = player.getWorld();
				} else {
					world = Bukkit.getWorlds().get(0);
				}

				world.setSpawnLocation(x, y, z);
			} catch(NumberFormatException e) {
				String msg = "Les coordonnées indiquées semblent incorrectes";
				commandSender.sendMessage(ChatColor.RED + msg);
				return true;
			}
		} else {
			return false;
		}

		StringBuilder message = new StringBuilder();
		message.append("Spawn défini à ");
		message.append(ChatColor.GREEN + "(");
		message.append(x + ", ");
		message.append(y + ", ");
		message.append(z + ")");
		message.append(ChatColor.GOLD + " pour le monde ");
		message.append(ChatColor.GREEN + world.getName());
		RftdLogger.log(Level.SUCCESS, commandSender, message.toString());

		return true;
	}
}
