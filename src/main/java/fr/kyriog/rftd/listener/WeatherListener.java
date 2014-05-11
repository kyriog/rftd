package fr.kyriog.rftd.listener;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WeatherListener implements Listener {
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent e) {
		if(e.toWeatherState()) {
			e.setCancelled(true);
			World world = e.getWorld();
			world.setStorm(false);
			world.setThundering(false);
			world.setWeatherDuration(100000);
		}
	}
}
