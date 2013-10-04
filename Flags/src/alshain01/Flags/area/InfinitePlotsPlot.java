package alshain01.Flags.area;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import alshain01.Flags.Flags;
import alshain01.Flags.Message;
import uk.co.jacekk.bukkit.infiniteplots.InfinitePlots;
import uk.co.jacekk.bukkit.infiniteplots.plot.Plot;
import uk.co.jacekk.bukkit.infiniteplots.plot.PlotLocation;

public class InfinitePlotsPlot extends Area implements Removable {
	protected final static String dataHeader = "InfinitePlotsData";
	protected Plot plot;
	
	// ******************************
	// Constructors
	// ******************************
	public InfinitePlotsPlot(Location location) {
		this.plot = InfinitePlots.getInstance().getPlotManager().getPlotAt(PlotLocation.fromWorldLocation(location));
	}
	
	public InfinitePlotsPlot(String playerName, String plotName) {
		this.plot = InfinitePlots.getInstance().getPlotManager().getPlotByName(playerName, plotName);
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
		return plot.getAdmin() + "." + plot.getName();
	}

	@Override
	public String getAreaType() {
		return Message.InfinitePlots.get();
	}

	@Override
	public Set<String> getOwners() {
		return new HashSet<String>(Arrays.asList(plot.getAdmin()));
	}

	@Override
	public World getWorld() {
		return plot.getLocation().getWorld();
	}

	@Override
	public boolean isArea() {
		if(plot != null) { return true; }
		return false;
	}

	@Override
	public boolean hasPermission(Player player) {
		if (getOwners().contains(player.getName())) {
			if (player.hasPermission("flags.flag.set")) { return true; }
			return false;
		}
		
		if (player.hasPermission("flags.flag.set.others")) { return true; }
		return false;
	}

	@Override
	public boolean hasBundlePermission(Player player) {
		if (getOwners().contains(player.getName())) {
			if (player.hasPermission("flags.bundle.set")) {	return true; }
			return false;
		}
		
		if (player.hasPermission("flags.bundle.set.others")) { return true;	}
		return false;
	}
	
	// ******************************
	// Comparable Interface
	// ******************************
	/**
	 * 0 if the the plots are the same, 3 if they are not.
	 * 
	 * @return The value of the comparison.
	 */	
	@Override
	public int compareTo(Area a) {
		if(a instanceof InfinitePlotsPlot && a.getSystemID().equalsIgnoreCase(this.getSystemID())) {
			return 0;
		}

		return 3;
	}
	
	// ******************************
	// Removable Interface
	// ******************************
	/**
	 * Permanently removes the area from the data store
	 * USE CAUTION!
	 */
	@Override
	public void remove() {
 	   Flags.instance.dataStore.write(getDataPath(), (String)null);
	}
}
