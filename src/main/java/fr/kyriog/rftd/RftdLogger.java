package fr.kyriog.rftd;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public final class RftdLogger {
	public enum Level {
		ERROR(ChatColor.RED),
		WARN(ChatColor.YELLOW),
		SUCCESS(ChatColor.GREEN),
		INFO(ChatColor.BLUE);

		private ChatColor color;

		Level(ChatColor color) {
			this.color = color;
		}

		public ChatColor getColor() {
			return color;
		}
	};

	public static void log(Level level, CommandSender commandSender, String log) {
		String message = generateMessage(level, log);

		if(commandSender != null && !(commandSender instanceof ConsoleCommandSender))
			commandSender.sendMessage(message);

		Logger logger = Bukkit.getLogger();
		message = ChatColor.stripColor(message);
		switch(level) {
		case ERROR:
			logger.severe(message);
			break;
		case WARN:
			logger.warning(message);
			break;
		case SUCCESS:
		case INFO:
			logger.info(message);
			break;
		}
	}

	public static void broadcast(Level level, String log) {
		String message = generateMessage(level, log);

		Bukkit.broadcastMessage(message);
	}

	public static void log(Level level, String log) {
		log(level, null, log);
	}

	private static String generateMessage(Level level, String log) {
		StringBuilder messageBuilder = new StringBuilder();
		messageBuilder.append("[");
		messageBuilder.append(level.getColor() + "RFTD");
		messageBuilder.append(ChatColor.WHITE + "] ");
		messageBuilder.append(ChatColor.GOLD + log);
		return messageBuilder.toString();
	}

	private RftdLogger() {}
}
