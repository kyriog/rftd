package fr.kyriog.rftd;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Scoreboard;

public final class RftdHelper {
	public static Scoreboard getScoreboard() {
		return Bukkit.getScoreboardManager().getMainScoreboard();
	}

	public static ChatColor getColorFromString(String color) {
		String[] stringColors = { "black", "darkblue", "darkgreen", "darkaqua",
				"darkred", "darkpurple", "gold", "gray", "darkgray", "blue", "green",
				"aqua", "red", "lightpurple", "yellow", "white"
		};
		ChatColor[] chatColors = { ChatColor.BLACK, ChatColor.DARK_BLUE, ChatColor.DARK_GREEN,
				ChatColor.DARK_AQUA, ChatColor.DARK_RED, ChatColor.DARK_PURPLE, ChatColor.GOLD,
				ChatColor.GRAY, ChatColor.DARK_GRAY, ChatColor.BLUE, ChatColor.GREEN, ChatColor.AQUA,
				ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.YELLOW, ChatColor.WHITE
		};

		HashMap<String, ChatColor> map = new HashMap<String, ChatColor>();
		for(int i = 0; i < stringColors.length; i++) {
			map.put(stringColors[i], chatColors[i]);
		}

		return map.get(color);
	}

	private RftdHelper() {}
}
