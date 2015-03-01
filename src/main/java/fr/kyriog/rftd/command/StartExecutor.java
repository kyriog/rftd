package fr.kyriog.rftd.command;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import fr.kyriog.rftd.RftdController;
import fr.kyriog.rftd.RftdLogger;
import fr.kyriog.rftd.RftdLogger.Level;

public class StartExecutor extends BaseAdminExecutor {
	private RftdController controller;

	public StartExecutor(RftdController controller) {
		this.controller = controller;
	}

	@Override
	public boolean onCommand(CommandSender commandSender, String[] args) {
		if(controller.isPlaying()) {
			String msg = "La partie a déjà commencée !";
			RftdLogger.log(Level.WARN, commandSender, msg);
			return true;
		}

		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
		Set<String> noTeamPlayers = new HashSet<String>();
		for(Player player : players) {
			Team team = scoreboard.getPlayerTeam(player);
			if(team == null)
				noTeamPlayers.add(player.getName());
		}

		if(noTeamPlayers.size() > 0) {
			String msg = "Au moins un joueur connecté ne fait pas partie d'une équipe :";
			RftdLogger.log(Level.ERROR, commandSender, msg);

			String playersNames = "";
			for(String playerName : noTeamPlayers) {
				playersNames += playerName + " ";
			}

			RftdLogger.log(Level.WARN, commandSender, playersNames);

			msg = "Pour ajouter un joueur à une équipe, utilisez le /team join <équipe> <joueur>";
			RftdLogger.log(Level.INFO, commandSender, msg);

			return true;
		}

		if(controller.getEggLocation() == null) {
			String msg = "L'endroit de dépôt de l'œuf de l'enderdragon n'a pas été défini !";
			RftdLogger.log(Level.ERROR, commandSender, msg);

			return true;
		}

		controller.start();
		return true;
	}
}
