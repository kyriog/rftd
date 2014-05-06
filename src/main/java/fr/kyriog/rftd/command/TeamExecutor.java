package fr.kyriog.rftd.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import fr.kyriog.rftd.RftdHelper;
import fr.kyriog.rftd.RftdLogger;
import fr.kyriog.rftd.RftdLogger.Level;

public class TeamExecutor extends BaseAdminExecutor {
	@Override
	public boolean onCommand(CommandSender commandSender, String[] args) {
		if(args.length < 1)
			return false;

		switch(args[0]) {
		case "add":
			executeAdd(commandSender, args);
			break;
		case "join":
			executeJoin(commandSender, args);
			break;
		}

		return true;
	}

	private void executeAdd(CommandSender commandSender, String[] args) {
		if(args.length < 3) {
			String msg = "Usage: /team add <color> <team name> [team display name]";
			commandSender.sendMessage(ChatColor.RED + msg);
			return;
		}

		ChatColor color = RftdHelper.getColorFromString(args[1]);
		if(color == null) {
			String msg = "Unable to found " + args[1] + " color";
			commandSender.sendMessage(ChatColor.RED + msg);
			return;
		}

		try {
			Scoreboard scoreboard = RftdHelper.getScoreboard();
			Team newTeam = scoreboard.registerNewTeam(args[2]);
			newTeam.setAllowFriendlyFire(true);
			newTeam.setPrefix(color + "");
			if(args.length > 3) {
				String teamName = "";
				for(int i = 3; i < args.length; i++) {
					teamName += args[i];
					if(i != args.length - 1)
						teamName += " ";
				}
				newTeam.setDisplayName(teamName);
			}

			StringBuilder message = new StringBuilder();
			message.append("La team ");
			message.append(color + args[2]);
			message.append(ChatColor.GOLD + " a été créée avec succès");

			RftdLogger.log(Level.SUCCESS, commandSender, message.toString());
		} catch(IllegalArgumentException e) {
			StringBuilder message = new StringBuilder();
			message.append("Une team portant le nom ");
			message.append(color + args[2]);
			message.append(ChatColor.GOLD + " existe déjà !");

			RftdLogger.log(Level.WARN, commandSender, message.toString());
		}
	}

	private void executeJoin(CommandSender commandSender, String[] args) {
		if(args.length != 3) {
			String msg = "Usage: /team join <team> <player>";
			commandSender.sendMessage(ChatColor.RED + msg);
			return;
		}

		Team team = RftdHelper.getScoreboard().getTeam(args[1]);
		if(team == null) {
			String msg = "La team " + args[1] + " n'existe pas !";
			commandSender.sendMessage(ChatColor.RED + msg);
			return;
		}

		Player playerToAdd = null;
		Player[] players = Bukkit.getOnlinePlayers();
		for(Player player : players) {
			if(args[2].equals(player.getName())) {
				playerToAdd = player;
				break;
			}
		}

		if(playerToAdd == null) {
			String msg = "Le joueur " + args[2] + " n'est pas connecté sur le serveur !";
			commandSender.sendMessage(ChatColor.RED + msg);
			return;
		}

		team.addPlayer(playerToAdd);
		playerToAdd.setDisplayName(team.getPrefix() + playerToAdd.getName());

		StringBuilder message = new StringBuilder();
		message.append(playerToAdd.getDisplayName());
		message.append(ChatColor.GOLD + " a été ajouté à la team ");
		message.append(team.getPrefix() + team.getDisplayName());
		RftdLogger.log(Level.SUCCESS, commandSender, message.toString());
	}
}
