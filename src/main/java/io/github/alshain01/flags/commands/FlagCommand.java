package io.github.alshain01.flags.commands;

import io.github.alshain01.flags.*;
import io.github.alshain01.flags.System;
import io.github.alshain01.flags.area.Area;
import io.github.alshain01.flags.area.Default;
import io.github.alshain01.flags.area.Subdivision;
import io.github.alshain01.flags.economy.EPurchaseType;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;

import org.bukkit.entity.Player;

import java.util.*;

public class FlagCommand extends PluginCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!cmd.toString().equalsIgnoreCase("flag")) { return false; }

        if (args.length < 1) {
            if(sender instanceof Player) {
                sender.sendMessage(getFlagUsage((Player)sender, System.getActive().getAreaAt(((Player)sender).getLocation())));
                return true;
            }
            return false;
        }

        final FlagCommandType command = FlagCommandType.get(args[0]);
        if(command == null) {
            if(sender instanceof Player) {
                sender.sendMessage(getFlagUsage((Player)sender, System.getActive().getAreaAt(((Player)sender).getLocation())));
                return true;
            }
            return false;
        }

        CommandLocation location = null;
        boolean success = false;
        Flag flag = null;
        Set<String> players = new HashSet<String>();

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

            // Make sure we can set flags at that location
            if (System.getActive() == System.WORLD && (location == CommandLocation.AREA || location == CommandLocation.DEFAULT)) {
                sender.sendMessage(Message.NoSystemError.get());
                return true;
            }
        }

        // Location based commands require the player to be in the world
        // Inherit is a special case, doesn't require a location but assumes one exists
        if((location != null || command == FlagCommandType.INHERIT) && !(sender instanceof Player)) {
            sender.sendMessage(Message.NoConsoleError.get());
            return true;
        }

        // Get the flag if required.
        if(command.requiresFlag != null) {
            if(command.requiresFlag || args.length >= 3) {
                flag = Flags.getRegistrar().getFlagIgnoreCase(args[2]);
                if (!Validate.isFlag(sender, flag, args[2])) { return true; }
            }
        }

        // Process the command
        switch(command) {
            case HELP:
                help(sender, getPage(args), getGroup(args));
                success = true;
                break;
            case INHERIT:
                inherit((Player) sender, getValue(args, 1));
                success = true;
                break;
            case GET:
                success = get((Player)sender, location, flag);
                break;
            case SET:
                set((Player) sender, location, flag, getValue(args, 3));
                success = true;
                break;
            case REMOVE:
                remove((Player) sender, location, flag);
                success = true;
                break;
            case VIEWTRUST:
                viewTrust((Player) sender, location, flag);
                success = true;
                break;
            case TRUST:
                players = getPlayers(args, command.requiredArgs - 1);
                success = trust((Player)sender, location, flag, players);
                break;
            case DISTRUST:
                if(args.length > command.requiredArgs) {
                    players = getPlayers(args, command.requiredArgs); } // Players can be omitted to distrust all
                distrust((Player) sender, location, flag, players);
                success = true;
                break;
            case PRESENTMESSAGE:
                success = presentMessage((Player)sender, location, flag);
                break;
            case MESSAGE:
                // Build the message from the remaining arguments
                StringBuilder message = new StringBuilder();
                for (int x = 3; x < args.length; x++) {
                    message.append(args[x]);
                    if (x < args.length - 1) {	message.append(" "); }
                }

                message((Player) sender, location, flag, message.toString());
                success = true;
                break;
            case ERASEMESSAGE:
                erase((Player) sender, location, flag);
                success = true;
                break;
            case CHARGE:
                final EPurchaseType t = EPurchaseType.get(args[1]);
                if (t != null && args.length > 3) {
                    setPrice(sender, t, flag, args[3]);
                } else {
                    getPrice(sender, t, flag);
                }
                success = true;
                break;
        }

        if(!success) {
            sender.sendMessage(command.getHelp());
        }
        return true;
    }

    private static String getFlagUsage(Player player, Area area) {
        //usage: /flag <get|set|remove|trust|distrust|viewtrust|message|presentmessage|erasemessage|charge|inherit|help>
        // We can assume if we get this far, the player has read access to the command.
        StringBuilder usage = new StringBuilder("/flag <get");
        if(player.hasPermission("flags.command.flag.set") && area.hasPermission(player)) {
            usage.append("|set|remove|trust|distrust");
        }
        usage.append("|viewtrust"); //read access

        if(player.hasPermission("flags.command.flag.set") && area.hasPermission(player)) {
            usage.append("|message|erasemessage");
        }
        usage.append("|presentmessage");

        if(Flags.getEconomy() != null && player.hasPermission("flags.command.flag.charge")) {
            usage.append("|charge");
        }

        if(player.hasPermission("flags.command.flag.set") && area.hasPermission(player) && System.getActive().hasSubdivisions()) {
            usage.append("|inherit");
        }

        usage.append("|help>");

        return usage.toString();
    }

    private static String getGroup(String[] args) {
        if(args.length < 2) { return null; }
        if(args.length >= 3) { return args[1]; }

        // Either group or page was omitted, which one?
        try {
            //noinspection ResultOfMethodCallIgnored
            Integer.parseInt(args[1]);
        } catch (Exception e) {
            // It was a string.
            return args[1];
        }
        // It was an integer
        return null;
    }

    /*
 * Value Command Handlers
 */
    protected static boolean get(Player player, CommandLocation location, Flag flag) {
        // Acquire the area
        Area area = getArea(player, location);
        if(!Validate.isArea(player, area)) { return false; }

        // Return the single flag requested
        if (flag != null) {
            player.sendMessage(Message.GetFlag.get()
                    .replaceAll("\\{AreaType\\}", area.getSystemType().getAreaType().toLowerCase())
                    .replaceAll("\\{Flag\\}", flag.getName())
                    .replaceAll("\\{Value\\}", getFormattedValue(area.getValue(flag, false)).toLowerCase()));
            return true;
        }

        // No flag provided, list all set flags for the area
        StringBuilder message = new StringBuilder(Message.GetAllFlags.get()
                .replaceAll("\\{AreaType\\}", area.getSystemType().getAreaType().toLowerCase()));
        boolean first = true; // Governs whether we insert a comma or not (true means no)
        Boolean value;
        Area defaultArea = new Default(player.getWorld());

        for(Flag f : Flags.getRegistrar().getFlags()) {
            value = area.getValue(f, true);

            // Output the flag name
            if (value != null) {
                if ((area instanceof Default && value != f.getDefault())
                        || (!(area instanceof Default) && value != defaultArea.getValue(f, false))){
                    if (!first) { message.append(", ");	}
                    else { first = false; }
                    message.append(f.getName());
                }
            }
        }
        message.append(".");
        player.sendMessage(message.toString());

        return true;
    }

    protected static void set(Player player, CommandLocation location, Flag flag, Boolean value) {
        // Acquire the area
        Area area = getArea(player, location);
        if(!Validate.isArea(player, area)
                || !Validate.isPermitted(player, flag)
                || !Validate.isPermitted(player, area))
        { return; }

        // Acquire the value (maybe)
        if(value == null) {	value = !area.getValue(flag, false); }

        // Set the flag
        if(area.setValue(flag, value, player)) {
            player.sendMessage(Message.SetFlag.get()
                    .replaceAll("\\{AreaType\\}", area.getSystemType().getAreaType().toLowerCase())
                    .replaceAll("\\{Flag\\}", flag.getName())
                    .replaceAll("\\{Value\\}", getFormattedValue(value).toLowerCase()));
        }
    }

    protected static void remove(Player player, CommandLocation location, Flag flag) {
        // Acquire the area
        Area area = getArea(player, location);
        if(!Validate.isArea(player, area) || !Validate.isPermitted(player, area)) { return; }

        // Removing single flag type
        if (flag != null) {
            if (!Validate.isPermitted(player, flag)) { return; }

            if(area.setValue(flag, null, player)) {
                player.sendMessage(Message.RemoveFlag.get()
                        .replaceAll("\\{AreaType\\}", area.getSystemType().getAreaType().toLowerCase())
                        .replaceAll("\\{Flag\\}", flag.getName()));
            }
            return;
        }

        // Removing all flags if the player has permission
        boolean success = true;
        for(Flag f : Flags.getRegistrar().getFlags()) {
            if(area.getValue(f, true) != null) {
                if (!player.hasPermission(f.getPermission()) || !area.setValue(f, null, player)) {
                    success = false;
                }
            }
        }

        player.sendMessage((success ? Message.RemoveAllFlags.get() : Message.RemoveAllFlagsError.get())
                .replaceAll("\\{AreaType\\}", area.getSystemType().getAreaType().toLowerCase()));
    }

    /*
     * Trust Command Handlers
     */
    protected static void viewTrust(Player player, CommandLocation location, Flag flag) {
        boolean first = true;
        StringBuilder message;
        Area area = getArea(player, location);

        Set<String> trustList;
        if(player.hasPermission("flags.view.permtrust")) {
            trustList = area.getTrustList(flag);
        } else {
            trustList = area.getPlayerTrustList(flag);
        }

        if(!Validate.isPlayerFlag(player, flag)
                || !Validate.isArea(player, area)
                || !Validate.isTrustList(player, trustList, area.getSystemType().getAreaType(), flag.getName())) { return; }

        // List all set flags
        message = new StringBuilder(Message.GetTrust.get()
                .replaceAll("\\{AreaType\\}", area.getSystemType().getAreaType().toLowerCase())
                .replaceAll("\\{Flag\\}", flag.getName()));

        for (String p : trustList) {
            if (!first) { message.append(", ");	}
            else { first = false; }
            message.append(p);
        }

        message.append(".");
        player.sendMessage(message.toString());
    }

    protected static boolean trust(Player player, CommandLocation location, Flag flag, Set<String> playerList) {
        if(playerList.size() == 0) { return false; }

        Area area = getArea(player, location);
        if(!Validate.isPlayerFlag(player, flag)
                || !Validate.isArea(player, area)
                || !Validate.isPermitted(player, flag)
                || !Validate.isPermitted(player, area))
        { return true; }

        boolean success = true;
        for(String p : playerList) {
            if(!area.setTrust(flag, p, true, player)) {	success = false; }
        }

        player.sendMessage((success ? Message.SetTrust.get() : Message.SetTrustError.get())
                .replaceAll("\\{AreaType\\}", area.getSystemType().getAreaType().toLowerCase())
                .replaceAll("\\{Flag\\}", flag.getName()));
        return true;
    }

    protected static void distrust(Player player, CommandLocation location, Flag flag, Set<String> playerList) {
        boolean success = true;
        Area area = getArea(player, location);

        if(!Validate.isPlayerFlag(player, flag)
                || !Validate.isArea(player, area)
                || !Validate.isPermitted(player, flag)
                || !Validate.isPermitted(player, area))
        { return; }

        Set<String> trustList = area.getTrustList(flag);
        if(!Validate.isTrustList(player, trustList, area.getSystemType().getAreaType(), flag.getName())) {
            return;
        }

        //If playerList is empty, remove everyone
        for(String p : playerList.isEmpty() ? trustList : playerList) {
            if (!area.setTrust(flag, p, false, player)) { success = false; }
        }

        player.sendMessage((success ? Message.RemoveTrust.get() : Message.RemoveTrustError.get())
                .replaceAll("\\{AreaType\\}", area.getSystemType().getAreaType().toLowerCase())
                .replaceAll("\\{Flag\\}", flag.getName()));
    }

    /*
     * Message Command Handlers
     */
    protected static boolean presentMessage(Player player, CommandLocation location, Flag flag) {
        // Acquire the flag
        if(!flag.isPlayerFlag()) {
            player.sendMessage(Message.PlayerFlagError.get()
                    .replaceAll("\\{Flag\\}", flag.getName()));
            return true;
        }

        // Acquire the area
        Area area = getArea(player, location);
        if(area == null) { return false; }

        // Send the message
        player.sendMessage(area.getMessage(flag, player.getName()));
        return true;
    }

    protected static void message(Player player, CommandLocation location, Flag flag, String message) {
        Area area = getArea(player, location);

        if(!Validate.isPlayerFlag(player, flag)
                || !Validate.isArea(player, area)
                || !Validate.isPermitted(player, area)
                || !Validate.isPermitted(player, flag))
        { return; }

        if(area.setMessage(flag, message, player)) {
            player.sendMessage(area.getMessage(flag, player.getName()));
        }
    }

    protected static void erase(Player player, CommandLocation location, Flag flag) {
        Area area = getArea(player, location);

        if(!Validate.isPlayerFlag(player, flag)
                || !Validate.isArea(player, area)
                || !Validate.isPermitted(player, area)
                || !Validate.isPermitted(player, flag))
        { return; }


        if (area.setMessage(flag, null, player)) {
            player.sendMessage(area.getMessage(flag, player.getName()));
        }
    }

    /*
     * Inheritance Command Handlers
     */
    protected static void inherit(Player player, Boolean value) {
        Area area = getArea(player, CommandLocation.AREA);
        if(!Validate.canSubdivide(player)
                || !Validate.isArea(player, area)
                || !Validate.isSubdivision(player, area)) {
            return;
        }

        if(value == null) {
            value = !((Subdivision)area).isInherited();
        }

        ((Subdivision)area).setInherited(value);
        player.sendMessage(Message.SetInherited.get()
                .replaceAll("\\{Value\\}", getFormattedValue(((Subdivision) area).isInherited()).toLowerCase()));
    }

    /*
     * Price Command Handlers
     */
    protected static void getPrice(CommandSender sender, EPurchaseType type, Flag flag) {
        if(!Validate.hasEconomy(sender)) { return; }

        sender.sendMessage(Message.GetPrice.get()
                .replaceAll("\\{PurchaseType\\}", type.getLocal().toLowerCase())
                .replaceAll("\\{Flag\\}", flag.getName())
                .replaceAll("\\{Price\\}", Flags.getEconomy().format(flag.getPrice(type))));
    }

    protected static boolean setPrice(CommandSender sender, EPurchaseType type, Flag flag, String price) {
        if(!Validate.hasEconomy(sender)) { return true; }
        if((sender instanceof Player) && !Validate.canEditPrice(sender)) { return true; }

        double p;
        try { p = Double.valueOf(price); }
        catch (NumberFormatException ex) { return false; }

        flag.setPrice(type, p);
        sender.sendMessage(Message.SetPrice.get()
                .replaceAll("\\{PurchaseType\\}", type.getLocal().toLowerCase())
                .replaceAll("\\{Flag\\}", flag.getName())
                .replaceAll("\\{Price\\}", price));
        return true;
    }

    /*
     * Help Command Handlers
     */
    protected static void help (CommandSender sender, int page, String group) {
        Registrar registrar = Flags.getRegistrar();
        List<String> groupNames = new ArrayList<String>();
        List<String> allowedFlagNames = new ArrayList<String>();

        // First we need to filter out the flags and groups to show to the particular user.
        for(Flag flag : Flags.getRegistrar().getFlags()) {
            // Add flags for the requested group only
            if(group == null || group.equalsIgnoreCase(flag.getGroup())) {
                // Only show flags that can be used.
                if((sender).hasPermission(flag.getPermission())){
                    allowedFlagNames.add(flag.getName());
                    // Add the group, but only once and only if a group hasn't been requested
                    if(group == null && !groupNames.contains(flag.getGroup())) {
                        groupNames.add(flag.getGroup()); // Add the flags group.
                    }
                }
            }
        }

        // No flags were found, there should always be flags.
        List<String> combinedHelp = new ArrayList<String>();
        if(allowedFlagNames.size() == 0) {
            sender.sendMessage(Message.NoFlagFound.get()
                    .replaceAll("\\{Type\\}", Message.Flag.get().toLowerCase()));
            return;
        }

        // Show them alphabetically and group them together for easier coding
        if(groupNames.size() > 0) {
            Collections.sort(groupNames);
            combinedHelp.addAll(groupNames);
        }

        Collections.sort(allowedFlagNames);
        combinedHelp.addAll(allowedFlagNames);


        //Get total pages
        //1 header per page
        //9 flags per page, except on the first which has a usage line and 8 flags
        int total = ((combinedHelp.size() + 1) / 9);


        // Add the last page, if the last page is not full (less than 9 flags)
        if ((combinedHelp.size() + 1) % 9 != 0) { total++; }

        //Check the page number requested
        if (page < 1 || page > total) { page = 1; }

        String indexType = Message.Index.get();
        if(group != null) {
            indexType = registrar.getFlag(combinedHelp.get(0)).getGroup();
        }

        sender.sendMessage(Message.HelpHeader.get()
                .replaceAll("\\{Group\\}", indexType)
                .replaceAll("\\{Page\\}", String.valueOf(page))
                .replaceAll("\\{TotalPages\\}", String.valueOf(total))
                .replaceAll("\\{Type\\}", Message.Flag.get()));

        // Setup for only displaying 10 lines at a time (including the header)
        int lineCount = 0;

        // Usage line.
        if (page == 1) {
            if(group == null) {
                sender.sendMessage(Message.HelpInfo.get()
                        .replaceAll("\\{Type\\}", Message.Flag.get().toLowerCase()));
                lineCount++;
            } else {
                sender.sendMessage(Message.GroupHelpInfo.get()
                        .replaceAll("\\{Type\\}", registrar.getFlag(combinedHelp.get(0)).getGroup()));
            }
        }

        // Find the start position in the array of names
        int position = ((page - 1) * 9) - 1;
        if(position < 0) { position = 0; }



        // Output the results
        while (position < combinedHelp.size()) {
            if(groupNames.contains(combinedHelp.get(position))) {
                sender.sendMessage(Message.HelpTopic.get()
                        .replaceAll("\\{Topic\\}", combinedHelp.get(position))
                        .replaceAll("\\{Description\\}", Message.GroupHelpDescription.get().replaceAll("\\{Group\\}", combinedHelp.get(position))));
            } else {
                sender.sendMessage(Message.HelpTopic.get()
                        .replaceAll("\\{Topic\\}", combinedHelp.get(position))
                        .replaceAll("\\{Description\\}", registrar.getFlag(combinedHelp.get(position)).getDescription()));
            }

            if(++lineCount == 9) {
                return;
            }
            position++;
        }
    }
}