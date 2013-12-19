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

package io.github.alshain01.Flags.commands;

import io.github.alshain01.Flags.Flag;
import io.github.alshain01.Flags.Flags;
import io.github.alshain01.Flags.Message;
import io.github.alshain01.Flags.SystemType;
import io.github.alshain01.Flags.area.Area;
import io.github.alshain01.Flags.area.Default;
import io.github.alshain01.Flags.area.Subdivision;
import io.github.alshain01.Flags.area.World;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;

final class Validate {
	private Validate() {}
	
	protected static boolean isArea(CommandSender cs, Area a) {
		if(a == null || !a.isArea()) {
			cs.sendMessage(Message.NoAreaError.get()
					.replaceAll("\\{AreaType\\}", SystemType.getActive().getAreaType()));
			return false;
		}
		return true;
	}
	
	protected static boolean isSubdivision(CommandSender cs, Area a) {
		if(!(a instanceof Subdivision) || !((Subdivision)a).isSubdivision()) {
			cs.sendMessage(Message.SubdivisionError.get());
			return false;
		}
		return true;
	}
	
	protected static boolean isPlayerFlag(CommandSender cs, Flag f) {
		if(f.isPlayerFlag()) { return true; }
		cs.sendMessage(Message.PlayerFlagError.get()
				.replaceAll("\\{Flag\\}", f.getName()));
		return false;
	}
	
	protected static boolean isTrustList(CommandSender cs, Set<String> tl, String a, String f) {
		if(tl != null && !tl.isEmpty()) { return true; }
		cs.sendMessage(Message.InvalidTrustError.get()
				.replaceAll("\\{AreaType\\}", a.toLowerCase())
				.replaceAll("\\{Flag\\}", f));
		return false;
	}

	protected static boolean isFlag(CommandSender cs, Flag f, String n) {
		if(f != null) { return true; }
		cs.sendMessage(Message.InvalidFlagError.get()
				.replaceAll("\\{RequestedName\\}", n)
				.replaceAll("\\{Type\\}", Message.Flag.get().toLowerCase()));
		return false;
	}
	
	protected static boolean isBundle(CommandSender cs, Set<Flag> b, String n) {
		if (b != null && !b.isEmpty()) { return true; }
		cs.sendMessage(Message.InvalidFlagError.get()
				.replaceAll("\\{RequestedName\\}", n)
				.replaceAll("\\{Type\\}", Message.Bundle.get().toLowerCase()));
		return false; 
	}
	
	protected static boolean isPermitted(Permissible p, Object o) {
		if(o instanceof Flag) {
			if(p.hasPermission(((Flag)o).getPermission())) { return true; }
			if(p instanceof CommandSender) {
				((CommandSender)p).sendMessage(Message.FlagPermError.get()
						.replaceAll("\\{Type\\}", Message.Flag.get().toLowerCase()));
			}
			return false;
		}
		
		if(o instanceof Area) {
			Area area = (Area)o;
			if (area.hasPermission(p)) { return true; }
			if(p instanceof CommandSender) {
				((CommandSender)p).sendMessage(((area instanceof World || area instanceof Default) 
						? Message.WorldPermError.get() : Message.AreaPermError.get())
							.replaceAll("\\{AreaType\\}", area.getAreaType())
							.replaceAll("\\{OwnerName\\}", area.getOwners().toArray()[0].toString())
							.replaceAll("\\{Type\\}", Message.Flag.get().toLowerCase()));
			}
			return false;
		}
		return false;
	}
	
	protected static boolean hasEconomy(CommandSender cs) {
		if(Flags.getEconomy() == null) {
			cs.sendMessage(Message.EconomyError.get());
			return false;
		}
		return true;
	}
	
	protected static boolean isBundlePermitted(Permissible p, Object o) {
		if(o instanceof String) {
			if(p.hasPermission("flags.bundle." + o)) { return true; }
			if(p instanceof CommandSender) {
				((CommandSender)p).sendMessage(Message.FlagPermError.get()
						.replaceAll("\\{Type\\}", Message.Bundle.get().toLowerCase()));
			}
			return false;
		}
		
		if(o instanceof Area) {
			Area area = (Area)o;
			if (area.hasBundlePermission(p)) { return true; }
			if(p instanceof CommandSender) {
				((CommandSender)p).sendMessage(((area instanceof World || area instanceof Default)
						? Message.WorldPermError.get() : Message.AreaPermError.get())
							.replaceAll("\\{AreaType\\}", area.getAreaType())
							.replaceAll("\\{OwnerName\\}", area.getOwners().toArray()[0].toString())
							.replaceAll("\\{Type\\}", Message.Bundle.get().toLowerCase()));
			}
			return false;
		}
		return false;
	}
	
	protected static boolean canEditBundle(Permissible p) {
		if (p.hasPermission("flags.command.bundle.edit")) { return true; }
		if(p instanceof CommandSender) {
			((CommandSender)p).sendMessage(Message.BundlePermError.get());
		}
		return false;
	}
	
	protected static boolean canEditPrice(Permissible p) {
		if (p.hasPermission("flags.command.flag.charge")) { return true; }
		if(p instanceof CommandSender) {
			((CommandSender)p).sendMessage(Message.PricePermError.get());
		}
		return false;
	}
}
