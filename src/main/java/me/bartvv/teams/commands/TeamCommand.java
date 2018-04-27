package me.bartvv.teams.commands;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;

import me.bartvv.teams.Teams;
import me.bartvv.teams.Utils;
import me.bartvv.teams.manager.User;

public class TeamCommand {

	protected Teams teams;

	public void setTeams(Teams teams) {
		this.teams = teams;
	}

	public void onCommand(User user, Command command, String commandLabel, String[] args) throws Exception {
		throw new Exception("Not-Supported");
	}

	public List<String> onTabComplete(User user, String commandLabel, String[] args) throws Exception {
		return Collections.emptyList();
	}

	public String tl(String message, Object... obj) {
		return Utils.tl(message, obj);
	}
}
