package fr.kyriog.rftd.listener;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerListener implements Listener {
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
		String format = "<%1$s" + ChatColor.WHITE + "> %2$s";
		e.setFormat(format);
	}
}
