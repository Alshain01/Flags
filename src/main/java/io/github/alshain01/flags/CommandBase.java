package io.github.alshain01.flags;

import io.github.alshain01.flags.api.Flag;
import io.github.alshain01.flags.api.FlagsAPI;
import io.github.alshain01.flags.api.area.Area;
import io.github.alshain01.flags.api.area.Ownable;
import io.github.alshain01.flags.api.area.Subdividable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

abstract class CommandBase {
    protected enum CommandLocation {
        AREA('a'), WILDERNESS('w'), DEFAULT('d');

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
            cs.sendMessage(Message.NO_AREA_ERROR.get()
                    .replace("{AreaType}", FlagsAPI.getAreaPlugin().getCuboidName().toLowerCase()));
            return true;
        }

        static boolean notFlag(CommandSender sender, Flag flag, String requestedFlag) {
            if(flag != null) { return false; }
            sender.sendMessage(Message.INVALID_FLAG_ERROR.get()
                    .replace("{RequestedName}", requestedFlag)
                    .replace("{Type}", Message.FLAG.get().toLowerCase()));
            return true;
        }

        static boolean notPermittedFlag(Permissible p, Area a) {
            if (a.hasFlagPermission(p)) { return false; }
            if(p instanceof CommandSender) {
                String message = a instanceof AreaWilderness || a instanceof AreaDefault
                        ? Message.WILDERNESS_PERM_ERROR.get() : Message.AREA_PERM_ERROR.get()
                        .replace("{AreaType}", a.getCuboidPlugin().getCuboidName())
                        .replace("{Type}", Message.FLAG.get().toLowerCase());

                if(a instanceof Ownable) {
                    message = message.replace("{OwnerName}", ((Ownable)a).getOwnerName().toArray()[0].toString());
                } else {
                    message = message.replace("{OwnerName}", "an administrator");
                }
                ((CommandSender)p).sendMessage(message);
            }
            return true;
        }

        static boolean notPermittedFlag(Permissible p, Flag f) {
            if(p.hasPermission((f).getPermission())) { return false; }
            if(p instanceof CommandSender) {
                ((CommandSender)p).sendMessage(Message.FLAG_PERM_ERROR.get().replace("{Type}", Message.FLAG.get().toLowerCase()));
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
            if (FlagsAPI.isBundle(bundle)) { return false; }
            sender.sendMessage(Message.INVALID_FLAG_ERROR.get()
                    .replace("{RequestedName}", bundle)
                    .replace("{Type}", Message.BUNDLE.get().toLowerCase()));
            return true;
        }

        private static boolean notPermittedBundle(Permissible p, String bundleName) {
            if(p.hasPermission("flags.bundle." + bundleName)) { return false; }
            if(p instanceof CommandSender) {
                ((CommandSender)p).sendMessage(Message.FLAG_PERM_ERROR.get()
                        .replace("{Type}", Message.BUNDLE.get().toLowerCase()));
            }
            return true;
        }

        private static boolean notPermittedBundle(Permissible p, Area area) {
            if (area.hasBundlePermission(p)) { return false; }
            if(p instanceof CommandSender) {
                String message = ((area instanceof AreaWilderness || area instanceof AreaDefault)
                        ? Message.WILDERNESS_PERM_ERROR.get() : Message.AREA_PERM_ERROR.get())
                        .replace("{AreaType}", area.getCuboidPlugin().getCuboidName())
                        .replace("{Type}", Message.BUNDLE.get().toLowerCase());

                if(area instanceof Ownable) {
                    message = message.replace("{OwnerName}", ((Ownable)area).getOwnerName().toArray()[0].toString());
                } else {
                    message = message.replace("{OwnerName}", "an administrator");
                }

                ((CommandSender)p).sendMessage(message);
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
                ((CommandSender)p).sendMessage(Message.BUNDLE_PERM_ERROR.get());
            }
            return true;
        }

        static boolean notPlayerFlag(CommandSender cs, Flag f) {
            if(f.isPlayerFlag()) { return false; }
            cs.sendMessage(Message.PLAYER_FLAG_ERROR.get()
                    .replace("{Flag}", f.getName()));
            return true;
        }

        static boolean notSubdividable(CommandSender cs) {
            if(FlagsAPI.getAreaPlugin().isSubdividable()) { return false; }
            cs.sendMessage(Message.SUBDIVISION_SUPPORT_ERROR.get().replace("{System}", FlagsAPI.getAreaPlugin().getDisplayName()));
            return true;
        }

        static boolean notSubdivision(CommandSender cs, Area a) {
            if(!(a instanceof Subdividable) || !((Subdividable)a).isSubdivision()) {
                cs.sendMessage(Message.SUBDIVISION_ERROR.get());
                return true;
            }
            return false;
        }

        static boolean notTrustList(CommandSender cs, Set<String> tl, String a, String f) {
            if(notTrustList(tl)) {
                cs.sendMessage(Message.INVALID_TRUST_ERROR.get()
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
                cs.sendMessage(Message.ECONOMY_ERROR.get());
                return true;
            }
            return false;
        }

        static boolean notPermittedEditPrice(Permissible p) {
            if (p.hasPermission("flags.command.flag.charge")) { return false; }
            if(p instanceof CommandSender) {
                ((CommandSender)p).sendMessage(Message.PRICE_PERM_ERROR.get());
            }
            return true;
        }

        static boolean isNullOrEmpty(CommandSender sender, Collection c, Message m) {
            if (c == null || c.size() == 0) {
                sender.sendMessage(Message.NO_FLAG_FOUND.get().replace("{Type}", m.get().toLowerCase()));
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
        return (value) ? Message.VALUE_COLOR_TRUE.get() : Message.VALUE_COLOR_FALSE.get();
    }

    static Area getArea(Location loc, CommandLocation location) {
        if (location == CommandLocation.DEFAULT) {
            return new AreaDefault(loc.getWorld());
        } else if (location == CommandLocation.WILDERNESS) {
            return new AreaWilderness(loc.getWorld());
        } else if (location == CommandLocation.AREA) {
            if(FlagsAPI.hasArea(loc)) return FlagsAPI.getAreaAt(loc);
        }
        // Invalid location selection
        return null;
    }

    static Area getArea(Player player, CommandLocation location) {
        return getArea(player.getLocation(), location);
    }

    static Set<Player> getPlayerList(Player player, Set<String> playerNames) {
        // Convert the strings to players
        Set<String> failedPlayers = new HashSet<String>();
        Set<Player> players = new HashSet<Player>();
        for(String name : playerNames) {
            Player p = Bukkit.getPlayer(name);
            if(p != null) {
                players.add(p);
            } else {
                failedPlayers.add(name);
            }
        }

        // Send a message letting them know there was an issue
        if(failedPlayers.size() > 0) {
            boolean first = true;
            StringBuilder failedList = new StringBuilder();
            for(String name : failedPlayers) {
                if(!first) {
                    failedList.append(", ");
                } else {
                    first = false;
                }
                failedList.append(name);
            }

            player.sendMessage(Message.PLAYER_NOT_FOUND_ERROR.get().replace("{Player}", failedList.toString()));
        }
        return players;
    }
}
