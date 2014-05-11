package fr.kyriog.rftd;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import fr.kyriog.rftd.RftdLogger.Level;

public class RftdController {
	private RftdPlugin plugin;
	private BukkitTask task;
	private Location eggLocation;
	private boolean playing = false;
	private boolean starting = false;

	public RftdController(RftdPlugin plugin) {
		this.plugin = plugin;
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

	public void onEnable() {
		String eggLocationString = plugin.getConfig().getString("egg");
		if(eggLocationString != null) {
			Location eggLocation = RftdHelper.stringToBlockLocation(eggLocationString);
			setEggLocation(eggLocation);
		}

		for(World world : Bukkit.getWorlds()) {
			world.setFullTime(6000);
			world.setGameRuleValue("doDaylightCycle", "false");
		}
	}

	public void start() {
		playing = true;
		starting = true;

		String msg = "Tout le monde est prêt ? On ne bouge plus !";
		RftdLogger.broadcast(Level.SUCCESS, msg);

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
	}

	public void end(Player winner) {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Team team = scoreboard.getPlayerTeam(winner);

		String win = "C'est fini ! L'œuf a été déposé !";
		RftdLogger.broadcast(Level.SUCCESS, win);

		StringBuilder msg = new StringBuilder();
		msg.append("L'équipe ");
		msg.append(team.getPrefix() + team.getName());
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

		playing = false;
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
}
