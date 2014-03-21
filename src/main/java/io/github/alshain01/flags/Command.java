package io.github.alshain01.flags;

import io.github.alshain01.flags.area.Area;
import io.github.alshain01.flags.area.Default;
import io.github.alshain01.flags.area.Subdivision;
import io.github.alshain01.flags.area.Wilderness;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

abstract class Command {
    protected enum CommandLocation {
        AREA('a'), WORLD('w'), DEFAULT('d');

        private final char alias;

        CommandLocation(char alias) {
            this.alias = alias;
        }

        public static CommandLocation get(String name) {
            for(CommandLocation c : CommandLocation.values()) {
                if(name.toLowerCase().equals(c.toString().toLowerCase()) || name.toLowerCase().equals(String.valueOf(c.alias))) {
                    return c;
                }
            }
            return null;
        }
    }

    final static class Validate {
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

        static boolean isNullOrEmpty(CommandSender sender, Collection c, Message m) {
            if (c == null || c.size() == 0) {
                sender.sendMessage(Message.NoFlagFound.get().replace("{Type}", m.get().toLowerCase()));
                return true;
            }
            return false;
        }
    }

    /**
     * Returns a page number from argument 2 or 3
     *
     * @param args Command arguments
     * @return The page number.
     */
    static int getPage(String[] args) {
        if(args.length < 2) { return 1; }

        String page;
        if(args.length >= 3) { page = args[2]; }
        else { page = args[1]; }

        // Either group or page was omitted, which one?
        try {
            return Integer.valueOf(page);
        } catch(Exception e){
            // It was a string.
            return 1;
        }
    }

    /**
     * Returns a true, false, or null value from the argument
     *
     * @param args The argument to check for a Boolean value
     * @return The Boolean value
     */
    static Boolean getValue(String[] args, int argument) {
        if (args.length > argument) {
            if(args[argument].toLowerCase().charAt(0) == 't') {
                return true;
            } else if (args[argument].toLowerCase().charAt(0) == 'f') {
                return false;
            }
        }
        return null;
    }

    /**
     * Returns a list of players starting with argument 4
     *
     * @param args Command arguments
     * @return A list of players
     */
    static Set<String> getPlayers(String[] args, int start) {
        return new HashSet<String>(Arrays.asList(args).subList(start, args.length));
    }

    static String getFormattedValue(boolean value) {
        return (value) ? Message.ValueColorTrue.get() : Message.ValueColorFalse.get();
    }

    static Area getArea(Location loc, CommandLocation location) {
        if (location == CommandLocation.DEFAULT) {
            return new Default(loc.getWorld());
        } else if (location == CommandLocation.WORLD) {
            return new Wilderness(loc.getWorld());
        } else if (location == CommandLocation.AREA) {
            Area area = CuboidType.getActive().getAreaAt(loc);
            return (area instanceof Wilderness) ? null : area;
        }
        // Invalid location selection
        return null;
    }

    static Area getArea(Player player, CommandLocation location) {
        return getArea(player.getLocation(), location);
    }
}
