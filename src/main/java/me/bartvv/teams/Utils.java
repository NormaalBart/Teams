package me.bartvv.teams;

import java.text.MessageFormat;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;

import net.md_5.bungee.api.ChatColor;

public class Utils {

	private static final Teams TEAMS = JavaPlugin.getPlugin(Teams.class);

	public static String tl(String message, Object... obj) {
		message = TEAMS.getMessages().getString(message);

		if (!(obj.length == 0)) {
			MessageFormat temp = new MessageFormat(message);
			String formatted = temp.format(obj);
			return formatted;
		}
		return message;
	}

	public static void debug(String string) {
		if (TEAMS.isDebug()) {
			System.out.println("[Teams] [DEBUG] " + string);
		}
	}

	public static List<String> tlList(String message, Object... obj) {
		List<String> msg = TEAMS.getMessages().getStringList(message);
		List<String> toReturn = Lists.newArrayList();

		if (!(obj.length == 0)) {
			for (int i = 0; i < msg.size(); i++) {
				MessageFormat temp = new MessageFormat(msg.get(i));
				String formatted = temp.format(obj);
				toReturn.add(ChatColor.translateAlternateColorCodes('&', formatted));
			}
		}
		return toReturn;
	}
}
