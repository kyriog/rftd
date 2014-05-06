package fr.kyriog.rftd.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public abstract class BaseAdminExecutor implements CommandExecutor {
	public abstract boolean onCommand(CommandSender commandSender, String[] args);

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
		if(!commandSender.isOp()) {
			String msg = "Vous n'avez pas accès à cette commande.";
			commandSender.sendMessage(ChatColor.RED + msg);
			return true;
		}

		return onCommand(commandSender, args);
	}
}
