package fr.kyriog.rftd;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;

public final class RftdHelper {
	public final static float DEFAULT_WALKSPEED = (float) 0.2;

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

	public static String blockLocationToString(Location location) {
		StringBuilder stringLocation = new StringBuilder();

		stringLocation.append(location.getWorld().getName() + ":");
		stringLocation.append(location.getBlockX() + ":");
		stringLocation.append(location.getBlockY() + ":");
		stringLocation.append(location.getBlockZ());

		return stringLocation.toString();
	}

	public static Location stringToBlockLocation(String stringLocation) {
		String[] locationArray = stringLocation.split(":");

		World world = Bukkit.getWorld(locationArray[0]);
		if(world == null)
			return null;

		int x = Integer.parseInt(locationArray[1]);
		int y = Integer.parseInt(locationArray[2]);
		int z = Integer.parseInt(locationArray[3]);

		return new Location(world, x, y, z);
	}

	public static void potionEveryone(PotionEffect... effects) {
		Player[] players = Bukkit.getOnlinePlayers();
		for(Player player : players) {
			for(PotionEffect effect : effects)
				player.addPotionEffect(effect);
		}
	}

	public static void canEveryoneWalk(boolean walk) {
		float walkspeed = walk ? RftdHelper.DEFAULT_WALKSPEED : 0;
		Player[] players = Bukkit.getOnlinePlayers();
		for(Player player : players) {
			player.setWalkSpeed(walkspeed);
		}
	}

	private RftdHelper() {}
}
