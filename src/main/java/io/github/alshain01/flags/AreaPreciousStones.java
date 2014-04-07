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

package io.github.alshain01.flags;

import java.util.*;

import io.github.alshain01.flags.api.CuboidPlugin;
import io.github.alshain01.flags.api.area.Area;
import io.github.alshain01.flags.api.area.Nameable;
import io.github.alshain01.flags.api.area.Ownable;
import io.github.alshain01.flags.api.area.Subdividable;
import io.github.alshain01.flags.api.exception.InvalidAreaException;
import io.github.alshain01.flags.api.exception.InvalidSubdivisionException;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Class for creating areas to manage a PreciousStones Field.
 */
final public class AreaPreciousStones extends AreaRemovable implements Nameable, Ownable, Subdividable {
	private Field field;

    /**
     * Creates an instance of AreaPreciousStones based on a Bukkit Location
     *
     * @param location
     *            The Bukkit location
     */
	public AreaPreciousStones(Location location) {
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

    /**
     * Creates an instance of AreaPreciousStones based on a world and field ID
     *
     * @param ID
     *            The claim ID
     */
	public AreaPreciousStones(World world, long ID) {
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

    /**
     * Creates an instance of AreaPreciousStones based on a world and field object
     *
     * @param field
     *            The field object
     */
    private AreaPreciousStones(Field field) {
        this.field = field;
    }

    /**
     * Gets if there is a field at the location.
     *
     * @return True if a field exists at the location.
     */
    public static boolean hasField(Location location) {
        return PreciousStones.API().isFieldProtectingArea(FieldFlag.ALL, location);
    }

    @Override
    public UUID getUniqueId() {
        if (isArea()) return null;
        throw new InvalidAreaException();
    }

    @Override
    public String getId() {
        if (isArea()) return String.valueOf(field.getId());
        throw new InvalidAreaException();
    }

    @Override
    public CuboidPlugin getCuboidPlugin() {
        return CuboidPlugin.PRECIOUSSTONES;
    }

    @Override
    public String getName() {
        if (isArea()) return field.getName();
        throw new InvalidAreaException();
    }

    @Override
    public Set<UUID> getOwnerUniqueId() {
        //TODO: Waiting on PreciousStones
        return new HashSet<UUID>(Arrays.asList(UUID.randomUUID()));
    }

    @Override
    public Set<String> getOwnerName() {
        if (isArea()) return new HashSet<String>(Arrays.asList(field.getOwner()));
        throw new InvalidAreaException();
    }

    @Override
	public World getWorld() {
        if (isArea()) return Bukkit.getWorld(field.getWorld());
        throw new InvalidAreaException();
    }

	@Override
	public boolean isArea() {
        return field != null;
    }

    @Override
    public boolean isSubdivision() {
        if (isArea()) return field.isChild();
        throw new InvalidAreaException();
    }

    @Override
    public boolean isParent(Area area) {
        if (isSubdivision()) {
            Validate.notNull(area);
            return area instanceof AreaPreciousStones && field.isParent()
                    && field.getChildren().contains(((AreaPreciousStones) area).field);
        }
        throw new InvalidSubdivisionException();
    }

    @Override
    public Area getParent() {
        if (isSubdivision()) return new AreaPreciousStones(field.getParent());
        throw new InvalidSubdivisionException();
    }

	@Override
	public boolean isInherited() {
        if (isSubdivision()) return Flags.getDataStore().readInheritance(this);
        throw new InvalidSubdivisionException();
	}

	@Override
	public void setInherited(boolean value) {
        if (isSubdivision()) {
            Flags.getDataStore().writeInheritance(this, value);
        }
        throw new InvalidSubdivisionException();
	}
}