package me.bartvv.teams.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import me.bartvv.teams.Teams;
import me.bartvv.teams.manager.Team;
import me.bartvv.teams.manager.User;

public class LeaveEvent implements Listener {

	private Teams team;

	public LeaveEvent(Teams team) {
		this.team = team;
		team.getServer().getPluginManager().registerEvents(this, team);
	}

	@EventHandler
	public void on(PlayerQuitEvent e) {
		User user = team.getUser(e.getPlayer().getName());

		if (user == null)
			return;

		Team team = user.getTeam();
		if (team == null)
			return;

		team.removeMember(e.getPlayer().getName());
	}

}
