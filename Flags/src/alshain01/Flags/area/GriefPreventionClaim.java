package alshain01.Flags.area;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;

import alshain01.Flags.Flags;
import alshain01.Flags.Message;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

public class GriefPreventionClaim extends Area implements Removable, Siege, Administrator {
	protected final static String dataHeader = "GriefPreventionData.";
	protected Claim claim;
	
	// ******************************
	// Constructors
	// ******************************	
	public GriefPreventionClaim(Location location) {
		this.claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
	}
	
	public GriefPreventionClaim(long ID) {
		this.claim = GriefPrevention.instance.dataStore.getClaim(ID);
	}
	
	protected GriefPreventionClaim() { }
	
	// ******************************
	// Area Interface
	// ******************************
	@Override
	protected String getDataPath() {
		return dataHeader + getSystemID();
	}
	
	@Override
	public String getSystemID() {
		if(isArea() && claim.parent != null) {
			return String.valueOf(claim.parent.getID());
		}else if(isArea()) {
			return String.valueOf(claim.getID());
		} else {
			return null;
		}
	}
	
	@Override
	public String getAreaType() {
		return Message.GriefPrevention.get();
	}
	
	@Override
	public Set<String> getOwners() {
		return new HashSet<String>(Arrays.asList(claim.getOwnerName()));
	}
	
	@Override
	public org.bukkit.World getWorld() {
		return claim.getGreaterBoundaryCorner().getWorld();
	}
	
	@Override
	public boolean isArea() {
		return (claim != null);
	}
    
	// ******************************
	// Comparable Interface
	// ******************************
	/**
	 * 0 if the the claims are the same, 3 if they are not.
	 * 
	 * @return The value of the comparison.
	 */
	@Override
	public int compareTo(Area a) {
		if(a instanceof GriefPreventionClaim && a.getSystemID().equals(this.getSystemID())) {
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
 	   Flags.getDataStore().write(getDataPath(), (String)null);
	}
	
	// ******************************
	// Siege Interface
	// ******************************
	@Override
	public boolean isUnderSiege() {
		if (claim == null || claim.siegeData == null) { return false; }
		return true;
	}

	// ******************************
	// Admin Interface
	// ******************************
	@Override
	public boolean isAdminArea() {
		return claim.isAdminClaim();
	}
}
