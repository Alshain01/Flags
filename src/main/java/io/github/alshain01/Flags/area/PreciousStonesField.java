/* Copyright 2013 Kevin Seiden. All rights reserved.

 This works is licensed under the Creative Commons Attribution-NonCommercial 3.0

 You are Free to:
    to Share: to copy, distribute and transmit the work
    to Remix: to adapt the work

 Under the following conditions:
    Attribution: You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
    Non-commercial: You may not use this work for commercial purposes.

 With the understanding that:
    Waiver: Any of the above conditions can be waived if you get permission from the copyright holder.
    Public Domain: Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
    Other Rights: In no way are any of the following rights affected by the license:
        Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
        The author's moral rights;
        Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.

 Notice: For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
 http://creativecommons.org/licenses/by-nc/3.0/
 */

package io.github.alshain01.Flags.area;

import io.github.alshain01.Flags.Flags;
import io.github.alshain01.Flags.SystemType;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class PreciousStonesField extends Area implements Subdivision, Removable {
	private Field field;
	
	public PreciousStonesField(Location location) {
		if(!PreciousStones.API().isFieldProtectingArea(FieldFlag.ALL, location)){
			field = null;
			return;
		}
		
		List<Field> fields = PreciousStones.API().getFieldsProtectingArea(FieldFlag.ALL, location);
		if(fields.size() == 1) {
			this.field = fields.get(0);
			return;
		}
		
		for(Field field : fields) {
			if (field.isChild()) {
				this.field = field;
				return;
			}
		}
	}
	
	public PreciousStonesField(World world, long ID) {
		 List<Field> fields = PreciousStones.getInstance().getStorageManager().getFields(world.getName());
		 for(Field field : fields) {
			 if(field.getId() == ID) {
				 this.field = field;
				 return;
			 }
		 }
		 Collection<Field> cuboidFields = PreciousStones.getInstance().getStorageManager().getCuboidFields(world.getName());
		 for(Field field : cuboidFields) {
			 if(field.getId() == ID) {
				 this.field = field;
				 return;
			 }
		 }
	}
	
	// This is private because it doesn't currently check parentOnly,
	// should only be used when you know parentOnly will be true
	// Works for now, but needs enhancement
	private PreciousStonesField(Location location, boolean parentOnly) {
		List<Field> fields = PreciousStones.API().getFieldsProtectingArea(FieldFlag.ALL, location);
		for(Field field : fields) {
			if(field.isParent()) {
				this.field = field;
				return;
			}
		}
	}
	
	public Field getField() {
		return field;
	}
	
	public static boolean hasField(Location location) {
		return PreciousStones.API().isFieldProtectingArea(FieldFlag.ALL, location);
	}
	
	@Override
	public int compareTo(Area a) {
		if (!(a instanceof PreciousStonesField)) {
			return 3;
		}
		
		Field testField = ((PreciousStonesField)a).getField();
		if(field.equals(testField)) {
			return 0;
		} else if (field.isChild() && field.getParent().equals(testField)) {
			return -1;
		} else if (testField.isChild() && testField.getParent().equals(field)) {
			return 1;
		} else if (field.isChild() && testField.isChild() && field.getParent().equals(testField.getParent())) {
			return 2;
		}
		return 3;
	}

	@Override
	public String getAreaType() {
		return SystemType.PRECIOUSSTONES.getAreaType();
	}

	@Override
	public Set<String> getOwners() {
		return new HashSet<String>(Arrays.asList(field.getOwner()));
	}

	@Override
	public SystemType getType() {
		return SystemType.PRECIOUSSTONES;
	}

	@Override
	public String getSystemID() {
		if (isArea() && field.isChild()) {
			return String.valueOf(field.getParent().getId());
		} else if (isArea()) {
			return String.valueOf(field.getId());
		} else {
			return null;
		}
	}

	@Override
	public World getWorld() {
		return Bukkit.getWorld(field.getWorld());
	}

	@Override
	public boolean isArea() {
		return field != null;
	}

	@Override
	public void remove() {
		Flags.getDataStore().remove(this);
	}

	@Override
	public String getSystemSubID() {
		return field !=null && field.isChild() ? String.valueOf(field.getId()) : null;
	}

	@Override
	public boolean isInherited() {
		return field != null && field.isChild() && Flags.getDataStore().readInheritance(this);
	}

	@Override
	public boolean isSubdivision() {
		return field.isChild();
	}

	@Override
	public void setInherited(Boolean value) {
		if (field == null || !field.isChild()) {
			return;
		}

		Flags.getDataStore().writeInheritance(this, value);
	}

	@Override
	public boolean isParent(Area area) {
		return area instanceof PreciousStonesField && field != null
                && field.isParent() && field.getChildren().contains(((PreciousStonesField)area).getField());
	}

	@Override
	public Area getParent() {
		if(!field.isChild()) { return null; }
		return new PreciousStonesField(field.getParent().getLocation(), true);
	}

}