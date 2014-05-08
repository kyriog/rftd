package fr.kyriog.rftd;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import fr.kyriog.rftd.command.EggExecutor;
import fr.kyriog.rftd.command.StartExecutor;
import fr.kyriog.rftd.command.TeamExecutor;
import fr.kyriog.rftd.listener.PlayerListener;

public class RftdPlugin extends JavaPlugin {
	@Override
	public void onEnable() {
		RftdController controller = new RftdController(this);

		getCommand("egg").setExecutor(new EggExecutor(controller));
		getCommand("start").setExecutor(new StartExecutor(controller));
		getCommand("team").setExecutor(new TeamExecutor());

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerListener(controller), this);
	}
}
