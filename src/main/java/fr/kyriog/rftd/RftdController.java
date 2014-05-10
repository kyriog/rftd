package fr.kyriog.rftd;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import fr.kyriog.rftd.RftdLogger.Level;

public class RftdController {
	private RftdPlugin plugin;
	private BukkitTask task;
	private boolean playing = false;
	private boolean starting = false;

	public RftdController(RftdPlugin plugin) {
		this.plugin = plugin;
	}

	public void setConfig(String path, Object value) {
		plugin.getConfig().set(path, value);
		plugin.saveConfig();
	}

	public boolean isPlaying() {
		return playing;
	}

	public boolean isStarting() {
		return starting;
	}

	public void onEnable() {
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

		task = Bukkit.getScheduler().runTaskTimer(plugin, new StartTimer(), 20, 20);
	}

	private void freePlayers() {
		starting = false;
		task.cancel();
		RftdHelper.canEveryoneWalk(true);

		for(World world : Bukkit.getWorlds()) {
			world.setFullTime(0);
			world.setGameRuleValue("doDaylightCycle", "true");
		}
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
