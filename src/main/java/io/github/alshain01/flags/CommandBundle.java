package io.github.alshain01.flags;

import io.github.alshain01.flags.api.Flag;
import io.github.alshain01.flags.api.FlagsAPI;
import io.github.alshain01.flags.api.area.Area;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.*;

final class CommandBundle extends CommandBase implements CommandExecutor {
    private enum BundleCommandType {
        SET('S', 4, 0, true, true, "Set <area|wilderness|default> <bundle> <true|false>"),
        GET('G', 3, 0, true, true, "Get <area|wilderness|default> <bundle>"),
        REMOVE('R', 3, 0, true, true, "Remove <area|wilderness|default> <bundle>"),
        TRUST('T', 4, -1, true, true, "Trust <area|wilderness|default> <bundle> <player> [player]..."),
        DISTRUST('D', 3, -1, true, true, "Distrust <area|wilderness|default> <bundle> [player] [player]..."),
        HELP('H', 1, 1, false, null, "Help [page]"),
        ADD('A', 3, -1, false, true, "Add <bundle> <flag> [flag]..."),
        CUT ('C', 3, -1, false, true, "Cut <bundle> <flag> [flag]..."),
        ERASE ('E', 2, 0, false, true, "Erase <bundle>");

        final char alias;
        final int requiredArgs;
        final int optionalArgs; // -1 for infinite
        final boolean requiresLocation;
        final Boolean requiresBundle; // null if bundle isn't even an optional arg.
        final String help;

        BundleCommandType(char alias, int requiredArgs, int optionalArgs, boolean hasLocation, Boolean requiresBundle, String help) {
            this.alias = alias;
            this.requiredArgs = requiredArgs;
            this.optionalArgs = optionalArgs;
            this.help = help;
            this.requiresLocation = hasLocation;
            this.requiresBundle = requiresBundle;
        }

        static BundleCommandType get(String name) {
            if(name.length() > 1)  { return valueOf(name.toUpperCase()); }
            return getByAlias(name.toUpperCase().toCharArray()[0]);
        }

        static BundleCommandType getByAlias(char alias) {
            for(BundleCommandType c : BundleCommandType.values()) {
                if(alias == c.alias) { return c; }
            }
            return null;
        }

        String getHelp() {
            return "/bundle " + this.help;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (args.length < 1) {
            if(sender instanceof Player) {
                sender.sendMessage(getUsage((Player) sender, FlagsAPI.getAreaAt(((Player) sender).getLocation())));
                return true;
            }
            return false;
        }

        final BundleCommandType command = BundleCommandType.get(args[0]);
        if(command == null) {
            if(sender instanceof Player) {
                sender.sendMessage(getUsage((Player) sender, FlagsAPI.getAreaAt(((Player) sender).getLocation())));
                return true;
            }
            return false;
        }

        CommandLocation location = null;
        String bundle = null;

        // Check argument length (-1 means infinite optional args)
        if(args.length < command.requiredArgs
                || (command.optionalArgs > 0 && args.length > command.requiredArgs + command.optionalArgs)) {
            sender.sendMessage(command.getHelp());
            return true;
        }

        // Check the command location for those that apply
        if(command.requiresLocation) {
            location = CommandLocation.get(args[1]);
            if(location == null) {
                sender.sendMessage(command.getHelp());
                return true;
            }

            // Location based commands require the player to be in the world
            if(!(sender instanceof Player)) {
                sender.sendMessage(Message.NO_CONSOLE_ERROR.get());
                return true;
            }
        }

        if(command.requiresBundle != null) {
            if(command.requiresBundle || args.length >= 3) {
                bundle = (command.requiresLocation) ? args[2] : args[1];
            }
        }

        // Process the command
        Set<String> players = new HashSet<String>();
        switch(command) {
            case HELP:
                help(sender, getPage(args));
                break;
            case GET:
                get((Player)sender, location, bundle);
                break;
            case SET:
                Boolean value = getValue(args, 3);
                if(value != null) { set((Player)sender, location, bundle, getValue(args, 3)); }
                break;
            case REMOVE:
                remove((Player)sender, location, bundle);
                break;
            case TRUST:
                players = getPlayers(args, command.requiredArgs - 1);
                trust((Player)sender, location, bundle, players);
                break;
            case DISTRUST:
                if(args.length > command.requiredArgs) {
                    players = getPlayers(args, command.requiredArgs); } // Players can be omitted to distrust all
                distrust((Player)sender, location, bundle, players);
                break;
            case ADD:
                add(sender, bundle, new HashSet<String>(Arrays.asList(args).subList(2, args.length)));
                break;
            case CUT:
                delete(sender, bundle, new HashSet<String>(Arrays.asList(args).subList(2, args.length)));
                break;
            case ERASE:
                erase(sender, bundle);
                break;
        }
        return true;
    }

    private static String getUsage(Player player, Area area) {
        //usage: /bundle <get|set|remove|add|delete|erase|help>
        // We can assume if we get this far, the player has read access to the command.
        StringBuilder usage = new StringBuilder("/bundle <get");
        if(player.hasPermission("flags.command.bundle.set") && area.hasBundlePermission(player)) {
            usage.append("|set|remove|trust|distrust");
        }

        if(player.hasPermission("flags.command.bundle.edit")) {
            usage.append("|add|cut|erase");
        }

        usage.append("|help>");

        return usage.toString();
    }

    private static void get(Player player, CommandLocation location, String bundleName) {
        final Area area = getArea(player, location);

        if(Validate.notPermittedBundle(player, area, bundleName)) { return; }
        final Collection<Flag> bundle = FlagsAPI.getBundle(bundleName);

        for(Flag flag : bundle) {
            player.sendMessage(Message.GET_BUNDLE.get()
                    .replace("{Bundle}", flag.getName())
                    .replace("{Value}", getFormattedValue(area.getState(flag))));
        }
    }

    private static void set(Player player, CommandLocation location, String bundleName, Boolean value) {
        boolean success = true;
        final Area area = getArea(player, location);

        if(Validate.notPermittedBundle(player, area, bundleName)) { return; }
        final Collection<Flag> bundle = FlagsAPI.getBundle(bundleName);

        for(Flag flag : bundle) {
            if(!area.setState(flag, value, player)) { success = false; }
        }

        player.sendMessage((success ? Message.SET_BUNDLE.get() : Message.SET_MULTIPLE_FLAGS_ERROR.get())
                .replace("{AreaType}", area.getAreaPlugin().getCuboidName().toLowerCase())
                .replace("{Bundle}", bundleName)
                .replace("{Value}", getFormattedValue(value).toLowerCase()));
    }

    private static void remove(Player player, CommandLocation location, String bundleName) {
        boolean success = true;
        final Area area = getArea(player, location);

        if(Validate.notPermittedBundle(player, area, bundleName)) { return; }
        final Collection<Flag> bundle = FlagsAPI.getBundle(bundleName);

        for (Flag flag : bundle) {
            if (!area.setState(flag, null, player)) { success = false; }
        }

        player.sendMessage((success ? Message.REMOVE_BUNDLE.get() : Message.REMOVE_ALL_FLAGS.get())
                .replace("{AreaType}", area.getAreaPlugin().getCuboidName().toLowerCase())
                .replace("{Bundle}", bundleName));
    }

    private static boolean trust(Player player, CommandLocation location, String bundleName, Set<String> trustees) {
        if(trustees.size() == 0) { return false; }

        boolean success = true;
        Area area = getArea(player, location);

        if(Validate.notPermittedBundle(player, area, bundleName)) { return true; }

        Set<Permission> permissions = new HashSet<Permission>();
        Set<OfflinePlayer> playerList = new HashSet<OfflinePlayer>();
        for(String t : trustees) {
            if(t.contains(".")) {
                permissions.add(new Permission(t));
            } else {
                OfflinePlayer p = PlayerCache.getOfflinePlayer(t);
                if (p != null) {
                    playerList.add(p);
                } else {
                    success = false;
                }
            }
        }

        for(Flag f : FlagsAPI.getBundle(bundleName)) {
            if(!f.isPlayerFlag()) { continue; }

            for(OfflinePlayer p : playerList) {
                if(!area.setTrust(f, p, player)) { success = false; }
            }

            for(Permission p : permissions) {
                if(!area.setTrust(f, p, player)) { success = false; }
            }
        }

        player.sendMessage((success ? Message.SET_TRUST.get() : Message.SET_TRUST_ERROR.get())
                .replace("{AreaType}", area.getAreaPlugin().getCuboidName().toLowerCase())
                .replace("{Flag}", bundleName));
        return true;
    }

    private static void distrust(Player player, CommandLocation location, String bundleName, Set<String> trustees) {
        boolean success = true;
        Area area = getArea(player, location);

        if(Validate.notPermittedBundle(player, area, bundleName)) { return; }

        Collection<Permission> permissions = new HashSet<Permission>();
        Collection<OfflinePlayer> playerList = new HashSet<OfflinePlayer>();

        for(String t : trustees) {
            if(t.contains(".")) {
                permissions.add(new Permission(t));
            } else {
                OfflinePlayer p = PlayerCache.getOfflinePlayer(t);
                if (p != null) {
                    playerList.add(p);
                } else {
                    success = false;
                }
            }
        }

        for(Flag f : FlagsAPI.getBundle(bundleName)) {
            // Did the user request the trust list be cleared?
            if(trustees.isEmpty()) {
                playerList = area.getPlayerTrust(f);
                permissions = area.getPermissionTrust(f);
            }

            for (OfflinePlayer p : playerList) {
                if (!area.removeTrust(f, p, player)) {
                    success = false;
                }
            }

            for (Permission p : permissions) {
                if (!area.removeTrust(f, p, player)) {
                    success = false;
                }
            }
        }

        player.sendMessage((success ? Message.REMOVE_TRUST.get() : Message.REMOVE_TRUST_ERROR.get())
                .replace("{AreaType}", area.getAreaPlugin().getCuboidName().toLowerCase())
                .replace("{Flag}", bundleName));
    }

    private static void add(CommandSender sender, String bundleName, Set<String> flags) {
        if(Validate.notPermittedEditBundle(sender)){ return; }

        Flag flag;
        Collection<Flag> bundle;

        if(FlagsAPI.isBundle(bundleName)) {
            bundle = FlagsAPI.getBundle(bundleName);
        } else {
            bundle = new HashSet<Flag>();
        }

        for(String f : flags) {
            flag = FlagsAPI.getRegistrar().getFlagIgnoreCase(f);
            if (flag == null) {
                sender.sendMessage(Message.ADD_BUNDLE_ERROR.get());
                return;
            }
            bundle.add(flag);
        }

        FlagsAPI.setBundle(bundleName, bundle);
        sender.sendMessage(Message.UPDATE_BUNDLE.get()
                .replace("{Bundle}", bundleName));
    }

    private static void delete(CommandSender sender, String bundleName, Set<String> flags) {
        if(Validate.notPermittedEditBundle(sender)){ return; }
        if(Validate.notBundle(sender, bundleName)) { return; }

        boolean success = true;
        Collection<Flag> bundle = FlagsAPI.getBundle(bundleName.toLowerCase());

        for(String s : flags) {
            Flag flag = FlagsAPI.getRegistrar().getFlag(s);
            if (flag == null || !bundle.remove(flag)) {
                success = false; }
        }
        FlagsAPI.setBundle(bundleName, bundle);

        sender.sendMessage((success ? Message.UPDATE_BUNDLE.get() : Message.REMOVE_ALL_FLAGS_ERROR.get())
                .replace("{Bundle}", bundleName));
    }

    private static void erase(CommandSender sender, String bundleName) {
        if(Validate.notPermittedEditBundle(sender)){ return; }

        Collection<String> bundles = FlagsAPI.getBundleNames();
        if (bundles == null || bundles.size() == 0 || !bundles.contains(bundleName)) {
            sender.sendMessage(Message.ERASE_BUNDLE_ERROR.get());
            return;
        }

        FlagsAPI.setBundle(bundleName, null);
        sender.sendMessage(Message.ERASE_BUNDLE.get().replace("{Bundle}", bundleName));
    }

    private static void help (CommandSender sender, int page) {
        int startIndex, endIndex, totalPages;
        List<String> bundles = new ArrayList<String>(FlagsAPI.getBundleNames());

        if (Validate.isNullOrEmpty(sender, bundles, Message.BUNDLE)) { return; }
        Collections.sort(bundles);

        // Get total pages: 1 header per page
        // 9 flags per page, except on the first which has a usage line and 8 flags
        // Add the last page, if the last page is not full (less than 9 flags)
        totalPages = ((bundles.size() + 1) / 9);
        if ((bundles.size() + 1) % 9 != 0) { totalPages++; }

        //Check the page number requested
        if (page < 1 || page > totalPages) { page = 1; }

        // Find the sub list for this page
        startIndex = ((page - 1) * 9) - 1;
        if(startIndex < 0) { startIndex = 0; }

        endIndex = startIndex + page > 1 ? 9 : 8;
        if(endIndex > bundles.size()) { endIndex = bundles.size(); }

        bundles = bundles.subList(startIndex, endIndex);

        // Send the help header
        sender.sendMessage(Message.HELP_HEADER.get()
                .replace("{Type}", Message.BUNDLE.get())
                .replace("{Group}", Message.INDEX.get())
                .replace("{Page}", String.valueOf(page))
                .replace("{TotalPages}", String.valueOf(totalPages))
                .replace("{Type}", Message.BUNDLE.get()));

        // Send the usage line.  Displays only on the first page.
        if (page == 1) {
            sender.sendMessage(Message.HELP_INFO.get()
                    .replace("{Type}", Message.BUNDLE.get().toLowerCase()));
        }

        // Send the help lines
        for(String b : bundles) {
            Collection<Flag> flags = FlagsAPI.getBundle(b);
            if (flags == null || flags.size() == 0) { continue; }

            // Build the help line
            StringBuilder description = new StringBuilder("");
            boolean first = true;

            for (Flag flag : flags) {
                if(!first){
                    description.append(", ");
                } else {
                    first = false;
                }

                description.append(flag.getName());
            }
            sender.sendMessage(Message.HELP_TOPIC.get()
                    .replace("{Topic}", b)
                    .replace("{Description}", description.toString()));
        }
    }
}
