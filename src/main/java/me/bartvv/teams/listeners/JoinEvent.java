package me.bartvv.teams.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.bartvv.teams.Teams;

public class JoinEvent implements Listener {
	
	private Teams team;
	
	public JoinEvent(Teams team) {
		this.team = team;
		team.getServer().getPluginManager().registerEvents(this, team);
	}

	@EventHandler
	public void on(PlayerJoinEvent e) {
		team.addUser(e.getPlayer().getName());
	}

}
