package fr.kyriog.rftd;

public class RftdController {
	private RftdPlugin plugin;

	public RftdController(RftdPlugin plugin) {
		this.plugin = plugin;
	}

	public void setConfig(String path, Object value) {
		plugin.getConfig().set(path, value);
		plugin.saveConfig();
	}
}
