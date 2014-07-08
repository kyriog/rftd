package fr.kyriog.rftd;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import fr.kyriog.rftd.RftdLogger.Level;

public class RftdController {
	private RftdPlugin plugin;
	private BukkitTask task;
	private Location eggLocation;
	private boolean playing = false;
	private boolean starting = false;
	private int episode = 0;
	private boolean trappedEgg = false;

	public RftdController(RftdPlugin plugin) {
		this.plugin = plugin;
	}

	public int getEpisode() {
		return episode;
	}

	public void setEggLocation(Location egg) {
		World world = egg.getWorld();
		int x = egg.getBlockX();
		int y = egg.getBlockY();
		int z = egg.getBlockZ();

		Location simpleEggLocation = new Location(world, x, y, z);
		this.eggLocation = simpleEggLocation;

		world.setSpawnLocation(x, y+2, z);

		String eggLocationString = RftdHelper.blockLocationToString(simpleEggLocation);
		setConfig("egg", eggLocationString);
	}

	public Location getEggLocation() {
		return eggLocation;
	}

	public boolean isPlaying() {
		return playing;
	}

	public boolean isStarting() {
		return starting;
	}

	public boolean isTrappedEgg() {
		return trappedEgg;
	}

	public void onEnable() {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
		if(objective != null)
			objective.unregister();

		String eggLocationString = plugin.getConfig().getString("egg");
		if(eggLocationString != null) {
			Location eggLocation = RftdHelper.stringToBlockLocation(eggLocationString);
			setEggLocation(eggLocation);
		}

		for(World world : Bukkit.getWorlds()) {
			world.setDifficulty(Difficulty.PEACEFUL);
			world.setFullTime(6000);
			world.setGameRuleValue("doDaylightCycle", "false");
		}
	}

	public void start() {
		playing = true;
		starting = true;

		String msg = "Tout le monde est prêt ? On ne bouge plus !";
		RftdLogger.broadcast(Level.SUCCESS, msg);

		RftdHelper.setDifficulty(Difficulty.HARD);
		RftdHelper.canEveryoneWalk(false);
		RftdHelper.setEveryoneGameMode(GameMode.ADVENTURE);

		Player[] players = Bukkit.getOnlinePlayers();
		for(Player player : players) {
			PlayerInventory inventory = player.getInventory();
			inventory.clear();
			inventory.setHelmet(null);
			inventory.setChestplate(null);
			inventory.setLeggings(null);
			inventory.setBoots(null);

			ItemStack endereyes = new ItemStack(Material.EYE_OF_ENDER, getEyeOfEnderQty());
			inventory.setItem(4, endereyes);
		}

		task = Bukkit.getScheduler().runTaskTimer(plugin, new StartTimer(), 20, 20);
	}

	private void freePlayers() {
		starting = false;
		task.cancel();

		Player[] players = Bukkit.getOnlinePlayers();
		for(Player player : players) {
			player.setHealth(20);
			player.setFoodLevel(20);
			player.setSaturation(20);
			player.setExp(0);
			player.setLevel(0);
		}

		RftdHelper.canEveryoneWalk(true);
		RftdHelper.setEveryoneGameMode(GameMode.SURVIVAL);

		for(World world : Bukkit.getWorlds()) {
			world.setFullTime(0);
			world.setGameRuleValue("doDaylightCycle", "true");
		}

		episode = 1;
		task = Bukkit.getScheduler().runTaskTimer(plugin, new ScoreboardTask(), 0, 20);
	}

	public void spawnTrappedEgg() {
		eggLocation.getBlock().setType(Material.DRAGON_EGG);

		// Spawning fireworks twice because it doesn't always works on the first try…
		for(int i = 0; i < 2; i++) {
			Firework fw = (Firework) eggLocation.getWorld()
					.spawnEntity(eggLocation.clone().add(0.5, 1, 0.5), EntityType.FIREWORK);

			FireworkMeta meta = fw.getFireworkMeta();
			Builder builder = FireworkEffect.builder()
					.with(Type.BALL)
					.withColor(Color.fromRGB(0x08080c));
			meta.addEffect(builder.build());
			builder = FireworkEffect.builder()
					.with(Type.BALL)
					.withColor(Color.fromRGB(0x2d0133));
			meta.addEffect(builder.build());
			meta.setPower(1);

			fw.setFireworkMeta(meta);
			fw.detonate();
		}

		for(Player player : Bukkit.getOnlinePlayers())
			player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 1);

		StringBuilder msg = new StringBuilder();
		msg.append("[");
		msg.append(ChatColor.DARK_PURPLE + "ENDERDRAGON");
		msg.append(ChatColor.WHITE + "] ");
		msg.append(ChatColor.RED + "Vous pensiez vous être débarrassé de moi ?");
		Bukkit.broadcastMessage(msg.toString());

		msg = new StringBuilder();
		msg.append("[");
		msg.append(ChatColor.DARK_PURPLE + "ENDERDRAGON");
		msg.append(ChatColor.WHITE + "] ");
		msg.append(ChatColor.DARK_RED + "Je vous attends au spawn...");
		Bukkit.broadcastMessage(msg.toString());

		String msgHelp = "Noobs : le spawn se trouve en x=" + eggLocation.getBlockX() + ", z=" + eggLocation.getBlockZ();
		RftdLogger.broadcast(Level.INFO, msgHelp);

		trappedEgg = true;
	}

	public void spawnDragon() {
		trappedEgg = false;
		System.out.println("Spawning dragon");
	}

	public void end(Player winner) {
		task.cancel();

		RftdHelper.setDifficulty(Difficulty.PEACEFUL);

		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Team team = scoreboard.getPlayerTeam(winner);

		String win = "C'est fini ! L'œuf a été déposé !";
		RftdLogger.broadcast(Level.SUCCESS, win);

		StringBuilder msg = new StringBuilder();
		msg.append("L'équipe ");
		msg.append(team.getPrefix() + team.getDisplayName());
		msg.append(ChatColor.GOLD + " remporte la victoire.");
		RftdLogger.broadcast(Level.SUCCESS, msg.toString());

		msg = new StringBuilder();
		msg.append("Félicitations à ");

		OfflinePlayer[] winnersPlayers = team.getPlayers().toArray(new OfflinePlayer[0]);
		for(int i = 0; i < winnersPlayers.length; i++) {
			msg.append(winnersPlayers[i].getName());

			if(i == winnersPlayers.length - 2)
				msg.append(" et ");
			else if(i < winnersPlayers.length - 2)
				msg.append(", ");
		}
		msg.append(" !");
		RftdLogger.broadcast(Level.SUCCESS, msg.toString());

		Player[] players = Bukkit.getOnlinePlayers();
		for(Player player : players) {
			if(player != winner)
				player.teleport(eggLocation.getWorld().getSpawnLocation());
		}

		BukkitScheduler scheduler = Bukkit.getScheduler();

		FireworkTask task = new FireworkTask();
		task.addEffect(Type.BALL_LARGE, Color.fromRGB(0x08080c), false);
		task.addEffect(Type.BALL_LARGE, Color.fromRGB(0x2d0133), false);
		scheduler.runTaskLater(plugin, task, 20);

		Color color = RftdHelper.getColorFromString(team.getPrefix());

		task = new FireworkTask();
		for(Team playingTeam : scoreboard.getTeams()) {
			if(playingTeam != team) {
				Color playingColor = RftdHelper.getColorFromString(playingTeam.getPrefix());
				task.addEffect(Type.BALL, playingColor, false);
			}
		}
		task.addEffect(Type.BALL_LARGE, color, true);
		scheduler.runTaskLater(plugin, task, 60);

		task = new FireworkTask();
		task.addEffect(Type.BALL, Color.fromRGB(0x08080c), false);
		task.addEffect(Type.BALL, Color.fromRGB(0x2d0133), false);
		task.addEffect(Type.STAR, color, true);
		scheduler.runTaskLater(plugin, task, 120);

		task = new FireworkTask();
		task.addEffect(Type.CREEPER, color, false);
		scheduler.runTaskLater(plugin, task, 180);

		playing = false;
	}

	private int getEyeOfEnderQty() {
		return plugin.getConfig().getInt("eyeQty", 1);
	}

	private int getEpisodeLength() {
		return plugin.getConfig().getInt("episodeLength", 20);
	}

	private int getSeason() {
		return plugin.getConfig().getInt("season", 0);
	}

	private void setConfig(String path, Object value) {
		plugin.getConfig().set(path, value);
		plugin.saveConfig();
	}

	private class StartTimer implements Runnable {
		private int timer = 5;

		@Override
		public void run() {
			if(timer == 5) {
				String msg = "Démarrage de la partie dans";
				RftdLogger.broadcast(Level.INFO, msg);
			}

			if(timer != 0) {
				RftdLogger.broadcast(Level.INFO, String.valueOf(timer));
				if(timer <= 3) {
					playSoundForAllPlayers(Sound.NOTE_PIANO, 10, 0.5);
				}
			} else {
				String msg = "Bonne chance à tous !";
				RftdLogger.broadcast(Level.SUCCESS, msg);
				playSoundForAllPlayers(Sound.NOTE_PIANO, 10, 1);
				freePlayers();
			}

			timer--;
		}

		private void playSoundForAllPlayers(Sound sound, float volume, double pitch) {
			Player[] players = Bukkit.getOnlinePlayers();
			for(Player player : players) {
				player.playSound(player.getLocation(), sound, volume, (float) pitch);
			}
		}
	}

	private class ScoreboardTask implements Runnable {
		public final static String objectiveName = "RFTD";

		private int minutesLeft = -1;
		private int secondsLeft = 1;

		@Override
		public void run() {
			secondsLeft--;
			if(minutesLeft == 0 && secondsLeft == 0) {
				minutesLeft = -1;
				secondsLeft = 10;
				String msg = "Fin de l'épisode " + episode;
				RftdLogger.broadcast(Level.INFO, msg);
				episode++;
			} else if(minutesLeft == -1 && secondsLeft == 0) {
				minutesLeft = getEpisodeLength();
				secondsLeft = 0;
				if(episode != 1) {
					String msg = "Début de l'épisode " + episode;
					RftdLogger.broadcast(Level.SUCCESS, msg);
				}
			} else if(secondsLeft == -1) {
				minutesLeft--;
				secondsLeft = 59;
			}

			Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
			Objective objective = scoreboard.getObjective(objectiveName);
			if(objective != null)
				objective.unregister();

			objective = scoreboard.registerNewObjective(objectiveName, "dummy");
			objective.getScore("Saison " + getSeason()).setScore(3);
			objective.getScore("Episode " + episode).setScore(2);
			objective.getScore("").setScore(1);

			StringBuilder time = new StringBuilder();
			if(secondsLeft <= 10) {
				if(minutesLeft == -1)
					time.append(ChatColor.RED + "");
				else if(minutesLeft == 0)
					time.append(ChatColor.GOLD + "");
			}
			if(minutesLeft == -1)
				time.append("-00");
			else
				time.append(" " + String.format("%02d", minutesLeft));
			time.append(":" + String.format("%02d", secondsLeft));
			objective.getScore(time.toString()).setScore(0);

			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		}
	}

	private class FireworkTask implements Runnable {
		private List<FireworkEffect> effects = new ArrayList<FireworkEffect>();

		public void addEffect(Type type, Color color, boolean withFlicker) {
			Builder builder = FireworkEffect.builder()
					.with(type)
					.withColor(color);
			if(withFlicker)
				builder = builder.withFlicker();
			effects.add(builder.build());
		}

		@Override
		public void run() {
			Firework fw = (Firework) eggLocation.getWorld()
					.spawnEntity(eggLocation, EntityType.FIREWORK);

			FireworkMeta meta = fw.getFireworkMeta();
			for(FireworkEffect effect : effects)
				meta.addEffect(effect);
			meta.setPower(1);
			fw.setFireworkMeta(meta);
		}
	}
}
