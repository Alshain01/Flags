package io.github.alshain01.flags;

import io.github.alshain01.flags.api.FlagsAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

final class CommandSector implements CommandExecutor {
    private enum SectorCommandType {
        DELETE('d'), DELETEALL('a'), DELETETOPLEVEL('t');

        final char alias;

        SectorCommandType(char alias) {
            this.alias = alias;
        }

        static SectorCommandType get(String name) {
            for(SectorCommandType t : SectorCommandType.values()) {
                if(name.toLowerCase().equals(t.toString().toLowerCase()) || name.toLowerCase().equals(String.valueOf(t.alias))) {
                    return t;
                }
            }
            return null;
        }

        boolean hasPermission(Permissible permissible) {
            return permissible.hasPermission("flags.sector." + this.toString().toLowerCase());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(Message.NO_CONSOLE_ERROR.get());
            return true;
        }

         if(args.length < 1) {
            sender.sendMessage(getUsage(sender));
            return true;
        }

        final SectorCommandType cType = SectorCommandType.get(args[0]);
        if(cType == null) {
            sender.sendMessage(getUsage(sender));
            return true;
        }

        if(!cType.hasPermission(sender)) {
            sender.sendMessage(Message.FLAG_PERM_ERROR.get().replace("{Type}", Message.COMMAND.get()));
            return true;
        }

        switch(cType) {
            case DELETE:
                sender.sendMessage(FlagsAPI.getSectorManager().delete(((Player)sender).getLocation())
                    ? Message.DELETE_SECTOR.get()
                    : Message.NO_SECTOR_ERROR.get());
                return true;
            case DELETETOPLEVEL:
                sender.sendMessage(FlagsAPI.getSectorManager().deleteTopLevel(((Player)sender).getLocation())
                        ? Message.DELETE_SECTOR.get()
                        : Message.NO_SECTOR_ERROR.get());
            case DELETEALL:
                FlagsAPI.getSectorManager().clear();
                sender.sendMessage(Message.DELETE_ALL_SECTORS.get());
                return true;
        }
        sender.sendMessage(getUsage(sender));
        return true;
    }

    private String getUsage(Permissible player) {
        StringBuilder helpText = new StringBuilder("/sector <");
        boolean first = true;

        for(SectorCommandType c : SectorCommandType.values()) {
            if(c.hasPermission(player)) {
                if(!first) { helpText.append(" | "); }
                helpText.append(c.toString().toLowerCase());
                first = false;
            }
        }
        return helpText.append(">").toString();
    }
}
