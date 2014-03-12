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

package io.github.alshain01.flags.command;

import io.github.alshain01.flags.*;
import io.github.alshain01.flags.System;
import io.github.alshain01.flags.area.Area;
import io.github.alshain01.flags.area.Default;
import io.github.alshain01.flags.area.Subdivision;
import io.github.alshain01.flags.area.World;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;

final class Validate {
	private Validate() {}
	
    static boolean notValid(CommandSender cs, Area a) {
        if(a == null || !a.isArea()) {
            cs.sendMessage(Message.NoAreaError.get()
                    .replace("{AreaType}", System.getActive().getAreaType().toLowerCase()));
            return true;
        }
        return false;
    }

    static boolean notValid(CommandSender cs, Flag f, String n) {
        if(f == null) {
            cs.sendMessage(Message.InvalidFlagError.get()
                    .replace("{RequestedName}", n)
                    .replace("{Type}", Message.Flag.get().toLowerCase()));
            return true;
        }
        return false;
    }

    static boolean notPlayerFlag(CommandSender cs, Flag f) {
        if(f.isPlayerFlag()) { return false; }
        cs.sendMessage(Message.PlayerFlagError.get()
                .replace("{Flag}", f.getName()));
        return true;
    }

    static boolean notSubdividable(CommandSender cs) {
        if(System.getActive().hasSubdivisions()) { return false; }
        cs.sendMessage(Message.SubdivisionSupportError.get().replace("{System}", System.getActive().getDisplayName()));
        return true;
    }
	
	static boolean notSubdivision(CommandSender cs, Area a) {
        if(!(a instanceof Subdivision) || !((Subdivision)a).isSubdivision()) {
            cs.sendMessage(Message.SubdivisionError.get());
            return true;
        }
        return false;
	}
	
	static boolean notTrustList(CommandSender cs, Set<String> tl, String a, String f) {
        if(notTrustList(tl)) {
            cs.sendMessage(Message.InvalidTrustError.get()
                    .replace("{AreaType}", a.toLowerCase())
                    .replace("{Flag}", f));
            return true;
        }
		return false;
	}

    static boolean notTrustList(Set<String> tl) {
        try {
            org.apache.commons.lang.Validate.notNull(tl);
            org.apache.commons.lang.Validate.notEmpty(tl);
            org.apache.commons.lang.Validate.noNullElements(tl);
        } catch (IllegalArgumentException ex) {
            return true;
        }
        return false;
    }

	static boolean notBundle(CommandSender cs, Set<Flag> b, String n) {
        try {
            org.apache.commons.lang.Validate.notNull(b);
            org.apache.commons.lang.Validate.notEmpty(b);
            org.apache.commons.lang.Validate.noNullElements(b);
        } catch (IllegalArgumentException ex) {
		    cs.sendMessage(Message.InvalidFlagError.get()
                    .replace("{RequestedName}", n)
				    .replace("{Type}", Message.Bundle.get().toLowerCase()));
            return true;
        }
		return false; 
	}
	
	static boolean notPermitted(Permissible p, Flag f) {
        if(p.hasPermission((f).getPermission())) { return false; }
        if(p instanceof CommandSender) {
            ((CommandSender)p).sendMessage(Message.FlagPermError.get().replace("{Type}", Message.Flag.get().toLowerCase()));
        }
        return true;
    }

    static boolean notPermitted(Permissible p, Area a) {
        if (a.hasPermission(p)) { return false; }
        if(p instanceof CommandSender) {
            ((CommandSender)p).sendMessage(((a instanceof World || a instanceof Default)
                    ? Message.WorldPermError.get() : Message.AreaPermError.get())
                        .replace("{AreaType}", a.getSystemType().getAreaType())
                        .replace("{OwnerName}", a.getOwners().toArray()[0].toString())
                        .replace("{Type}", Message.Flag.get().toLowerCase()));
        }
        return true;
	}
	
	static boolean noEconomyInstalled(CommandSender cs) {
		if(Flags.getEconomy() == null) {
			cs.sendMessage(Message.EconomyError.get());
			return true;
		}
		return false;
	}
	
	static boolean notPermittedBundle(Permissible p, String b) {
        if(p.hasPermission("flags.bundle." + b)) { return false; }
        if(p instanceof CommandSender) {
            ((CommandSender)p).sendMessage(Message.FlagPermError.get()
                    .replace("{Type}", Message.Bundle.get().toLowerCase()));
        }
        return true;
    }

    static boolean notPermittedBundle(Permissible p, Area a) {
        if (a.hasBundlePermission(p)) { return false; }
        if(p instanceof CommandSender) {
            ((CommandSender)p).sendMessage(((a instanceof World || a instanceof Default)
                    ? Message.WorldPermError.get() : Message.AreaPermError.get())
                        .replace("{AreaType}", a.getSystemType().getAreaType())
                        .replace("{OwnerName}", a.getOwners().toArray()[0].toString())
                        .replace("{Type}", Message.Bundle.get().toLowerCase()));
        }
        return true;
	}
	
	static boolean notPermittedEditBundle(Permissible p) {
		if (p.hasPermission("flags.command.bundle.edit")) { return false; }
		if(p instanceof CommandSender) {
			((CommandSender)p).sendMessage(Message.BundlePermError.get());
		}
		return true;
	}
	
	static boolean notPermittedEditPrice(Permissible p) {
		if (p.hasPermission("flags.command.flag.charge")) { return false; }
		if(p instanceof CommandSender) {
			((CommandSender)p).sendMessage(Message.PricePermError.get());
		}
		return true;
	}
}