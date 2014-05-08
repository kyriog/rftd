package fr.kyriog.rftd.command;

import org.bukkit.command.CommandSender;

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

		controller.start();
		return true;
	}
}
