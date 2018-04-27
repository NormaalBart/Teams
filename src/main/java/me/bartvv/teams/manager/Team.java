package me.bartvv.teams.manager;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import com.google.common.collect.Sets;

public class Team {

	private Set<String> members;
	private String teamName;
	private Location spawnLoc;

	public Team(String teamName) {
		this.teamName = teamName;
		this.members = Sets.newHashSet();
	}

	public String getTeamName() {
		return teamName;
	}

	public Set<String> getMembers() {
		return members;
	}

	public void addMember(String name) {
		this.members.add(name);
		Player player = Bukkit.getPlayer(name);

		if (player != null) {
			player.setPlayerListName(ChatColor.valueOf(teamName.toUpperCase()) + name);
			Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
			if (board == null) {
				board = Bukkit.getScoreboardManager().getNewScoreboard();
			}
			org.bukkit.scoreboard.Team team = board.getTeam(teamName);
			if (team == null) {
				team = board.registerNewTeam(teamName);
			}
			team.setPrefix("" + ChatColor.valueOf(teamName.toUpperCase()));
			team.addPlayer(player);
			for (Player players : Bukkit.getOnlinePlayers()) {
				players.setScoreboard(board);
			}
		}
	}

	public void removeMember(String name) {
		this.members.remove(name);

		Player player = Bukkit.getPlayer(name);

		if (player != null) {
			player.setPlayerListName(ChatColor.valueOf(teamName.toUpperCase()) + name);
			Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
			if (board == null)
				return;
			
			org.bukkit.scoreboard.Team team = board.getTeam(teamName);
			if (team == null)
				return;
			
			team.removePlayer(player);
			for (Player players : Bukkit.getOnlinePlayers()) {
				players.setScoreboard(board);
			}
		}
	}

	public Location getSpawnLoc() {
		return spawnLoc;
	}

	public void setSpawnLoc(Location spawnLoc) {
		this.spawnLoc = spawnLoc;
	}

}
