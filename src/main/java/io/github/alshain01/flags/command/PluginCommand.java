package io.github.alshain01.flags.command;

import io.github.alshain01.flags.CuboidType;
import io.github.alshain01.flags.Message;
import io.github.alshain01.flags.area.Area;
import io.github.alshain01.flags.area.Default;
import io.github.alshain01.flags.area.Wilderness;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

abstract class PluginCommand {
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

    static Area getArea(CommandSender sender, CommandLocation location) {
        if (location == CommandLocation.DEFAULT) {
            return new Default((((Player)sender).getWorld()));
        } else if (location == CommandLocation.WORLD) {
            return new Wilderness((((Player)sender).getWorld()));
        } else if (location == CommandLocation.AREA) {
            Area area = CuboidType.getActive().getAreaAt(((Player) sender).getLocation());
            return (area instanceof Wilderness) ? null : area;
        }
        // Invalid location selection
        return null;
    }
}
