package fr.kyriog.rftd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import be.maximvdw.titlemotd.ui.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Difficulty;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import fr.kyriog.rftd.RftdLogger.Level;

public class RftdController {
	private RftdPlugin plugin;
	private BukkitTask task;
	private Location eggLocation;
	private boolean playing = false;
	private boolean starting = false;
	private int episode = 0;
	private Location endPortalLocation = null;
	private boolean endPortalLocationAnnounced = false;
	private boolean listenEntityDamage = true;
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

	public boolean shouldListenEntityDamage() {
		return listenEntityDamage;
	}

	public void onEnable() {
		if(plugin.getConfig().getInt("endPortalLocationEpisodeAnnounce") == 0) {
			String msg = "No value found for 'endPortalLocationEpisodeAnnounce', disabling end portal announce";
			RftdLogger.log(Level.WARN, msg);

			endPortalLocationAnnounced = true;
		}

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

		Title title = new Title("Tout le monde est prêt ?");
		title.setTitleColor(ChatColor.DARK_AQUA);
		title.setSubtitle("On ne bouge plus !");
		title.setSubtitleColor(ChatColor.AQUA);
		title.setFadeInTime(9);
		title.setStayTime(20);
		title.setFadeOutTime(9);
		title.setTimingsToTicks();
		title.broadcast();
		RftdLogger.log(Level.INFO, "Starting game...");

		RftdHelper.setDifficulty(Difficulty.HARD);
		RftdHelper.canEveryoneWalk(false);
		RftdHelper.setEveryoneGameMode(GameMode.ADVENTURE);
		RftdHelper.setAnimalSpawnLimit(50);

		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
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

		task = Bukkit.getScheduler().runTaskTimer(plugin, new StartTimer(), 40, 20);
	}

	private void freePlayers() {
		starting = false;
		task.cancel();

		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
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

	public void endTeleportEvent(Location endPortalLocation) {
		if(this.endPortalLocation != null)
			return;

		this.endPortalLocation = endPortalLocation;

		String msg = "Un joueur vient d'entrer dans l'End !";
		RftdLogger.broadcast(Level.INFO, msg);

		for(Player player : Bukkit.getOnlinePlayers()) {
			player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 1);
		}
	}

	public void lockListenEntityDamage() {
		if(!listenEntityDamage)
			return;

		listenEntityDamage = false;
		Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				listenEntityDamage = true;
			}
		}, 20);
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
		msg.append(ChatColor.DARK_RED + "Je vous attends où vous auriez dû me déposer...");
		Bukkit.broadcastMessage(msg.toString());

		String msgHelp = "Noobs : Rendez-vous en x=" + eggLocation.getBlockX() + ", z=" + eggLocation.getBlockZ();
		RftdLogger.broadcast(Level.INFO, msgHelp);

		trappedEgg = true;

		// Start sound between 3 seconds (60 ticks) and 5 seconds (100 ticks)
		long nextTick = Math.round(Math.random() * 40) + 60;
		Bukkit.getScheduler().runTaskLater(plugin, new TrappedEggSoundTask(), nextTick);
	}

	public void spawnDragon() {
		trappedEgg = false;
		eggLocation.getBlock().setType(Material.AIR);
		@SuppressWarnings("deprecation")
		final FallingBlock fallingEgg = eggLocation.getWorld().spawnFallingBlock(eggLocation, Material.DRAGON_EGG, (byte) 0);
		fallingEgg.setVelocity(new Vector(0, 2, 0));

		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				fallingEgg.getWorld().spawnEntity(fallingEgg.getLocation(), EntityType.ENDER_DRAGON);
				fallingEgg.remove();
			}
		}, 35);
	}

	public void end(Player winner) {
		task.cancel();

		RftdHelper.setDifficulty(Difficulty.PEACEFUL);
		RftdHelper.setAnimalSpawnLimit(-1); // Reset to Minecraft default value (should be 15)

		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Team team = scoreboard.getPlayerTeam(winner);

		RftdLogger.log(Level.INFO, "Team " + team.getDisplayName() + " win the game!");

		Title winTitle = new Title("C'est fini !");
		winTitle.setTitleColor(ChatColor.YELLOW);
		winTitle.setSubtitle("L'œuf a été déposé !");
		winTitle.setSubtitleColor(ChatColor.GOLD);
		winTitle.setFadeInTime(10);
		winTitle.setStayTime(20);
		winTitle.setFadeOutTime(10);
		winTitle.setTimingsToTicks();
		winTitle.broadcast();

		String[] winAnimation = new String[]{"(>'-')>", "^('-')^", "<('-'<)", "^('-')^"};

		StringBuilder msg = new StringBuilder();
		msg.append("L'équipe ");
		msg.append(team.getPrefix() + team.getDisplayName());
		msg.append(ChatColor.GOLD + " remporte la victoire.");
		AnimatedTitleTask teamTask = new AnimatedTitleTask(msg.toString(), 32);
		teamTask.setAnimations(winAnimation);
		teamTask.setTimes(10, 0);
		teamTask.runTaskTimer(plugin, 40, 5);

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
		AnimatedTitleTask playersTask = new AnimatedTitleTask(msg.toString(), 32);
		playersTask.setAnimations(winAnimation);
		playersTask.setTimes(0, 10);
		playersTask.runTaskTimer(plugin, 200, 5);

		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
		for(Player player : players) {
			if(player != winner)
				player.teleport(eggLocation);
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
			if(timer != 0) {
				Title timerTitle = new Title(String.valueOf(timer));
				switch(timer) {
					case 3:
						timerTitle.setTitleColor(ChatColor.GOLD);
						break;
					case 2:
						timerTitle.setTitleColor(ChatColor.RED);
						break;
					case 1:
						timerTitle.setTitleColor(ChatColor.DARK_RED);
						break;
					default:
						timerTitle.setTitleColor(ChatColor.YELLOW);
				}
				timerTitle.setFadeInTime(0);
				timerTitle.setStayTime(15);
				timerTitle.setFadeOutTime(5);
				timerTitle.setTimingsToTicks();
				timerTitle.broadcast();
				if(timer <= 3) {
					playSoundForAllPlayers(Sound.NOTE_PIANO, 10, 0.5);
				}
				RftdLogger.log(Level.INFO, String.valueOf(timer));
			} else {
				Title timerTitle = new Title("Go !");
				timerTitle.setTitleColor(ChatColor.GREEN);
				timerTitle.setSubtitle("Bonne chance à tous !");
				timerTitle.setSubtitleColor(ChatColor.DARK_GREEN);
				timerTitle.setFadeInTime(5);
				timerTitle.setStayTime(40);
				timerTitle.setFadeOutTime(20);
				timerTitle.setTimingsToTicks();
				timerTitle.broadcast();
				playSoundForAllPlayers(Sound.NOTE_PIANO, 10, 1);
				RftdLogger.log(Level.INFO, "Game started!");
				freePlayers();
			}

			timer--;
		}

		private void playSoundForAllPlayers(Sound sound, float volume, double pitch) {
			Collection<? extends Player> players = Bukkit.getOnlinePlayers();
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
				Title endTitle = new Title("");
				endTitle.setSubtitle("Fin de l'épisode " + episode);
				endTitle.setSubtitleColor(ChatColor.GOLD);
				endTitle.setFadeInTime(5);
				endTitle.setStayTime(30);
				endTitle.setFadeOutTime(5);
				endTitle.setTimingsToTicks();
				endTitle.broadcast();
				RftdLogger.log(Level.INFO, "Ending episode " + episode);
				episode++;
			} else if(minutesLeft == -1 && secondsLeft == 0) {
				minutesLeft = getEpisodeLength();
				secondsLeft = 0;
				if(episode != 1) {
					String msg = "Début de l'épisode " + episode;
					RftdLogger.broadcast(Level.SUCCESS, msg);

					int episodeAnnounce = plugin.getConfig().getInt("endPortalLocationEpisodeAnnounce");
					if(episode >= episodeAnnounce
							&& !endPortalLocationAnnounced
							&& endPortalLocation != null) {
						endPortalLocationAnnounced = true;

						StringBuilder msgHelp = new StringBuilder();
						msgHelp.append("Noobs : Un portail vers l'End se trouve en ");
						msgHelp.append("x=" + endPortalLocation.getBlockX() + ", ");
						msgHelp.append("y=" + endPortalLocation.getBlockY() + ", ");
						msgHelp.append("z=" + endPortalLocation.getBlockZ());
						RftdLogger.broadcast(Level.INFO, msgHelp.toString());
					}
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

	private class TrappedEggSoundTask implements Runnable {
		@Override
		public void run() {
			if(!trappedEgg)
				return;

			eggLocation.getWorld().playSound(eggLocation, Sound.ENDERDRAGON_GROWL, (float) 0.7, 2);

			// Next sound between 3 seconds (60 ticks) and 10 seconds (200 ticks)
			long nextTick = Math.round(Math.random() * 140) + 60;
			Bukkit.getScheduler().runTaskLater(plugin, this, nextTick);
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

	private class AnimatedTitleTask extends BukkitRunnable {
		private List<String> animations = new ArrayList<>();
		private String message;
		private int fadeInTime = 0;
		private int fadeOutTime = 0;
		private int totalDuration;
		private int currentDuration = 0;
		private int currentAnimation = 0;

		public AnimatedTitleTask(String message, int duration) {
			this.message = message;
			this.totalDuration = duration;
		}

		public void addAnimation(String animation) {
			animations.add(animation);
		}

		public void setAnimations(String... animations) {
			this.animations.addAll(Arrays.asList(animations));
		}

		public void setTimes(int fadeInTime, int fadeOutTime) {
			this.fadeInTime = fadeInTime;
			this.fadeOutTime = fadeOutTime;
		}

		@Override
		public void run() {
			currentDuration++;

			Title animatedTitle = new Title(animations.get(currentAnimation));
			animatedTitle.setTitleColor(ChatColor.DARK_AQUA);
			animatedTitle.setSubtitle(message);
			animatedTitle.setSubtitleColor(ChatColor.GOLD);
			animatedTitle.setFadeInTime(currentDuration == 0 ? fadeInTime : 0);
			animatedTitle.setStayTime(10);
			animatedTitle.setFadeOutTime(currentDuration == totalDuration ? fadeOutTime : 0);
			animatedTitle.setTimingsToTicks();
			animatedTitle.broadcast();

			if(currentDuration == totalDuration)
				cancel();
			currentAnimation++;
			if(currentAnimation >= animations.size())
				currentAnimation = 0;
		}
	}
}
