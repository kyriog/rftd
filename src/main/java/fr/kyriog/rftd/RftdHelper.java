package fr.kyriog.rftd;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
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

	public static ChatColor getChatColorFromString(String color) {
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

	public static Color getColorFromString(String color) {
		ChatColor[] chatColors = { ChatColor.BLACK, ChatColor.DARK_BLUE, ChatColor.DARK_GREEN,
				ChatColor.DARK_AQUA, ChatColor.DARK_RED, ChatColor.DARK_PURPLE, ChatColor.GOLD,
				ChatColor.GRAY, ChatColor.DARK_GRAY, ChatColor.BLUE, ChatColor.GREEN,
				ChatColor.AQUA, ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.YELLOW,
				ChatColor.WHITE
		};
		Color[] colors = { Color.fromRGB(0x000000), Color.fromRGB(0x0000AA), Color.fromRGB(0x00AA00),
				Color.fromRGB(0x00AAAA), Color.fromRGB(0xAA0000), Color.fromRGB(0xAA00AA), Color.fromRGB(0xFFAA00),
				Color.fromRGB(0xAAAAAA), Color.fromRGB(0x555555), Color.fromRGB(0x5555FF), Color.fromRGB(0x55FF55),
				Color.fromRGB(0x55FFFF), Color.fromRGB(0xFF5555), Color.fromRGB(0xFF55FF), Color.fromRGB(0xFFFF55),
				Color.fromRGB(0xFFFFFF)
		};

		ChatColor chatColor = ChatColor.getByChar(color.substring(1));
		if(chatColor == null)
			return null;

		HashMap<ChatColor, Color> map = new HashMap<ChatColor, Color>();
		for(int i = 0; i < chatColors.length; i++ ) {
			map.put(chatColors[i], colors[i]);
		}

		return map.get(chatColor);
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
		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
		for(Player player : players) {
			for(PotionEffect effect : effects)
				player.addPotionEffect(effect);
		}
	}

	public static void canEveryoneWalk(boolean walk) {
		float walkspeed = walk ? RftdHelper.DEFAULT_WALKSPEED : 0;
		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
		for(Player player : players) {
			player.setWalkSpeed(walkspeed);
		}
	}

	public static void setEveryoneGameMode(GameMode gamemode) {
		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
		for(Player player : players) {
			player.setGameMode(gamemode);
		}
	}

	public static void setDifficulty(Difficulty difficulty) {
		List<World> worlds = Bukkit.getWorlds();
		for(World world : worlds) {
			world.setDifficulty(difficulty);
		}
	}

	private RftdHelper() {}
}
