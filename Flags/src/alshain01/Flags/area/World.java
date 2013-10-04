package alshain01.Flags.area;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import alshain01.Flags.Flag;
import alshain01.Flags.Flags;
import alshain01.Flags.Message;

public class World extends Area {
	private final static String dataHeader = "World.";
	private org.bukkit.World world;
	
	// ******************************
	// Constructors
	// ******************************
	
	public World(org.bukkit.World world) {
		this.world = world;
	}
	
	public World(Location location) {
		this.world = location.getWorld();
	}
	
	// ******************************
	// Area Interface
	// ******************************
	@Override
	protected String getDataPath() {
		return dataHeader + getSystemID();
	}
	
	@Override
	public String getSystemID() {
		return world.getName();
	}

	@Override
	public String getAreaType() {
		return Message.World.get();
	}
	
	@Override
	public Set<String> getOwners() {
		return new HashSet<String> (Arrays.asList("world"));
	}
	
	@Override
	public org.bukkit.World getWorld() {
		return world;
	}
	
	@Override
	public boolean isArea() {
		// There is always a world.
		return true;
	}
	
	@Override
	public boolean hasPermission(Player player) {
		if (player.hasPermission("flags.flag.set.world")) { return true; }
		return false;
	}

	@Override
	public boolean hasBundlePermission(Player player) {
		if (player.hasPermission("flags.bundle.set.world")) { return true; }
		return false;
	}
	
	@Override
	public Boolean getValue(Flag flag, boolean absolute) {
    	Boolean value = null;
    	if(isArea()) { 
	    	String valueString = Flags.instance.dataStore.read(getDataPath() + "." + flag.getName() + valueFooter);
	    	
	    	if (valueString != null && valueString.toLowerCase().contains("true")) { 
	    		value = true;
	    	} else if (valueString != null) {
	    		value = false;
	    	}
    	}
    	
    	if(absolute) { return value; }
        return (value != null) ? value : flag.getDefault();
	}

	@Override
	public Set<String> getTrustList(Flag flag) {
    	Set<String> trustedPlayers = Flags.instance.dataStore.readSet(dataHeader + getSystemID() + "." + flag.getName() + trustFooter);
    	return (trustedPlayers != null) ? trustedPlayers : new HashSet<String>();
	}

	@Override
	public String getMessage(Flag flag, boolean parse) {
		String message = Flags.instance.dataStore.read(dataHeader + getSystemID() + "." + flag.getName() + messageFooter);
	 	
		if (message == null) {
			message = flag.getDefaultWorldMessage();
		}
		
		if (parse) {
			message = message
					.replaceAll("\\{AreaType\\}", getAreaType().toLowerCase())
					.replaceAll("\\{World\\}", this.world.getName());
			message = ChatColor.translateAlternateColorCodes('&', message);
		}
		return message;
	}

	// ******************************
	// Comparable Interface
	// ******************************
	@Override
	public int compareTo(Area a) {
		if(a instanceof World && a.getSystemID().equalsIgnoreCase(this.getSystemID())) {
			return 0;
		}
		return 1;
	}
}
