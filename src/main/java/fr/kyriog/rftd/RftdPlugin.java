package fr.kyriog.rftd;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import fr.kyriog.rftd.command.EggExecutor;
import fr.kyriog.rftd.command.StartExecutor;
import fr.kyriog.rftd.command.TeamExecutor;
import fr.kyriog.rftd.listener.BlockListener;
import fr.kyriog.rftd.listener.EntityListener;
import fr.kyriog.rftd.listener.PlayerListener;
import fr.kyriog.rftd.listener.WeatherListener;

public class RftdPlugin extends JavaPlugin {
	@Override
	public void onEnable() {
		RftdController controller = new RftdController(this);

		controller.onEnable();

		getCommand("egg").setExecutor(new EggExecutor(controller));
		getCommand("start").setExecutor(new StartExecutor(controller));
		getCommand("team").setExecutor(new TeamExecutor());

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new BlockListener(controller), this);
		pm.registerEvents(new EntityListener(controller), this);
		pm.registerEvents(new PlayerListener(controller), this);
		pm.registerEvents(new WeatherListener(), this);
	}
}
