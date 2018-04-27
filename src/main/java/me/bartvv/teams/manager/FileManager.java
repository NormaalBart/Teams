package me.bartvv.teams.manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Maps;

import net.md_5.bungee.api.ChatColor;

public class FileManager {

	private boolean debug = false;

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	private JavaPlugin javaPlugin;
	private File file;
	private YamlConfiguration configuration;
	private YamlConfiguration backendConfiguration;
	private HashMap<String, Object> cache;
	private Integer unsavedChanges = 0;
	private Integer maxUnsavedChanges = 10;

	@Deprecated
	public FileManager(JavaPlugin javaPlugin, String name) {
		new FileManager(javaPlugin, name, 10);
	}

	public FileManager(JavaPlugin javaPlugin, String name, Integer maxUnsavedChanges) {
		Validate.notNull(javaPlugin, "JavaPlugin cannot be null");
		Validate.notNull(name, "Name cannot be null!");
		Validate.notNull(maxUnsavedChanges, "unsaved changes cannot be null!");
		if (!name.endsWith(".yml"))
			name = name + ".yml";

		this.javaPlugin = javaPlugin;
		this.file = new File(this.javaPlugin.getDataFolder(), name);
		if (!file.getParentFile().exists()) {
			debug("Creating parentfile dirs");
			file.getParentFile().mkdirs();
		}

		this.cache = Maps.newHashMap();
		if (!file.exists()) {
			this.javaPlugin.saveResource(name, false);
			debug("Saving resource" + name);
		}

		this.configuration = YamlConfiguration.loadConfiguration(this.file);

		InputStream stream = this.javaPlugin.getResource(name);
		Reader reader = new InputStreamReader(stream);

		this.backendConfiguration = YamlConfiguration.loadConfiguration(reader);
	}

	public List<String> getStringList(String path) {
		return getStringList(path, false);
	}

	@SuppressWarnings("unchecked")
	public List<String> getStringList(String path, Boolean load) {
		return (List<String>) get(path, load, null);
	}

	public void set(String path, Object obj) {
		set(path, obj, false);
	}

	public void set(String path, Object obj, Boolean save) {
		if (obj instanceof Location) {
			Location loc = (Location) obj;
			configuration.set(path + ".world", loc.getWorld().getName());
			configuration.set(path + ".X", loc.getX());
			configuration.set(path + ".Y", loc.getY());
			configuration.set(path + ".Z", loc.getZ());
			configuration.set(path + ".yaw", loc.getYaw());
			configuration.set(path + ".pitch", loc.getPitch());
		} else {
			configuration.set(path, obj);
		}
		unsavedChanges++;
		if (save) {
			if (save()) {
				unsavedChanges = 0;
			} else {
				debug("Failed to save config");
				javaPlugin.getLogger().log(Level.WARNING,
						"Config could not be saved for plugin: " + javaPlugin.getName() + "!");
				javaPlugin.getLogger().log(Level.WARNING, "Please contact plugin owner to fix this! ");
				javaPlugin.getLogger().log(Level.WARNING, "Also include the stacktrace shown above!");
			}
		} else {
			if (unsavedChanges >= maxUnsavedChanges) {
				if (save()) {
					unsavedChanges = 0;
				} else {
					debug("Failed to save config");
					javaPlugin.getLogger().log(Level.WARNING,
							"Config could not be saved for plugin: " + javaPlugin.getDescription().getName()
									+ "! (Version: " + javaPlugin.getDescription().getVersion() + ")");
					javaPlugin.getLogger().log(Level.WARNING, "Please contact plugin owner ("
							+ javaPlugin.getDescription().getAuthors().toString().replace("[", "").replace("]", "")
							+ ") to fix this! ");
					javaPlugin.getLogger().log(Level.WARNING, "Also include the stacktrace shown above!");
				}
			}
		}
	}

	public String getString(String path) {
		return getString(path, false, null);
	}

	public String getString(String path, Boolean load) {
		return getString(path, load, null);
	}

	public String getString(String path, Boolean load, String def) {
		return ChatColor.translateAlternateColorCodes('&', (String) get(path, load, def));
	}

	public Location getLocation(String path) {
		return getLocation(path, false);
	}

	public Location getLocation(String path, Boolean load) {
		Validate.notNull(path, "Path cannot be null");
		Location loc = null;
		Object obj;
		if (load) {
			String worldName = getString(path + ".world", true);
			World world = Bukkit.getWorld(worldName);
			Double x, y, z, yaw, pitch;
			x = getDouble(path + ".X", true);
			y = getDouble(path + ".Y", true);
			z = getDouble(path + ".Z", true);
			yaw = getDouble(path + ".yaw", true);
			pitch = getDouble(path + "pitch", true);
			loc = new Location(world, x, y, z, yaw.floatValue(), pitch.floatValue());
			if(loc != null) {
				cache.put(path, loc);
			}
		} else {
			obj = cache.get(path);
			if (obj != null && obj instanceof Location) {
				loc = (Location) obj;
			} else {
				obj = getLocation(path, true);
				if (obj != null && obj instanceof Location) {
					cache.put(path, obj);
					loc = (Location) obj;
				}
			}
		}
		return loc;
	}

	public int getInteger(String path) {
		return getInteger(path, false, -1);
	}

	public int getInteger(String path, Boolean load) {
		return getInteger(path, load, -1);
	}

	public int getInteger(String path, Boolean load, Integer def) {
		return (int) get(path, load, def);
	}

	public double getDouble(String path) {
		return getDouble(path, false, -1.0D);
	}

	public double getDouble(String path, Boolean load) {
		return getDouble(path, load, -1.0D);
	}

	public double getDouble(String path, Boolean load, Double def) {
		return (double) get(path, load, def);
	}

	public Object get(String path) {
		return get(path, false, null);
	}

	public Object get(String path, Boolean load) {
		return get(path, load, null);
	}

	public Object get(String path, Boolean load, Object def) {
		Validate.notNull(path, "Path cannot be null");
		Object obj;
		if (load) {
			debug("Getting an object from path " + path);
			obj = configuration.get(path, def);
			if (obj != null) {
				cache.put(path, obj);
			} else {
				debug("Getting an object from the backend configuration");
				obj = backendConfiguration.get(path, def);
				cache.put(path, obj);
			}
		} else {
			debug("Got configurationsection from path " + path + " via cache");
			obj = cache.get(path);
			if (obj == null) {
				obj = get(path, true, def);
			}
		}
		return obj == null ? def : obj;
	}

	public List<?> getList(String path) {
		return getList(path, false, null);
	}

	public List<?> getList(String path, Boolean load) {
		return getList(path, load, null);
	}

	public List<?> getList(String path, Boolean load, List<?> def) {
		return (List<?>) get(path, load, def);
	}

	public boolean getBoolean(String path) {
		return getBoolean(path, false, false);
	}

	public boolean getBoolean(String path, Boolean load) {
		return getBoolean(path, load, false);
	}

	public boolean getBoolean(String path, Boolean load, Boolean def) {
		return (boolean) get(path, load, def);
	}

	public ItemStack getItemStack(String path) {
		return getItemStack(path, false);
	}

	public ItemStack getItemStack(String path, Boolean load) {
		Validate.notNull(path, "Path cannot be null");
		ItemStack itemStack;

		if (load) {
			itemStack = configuration.getItemStack(path);
			cache.put(path, itemStack);
		} else {
			Object obj = cache.get(path);
			if (obj != null && obj instanceof ItemStack) {
				itemStack = (ItemStack) obj;
			} else {
				itemStack = getItemStack(path, true);
			}

		}
		return itemStack;
	}

	public ConfigurationSection getSection(String path) {
		return getSection(path, false);
	}

	public ConfigurationSection getSection(String path, Boolean load) {
		Validate.notNull(path, "Path cannot be null");
		ConfigurationSection section;
		if (load) {
			section = configuration.getConfigurationSection(path);
			cache.put(path, section);
			debug("Got configurationsection from path " + path);
		} else {
			Object obj = cache.get(path);
			if (obj != null && obj instanceof ConfigurationSection) {
				section = (ConfigurationSection) obj;
			} else {
				section = getSection(path, true);
			}
		}
		return section;
	}

	public YamlConfiguration getConfig() {
		return configuration;
	}

	public void resetCache(Boolean save) {
		if (save)
			this.save();
		cache.clear();
		debug("Resetted cache");
	}

	public boolean save() {
		try {
			if (unsavedChanges != 0) {
				configuration.save(file);
				unsavedChanges = 0;
				cache.clear();
				debug(this.file.getName() + " saved");
			}
			return true;
		} catch (IOException ioexception) {
			debug("Failed to save " + this.file.getName());
			ioexception.printStackTrace();
			return false;
		}
	}

	private void debug(String debug) {
		if (this.debug) {
			System.out.println("[" + this.javaPlugin.getName() + "] [DEBUG] " + debug);
		}
	}
}