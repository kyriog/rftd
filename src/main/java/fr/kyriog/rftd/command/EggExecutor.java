package fr.kyriog.rftd.command;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.kyriog.rftd.RftdController;
import fr.kyriog.rftd.RftdLogger;
import fr.kyriog.rftd.RftdLogger.Level;

public class EggExecutor extends BaseAdminExecutor {
	private RftdController controller;

	public EggExecutor(RftdController controller) {
		this.controller = controller;
	}

	@Override
	public boolean onCommand(CommandSender commandSender, String[] args) {
		if(!(commandSender instanceof Player)) {
			String msg = "Seuls les joueurs sont autorisés à effectuer cette commande";
			commandSender.sendMessage(ChatColor.RED + msg);
			return true;
		}

		Player player = (Player) commandSender;
		Location eggLocation = player.getLocation();
		controller.setEggLocation(eggLocation);

		StringBuilder message = new StringBuilder();
		message.append("Dépôt de l'œuf de l'enderdragon défini à ");
		message.append(ChatColor.GREEN + "(");
		message.append(eggLocation.getBlockX() + ", ");
		message.append(eggLocation.getBlockY() + ", ");
		message.append(eggLocation.getBlockZ() + ")");

		RftdLogger.log(Level.SUCCESS, player, message.toString());
		return true;
	}
}
