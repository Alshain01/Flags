package alshain01.Flags.data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.plugin.java.JavaPlugin;

import alshain01.Flags.data.CustomYML;

public class YamlDataStore implements DataStore {
	private static CustomYML data;
	private static CustomYML def;
	private static CustomYML world;
	private static CustomYML bundle;
	private static CustomYML price;
	
	private CustomYML getYml(String path) {
		String[] pathList = path.split("\\.");
		
		if (pathList[0].equalsIgnoreCase("world")) { return world; }
		else if (pathList[0].equalsIgnoreCase("default")) { return def; }
		else if (pathList[0].equalsIgnoreCase("bundle")) { return bundle; }
		else if (pathList[0].equalsIgnoreCase("price")) { return price; }
		else { return data; }
	}

	public YamlDataStore(JavaPlugin plugin){
		data = new CustomYML(plugin, "data.yml");
		world = new CustomYML(plugin, "world.yml");
		bundle = new CustomYML(plugin, "bundle.yml");
		def = new CustomYML(plugin, "default.yml");
		price = new CustomYML(plugin, "price.yml");
		
		price.saveDefaultConfig();
		bundle.saveDefaultConfig();
	}
	
    @Override
	public boolean reload(JavaPlugin plugin) {
		data.reload();
		def.reload();
		world.reload();
		bundle.reload();
		price.reload();
		return true;
	}
	
	@Override
	public void write(String path, String value) {
		CustomYML cYml = getYml(path);
		cYml.getConfig().set(path, value);
		cYml.saveConfig();
	}

	@Override
	public void write(String path, List<String> list) {
		CustomYML cYml = getYml(path);
		cYml.getConfig().set(path, list);
		cYml.saveConfig();
	}
	
	@Override
	public void write(String path, Set<String> set) {
		CustomYML cYml = getYml(path);
		cYml.getConfig().set(path, set);
		cYml.saveConfig();
	}
	
	@Override
	public void write(String path, double value) {
		CustomYML cYml = getYml(path);
		cYml.getConfig().set(path, value);
		cYml.saveConfig();		
	}

	@Override
	public List<String> readList(String path) {
		List<?> listData = getYml(path).getConfig().getList(path);
		if(listData == null) { return null; }
		
		List<String> stringData = new ArrayList<String>();
		
		for (Object value : listData) {
			stringData.add((String)value);
		}
		return stringData;
	}
	
	@Override
	public Set<String> readSet(String path) {
		List<?> setData = getYml(path).getConfig().getList(path);
		if(setData == null) { return null; }
		
		Set<String> stringData = new HashSet<String>();
		
		for (Object value : setData) {
			stringData.add((String)value);
		}
		return stringData;
	}

	@Override
	public Set<String> readKeys(String path) {
		if (read(path) != null) {
			return getYml(path).getConfig().getConfigurationSection(path).getKeys(false);
		} else {
			return new HashSet<String>();
		}
	}

	@Override
	public boolean isSet(String path) {
		if(getYml(path).getConfig().getString(path) != null) { return true; }
		return false;
	}
	
	@Override
	public String read(String path) {
		return getYml(path).getConfig().getString(path);
	}

	@Override
	public int readInt(String path) {
		return getYml(path).getConfig().getInt(path);
	}
	
	@Override
	public double readDouble(String path) {
		return getYml(path).getConfig().getDouble(path);
	}

	@Override
	public boolean create(JavaPlugin plugin) {
		// Don't change the version here, not needed
		this.setVersion("1.0.0");
		return true;
	}

	@Override
	public void update(JavaPlugin plugin) {
		//DatabaseManager.UpgradeDatabase(plugin, this);
	}

	@Override
	public boolean exists(JavaPlugin plugin) {
		File fileObject = new File(plugin.getDataFolder() + "\\default.yml");
		return fileObject.exists();
	}

	@Override
	public int getVersionMajor() {
		String ver = read("Default.Database.Version");
		
		// Correct for old "2-digit" version storage
		if (ver.length() <= 3) { ver += ".0"; }
		
		String[] version = ver.split("//.");
		return Integer.valueOf(version[0]);
	}
	
	@Override
	public int getVersionMinor() {
		String ver = read("Default.Database.Version");
		if (ver.length() <= 3) { ver += ".0"; }
		
		String[] version = ver.split("//.");
		return Integer.valueOf(version[1]);
	}

	@Override
	public int getBuild() {
		String ver = read("Default.Database.Version");
		if (ver.length() <= 3) { return 0; }
		
		String[] version = ver.split("//.");
		return Integer.valueOf(version[2]);
	}
	
	@Override
	public void setVersion(String version) {
		write("Default.Database.Version", version);
	}
}
