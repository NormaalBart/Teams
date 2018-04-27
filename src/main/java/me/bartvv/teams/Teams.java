package me.bartvv.teams;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.Getter;
import lombok.Setter;
import me.bartvv.teams.commands.TeamCommand;
import me.bartvv.teams.listeners.JoinEvent;
import me.bartvv.teams.listeners.LeaveEvent;
import me.bartvv.teams.manager.FileManager;
import me.bartvv.teams.manager.Team;
import me.bartvv.teams.manager.User;
import me.bartvv.teams.scoreboard.Glaedr;

public class Teams extends JavaPlugin {

	@Getter
	private transient FileManager messages, data, scoreboard;
	private transient FileManager config;
	private transient Map<Command, TeamCommand> commands;
	private static transient Map<String, User> users;
	@Getter
	private transient Map<String, Team> teams;
	@Getter
	@Setter
	private transient boolean canSwitch = true, debug = false;
	@Getter
	private transient Glaedr glaedr;

	@Override
	public void onEnable() {
		this.config = new FileManager(this, "config.yml", 10);
		this.messages = new FileManager(this, "messages.yml", 10);
		this.data = new FileManager(this, "data.yml", 10);
		this.scoreboard = new FileManager(this, "scoreboard.yml", 10);
		this.commands = Maps.newHashMap();
		users = Maps.newHashMap();
		this.teams = Maps.newHashMap();
		this.teams.put("red", new Team("red"));
		this.teams.put("blue", new Team("blue"));
		try {
			Location locRed = data.getLocation("team.red");
			Location locBlue = data.getLocation("team.blue");
			if (locRed != null) {
				this.teams.get("red").setSpawnLoc(locRed);
			}
			if (locBlue != null) {
				this.teams.get("blue").setSpawnLoc(locBlue);
			}
		} catch (Exception exc) {
		}

		debug = getConfiguration().getBoolean("debug");
		this.messages.setDebug(debug);
		this.config.setDebug(debug);
		this.data.setDebug(debug);

		for (Player player : Bukkit.getOnlinePlayers()) {
			addUser(player.getName());
		}

		addUser("console");

		new JoinEvent(this);
		new LeaveEvent(this);

		new BukkitRunnable() {
			@Override
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					User user = getUser(player.getName());
					Player nearest = null;
					Double distance = Double.MAX_VALUE;
					for (Player toCheck : Bukkit.getOnlinePlayers()) {
						User target = getUser(toCheck.getName());
						if (target.getTeam() != null && user.getTeam() != null
								&& !target.getTeam().getTeamName().equalsIgnoreCase(user.getTeam().getTeamName())) {
							Double distanceCheck = toCheck.getLocation().distanceSquared(player.getLocation());
							if (distanceCheck < distance) {
								distance = distanceCheck;
								nearest = toCheck;
							}
						}
					}
					if (nearest != null) {
						player.setCompassTarget(nearest.getLocation());
					}
				}
			}
		}.runTaskTimerAsynchronously(this, 0, 20);

		this.glaedr = new Glaedr(this, getScoreboard().getString("scoreboard.title"));
	}

	@Override
	public void onDisable() {
		data.save();

		try {
			Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
			org.bukkit.scoreboard.Team team = board.getTeam("red");
			team.setPrefix(ChatColor.RESET + "");
		} catch (Exception exc) {
		}
		
		try {
			Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
			org.bukkit.scoreboard.Team team = board.getTeam("blue");
			team.setPrefix(ChatColor.RESET + "");
		} catch (Exception exc) {
		}

		for (Player player : Bukkit.getOnlinePlayers()) {
			player.setPlayerListName(ChatColor.RESET + player.getName());
			Scoreboard board = player.getScoreboard();
			if (board != null) {
				User user = getUser(player.getName());
				if (user != null && user.getTeam() != null) {
					try {
						org.bukkit.scoreboard.Team team = board.getTeam(user.getTeam().getTeamName());
						team.setPrefix(ChatColor.RESET + "");
					} catch (Exception exc) {
					}
				}
			}
		}
	}

	@Override
	public void saveConfig() {
		config.save();
	}

	public FileManager getConfiguration() {
		return config;
	}

	public Team getTeam(String team) {
		return teams.get(team.toLowerCase());
	}

	public User getUser(String name) {
		return users.get(name.toLowerCase());
	}

	public void addUser(String name) {
		users.put(name.toLowerCase(), new User(name));
	}

	@Override
	public void reloadConfig() {
		this.data.resetCache(true);
		this.messages.resetCache(false);
		this.config.resetCache(false);
		this.scoreboard.resetCache(false);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		TeamCommand teamCommand = commands.get(command);

		if (teamCommand == null) {
			try {
				teamCommand = (TeamCommand) Teams.class.getClassLoader()
						.loadClass("me.bartvv.teams.commands.Command" + command.getName()).newInstance();
				teamCommand.setTeams(this);
				commands.put(command, teamCommand);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException exc) {
				sender.sendMessage("Something went wrong! " + exc.getMessage());
				exc.printStackTrace();
				return true;
			}

		}
		String name;
		if(sender instanceof Player) {
			name = sender.getName();
		} else {
			name = "console";
		}
		Utils.debug("User: " + name + " issued command /" + command.getName());
		User user = getUser(name);
		user.setSender(sender);

		try {
			teamCommand.onCommand(user, command, label, args);
		} catch (Exception e) {
			try {
				String message = getConfiguration().getString(e.getMessage());
				sender.sendMessage(message);
			} catch (Exception exc) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	public void setDebug(Boolean debug) {
		this.debug = debug;
		this.config.setDebug(debug);
		this.data.setDebug(debug);
		this.messages.setDebug(debug);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		TeamCommand cmd = commands.get(command);

		if (cmd == null) {
			try {
				cmd = (TeamCommand) Teams.class.getClassLoader()
						.loadClass("me.bartvv.teams.commands.Command" + command.getName()).newInstance();
				cmd.setTeams(this);
				commands.put(command, cmd);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException exc) {
				sender.sendMessage("Something went wrong! " + exc.getMessage());
				exc.printStackTrace();
				return Collections.emptyList();
			}

		}

		User user = getUser(sender instanceof Player ? sender.getName() : "console");
		user.setSender(sender);

		List<String> methodReturn;
		try {
			methodReturn = cmd.onTabComplete(user, alias, args);
		} catch (Exception e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
		List<String> toReturn = Lists.newArrayList();

		String lastArg = args[args.length - 1];

		for (String str : methodReturn) {
			if (str.toLowerCase().startsWith(lastArg.toLowerCase())) {
				toReturn.add(str);
			}
		}
		return toReturn;
	}
}
