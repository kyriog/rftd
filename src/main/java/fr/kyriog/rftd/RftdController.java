package fr.kyriog.rftd;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

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

		task = Bukkit.getScheduler().runTaskTimer(plugin, new StartTimer(), 20, 20);
	}

	private void freePlayers() {
		starting = false;
		task.cancel();

		RftdHelper.canEveryoneWalk(true);
		RftdHelper.setEveryoneGameMode(GameMode.SURVIVAL);

		for(World world : Bukkit.getWorlds()) {
			world.setFullTime(0);
			world.setGameRuleValue("doDaylightCycle", "true");
		}
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
