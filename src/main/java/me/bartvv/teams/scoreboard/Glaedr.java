package me.bartvv.teams.scoreboard;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import lombok.Getter;
import me.bartvv.teams.scoreboard.scoreboard.Entry;
import me.bartvv.teams.scoreboard.scoreboard.PlayerScoreboard;
import me.bartvv.teams.scoreboard.scoreboard.Wrapper;

/**
 * Copyright 2016 Alexander Maxwell Use and or redistribution of compiled JAR
 * file and or source code is permitted only if given explicit permission from
 * original author: Alexander Maxwell
 */
@Getter
public class Glaedr implements Listener {

	private static JavaPlugin plugin;
	private String title;
	private boolean hook, overrideTitle, scoreCountUp;
	private List<String> bottomWrappers, topWrappers;

	public Glaedr(JavaPlugin plugin, String title, boolean hook, boolean overrideTitle, boolean scoreCountUp) {
		Glaedr.plugin = plugin;
		this.title = ChatColor.translateAlternateColorCodes('&', title);
		this.hook = hook;
		this.overrideTitle = overrideTitle;
		this.scoreCountUp = scoreCountUp;

		bottomWrappers = new ArrayList<>();
		topWrappers = new ArrayList<>();
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public Glaedr(JavaPlugin plugin, String title) {
		this(plugin, title, false, true, false);
	}

	public void registerPlayers() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			new PlayerScoreboard(this, player);
		}
	}

	public static JavaPlugin getPlugin() {
		return plugin;
	}
}