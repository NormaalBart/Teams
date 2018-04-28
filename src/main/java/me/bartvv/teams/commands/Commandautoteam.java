package me.bartvv.teams.commands;

import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;

import me.bartvv.teams.Utils;
import me.bartvv.teams.manager.Team;
import me.bartvv.teams.manager.User;
import me.bartvv.teams.scoreboard.scoreboard.PlayerScoreboard;
import me.bartvv.teams.scoreboard.scoreboard.Wrapper;
import me.bartvv.teams.scoreboard.scoreboard.Wrapper.WrapperType;

public class Commandautoteam extends TeamCommand {

	@Override
	public void onCommand(User user, Command command, String commandLabel, String[] args) throws Exception {
		if (!user.getBase().hasPermission("teams.autoteam")) {
			user.sendMessage(tl("noPermission"));
			return;
		}
		Set<User> toSort = Sets.newHashSet();

		for (Player player : Bukkit.getOnlinePlayers()) {
			User userPlayer = teams.getUser(player.getName());
			if (userPlayer.getTeam() == null) {
				toSort.add(userPlayer);
			}
		}

		for (User userToSort : toSort) {
			if (userToSort.getTeam() == null) {
				Team lowestMembers = null;
				int lowestCount = Integer.MAX_VALUE;
				for (Team team : teams.getTeams().values()) {
					if (team.getMembers().size() < lowestCount) {
						lowestCount = team.getMembers().size();
						lowestMembers = team;
					}
				}

				if (lowestMembers != null) {
					Utils.debug("User " + userToSort.getName() + " joined the team " + lowestMembers.getTeamName());
					lowestMembers.addMember(userToSort.getName());
					userToSort.setTeam(lowestMembers);
				}
			}
		}

		for (Player player : Bukkit.getOnlinePlayers()) {
			User target = teams.getUser(player.getName());
			if (target == null) {
				teams.getLogger().log(Level.WARNING, "User is null! Error...");
				continue;
			}
			if (target.getTeam().getSpawnLoc() != null) {
				try {
					player.teleport(target.getTeam().getSpawnLoc());
				} catch (Exception exc) {
					teams.getLogger().log(Level.WARNING,
							"Failed to teleport " + player.getName() + " to team location!");
				}
			}
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
					"kit " + target.getTeam().getTeamName() + " " + player.getName());
		}

		user.sendMessage(tl("Teams-Sorted", toSort.size()));

		teams.getGlaedr().registerPlayers();

		for (PlayerScoreboard board : PlayerScoreboard.getScoreboards()) {
			board.clearLines();
			for (String lines : teams.getScoreboard().getStringList("scoreboard.lines")) {
				Wrapper wrapper = new Wrapper(lines, board, WrapperType.TOP);
				wrapper.setText(lines);
				board.getWrappers().add(wrapper);
				board.getEntries().add(wrapper);
				wrapper.setup();
			}
		}

		teams.setCanSwitch(false);
	}
}
