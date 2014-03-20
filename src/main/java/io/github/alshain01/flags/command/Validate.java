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
import io.github.alshain01.flags.area.*;

import java.util.Collection;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;

final class Validate {
	private Validate() {}
	
    static boolean notArea(CommandSender cs, Area area) {
        if(area != null && area.isArea()) { return false; }
        cs.sendMessage(Message.NoAreaError.get()
                .replace("{AreaType}", CuboidType.getActive().getCuboidName().toLowerCase()));
        return true;
    }

    static boolean notFlag(CommandSender sender, Flag flag, String requestedFlag) {
        if(flag != null) { return false; }
        sender.sendMessage(Message.InvalidFlagError.get()
                .replace("{RequestedName}", requestedFlag)
                .replace("{Type}", Message.Flag.get().toLowerCase()));
        return true;
    }

    static boolean notPermittedFlag(Permissible p, Area a) {
        if (a.hasPermission(p)) { return false; }
        if(p instanceof CommandSender) {
            ((CommandSender)p).sendMessage(((a instanceof Wilderness || a instanceof Default)
                    ? Message.WorldPermError.get() : Message.AreaPermError.get())
                    .replace("{AreaType}", a.getCuboidType().getCuboidName())
                    .replace("{OwnerName}", a.getOwners().toArray()[0].toString())
                    .replace("{Type}", Message.Flag.get().toLowerCase()));
        }
        return true;
    }

    static boolean notPermittedFlag(Permissible p, Flag f) {
        if(p.hasPermission((f).getPermission())) { return false; }
        if(p instanceof CommandSender) {
            ((CommandSender)p).sendMessage(Message.FlagPermError.get().replace("{Type}", Message.Flag.get().toLowerCase()));
        }
        return true;
    }

    static boolean notPermittedFlag(CommandSender sender, Area area, Flag flag, String request) {
        return notArea(sender, area)
                || notFlag(sender, flag, request)
                || notPermittedFlag(sender, flag)
                || notPermittedFlag(sender, area);
    }

    static boolean notBundle(CommandSender sender, String bundle) {
        if (Bundle.isBundle(bundle)) { return false; }
        sender.sendMessage(Message.InvalidFlagError.get()
                .replace("{RequestedName}", bundle)
                .replace("{Type}", Message.Bundle.get().toLowerCase()));
        return true;
    }

    private static boolean notPermittedBundle(Permissible p, String bundleName) {
        if(p.hasPermission("flags.bundle." + bundleName)) { return false; }
        if(p instanceof CommandSender) {
            ((CommandSender)p).sendMessage(Message.FlagPermError.get()
                    .replace("{Type}", Message.Bundle.get().toLowerCase()));
        }
        return true;
    }

    private static boolean notPermittedBundle(Permissible p, Area area) {
        if (area.hasBundlePermission(p)) { return false; }
        if(p instanceof CommandSender) {
            ((CommandSender)p).sendMessage(((area instanceof Wilderness || area instanceof Default)
                    ? Message.WorldPermError.get() : Message.AreaPermError.get())
                    .replace("{AreaType}", area.getCuboidType().getCuboidName())
                    .replace("{OwnerName}", area.getOwners().toArray()[0].toString())
                    .replace("{Type}", Message.Bundle.get().toLowerCase()));
        }
        return true;
    }

    static boolean notPermittedBundle(CommandSender sender, Area area, String bundleName) {
        return notArea(sender, area)
                || notBundle(sender, bundleName)
                || notPermittedBundle(sender, bundleName)
                || notPermittedBundle(sender, area);
    }

    static boolean notPermittedEditBundle(Permissible p) {
        if (p.hasPermission("flags.command.bundle.edit")) { return false; }
        if(p instanceof CommandSender) {
            ((CommandSender)p).sendMessage(Message.BundlePermError.get());
        }
        return true;
    }

    static boolean notPlayerFlag(CommandSender cs, Flag f) {
        if(f.isPlayerFlag()) { return false; }
        cs.sendMessage(Message.PlayerFlagError.get()
                .replace("{Flag}", f.getName()));
        return true;
    }

    static boolean notSubdividable(CommandSender cs) {
        if(CuboidType.getActive().hasSubdivisions()) { return false; }
        cs.sendMessage(Message.SubdivisionSupportError.get().replace("{System}", CuboidType.getActive().getDisplayName()));
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

	static boolean noEconomyInstalled(CommandSender cs) {
		if(Flags.getEconomy() == null) {
			cs.sendMessage(Message.EconomyError.get());
			return true;
		}
		return false;
	}
	
	static boolean notPermittedEditPrice(Permissible p) {
		if (p.hasPermission("flags.command.flag.charge")) { return false; }
		if(p instanceof CommandSender) {
			((CommandSender)p).sendMessage(Message.PricePermError.get());
		}
		return true;
	}

    static boolean notNullOrEmpty(CommandSender sender, Collection c, Message m) {
        if (c == null || c.size() == 0) {
            sender.sendMessage(Message.NoFlagFound.get().replace("{Type}", m.get()));
            return true;
        }
        return false;
    }
}
