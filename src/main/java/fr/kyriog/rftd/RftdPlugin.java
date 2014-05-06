package fr.kyriog.rftd;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import fr.kyriog.rftd.command.TeamExecutor;
import fr.kyriog.rftd.listener.PlayerListener;

public class RftdPlugin extends JavaPlugin {
	@Override
	public void onEnable() {
		getCommand("team").setExecutor(new TeamExecutor());

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerListener(), this);
	}
}
