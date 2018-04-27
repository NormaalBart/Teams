package me.bartvv.teams.manager;

import org.bukkit.command.CommandSender;

public class User {

	private String name;
	private Team team;
	private CommandSender sender;

	public User(String name) {
		this.name = name;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public Team getTeam() {
		return team;
	}

	public String getName() {
		return name;
	}

	public CommandSender getBase() {
		return this.sender;
	}

	public void setSender(CommandSender sender) {
		this.sender = sender;
	}

	public void sendMessage(String message) {
		getBase().sendMessage(message);
	}
}
