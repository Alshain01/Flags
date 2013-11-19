package alshain01.Flags.area;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.jzx7.regiosapi.RegiosAPI;
import net.jzx7.regiosapi.regions.Region;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import alshain01.Flags.Flags;
import alshain01.Flags.SystemType;

public class RegiosRegion extends Area implements Removable{
	final Region region;
	
	/**
	 * Creates an instance of RegiosRegion based on a Bukkit Location
	 * 
	 * @param location
	 *            The Bukkit location
	 */
	public RegiosRegion(Location location) {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Regios");
		if(plugin == null) {
			region = null;
			return;
		}
		region = ((RegiosAPI)plugin).getRegion(location);
	}
	
	/**
	 * Creates an instance of RegiosRegion based on a region name
	 * 
	 * @param name
	 *            The region name
	 */
	public RegiosRegion(String name) {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Regios");
		if(plugin == null) {
			region = null;
			return;
		}
		region = ((RegiosAPI)plugin).getRegion(name);
	}
	
	/**
	 * Gets if there is a region at the location.
	 * 
	 * @return True if a region exists at the location.
	 */
	public static boolean hasRegion(Location location) {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Regios");
		if(plugin == null) {
			return false;
		}
		return ((RegiosAPI)plugin).getRegion(location) != null;		
	}
	
	@Override
	public int compareTo(Area a) {
		return a instanceof RegiosRegion && a.getSystemID().equals(getSystemID()) ? 0 : 3;
	}

	@Override
	public String getAreaType() {
		return SystemType.REGIOS.getAreaType();
	}
	
	public Region getRegion() {
		return region;
	}

	@Override
	public Set<String> getOwners() {
		return new HashSet<String>(Arrays.asList(region.getOwner()));
	}

	@Override
	public SystemType getType() {
		return SystemType.REGIOS;
	}

	@Override
	public String getSystemID() {
		return region.getName();
	}

	@Override
	public World getWorld() {
		return Bukkit.getWorld(region.getWorld().getName());
	}

	@Override
	public boolean isArea() {
		return region != null;
	}

	@Override
	public void remove() {
		Flags.getDataStore().remove(this);
	}
}
