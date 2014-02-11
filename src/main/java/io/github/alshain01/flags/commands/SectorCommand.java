package io.github.alshain01.flags.commands;

import io.github.alshain01.flags.Flags;
import io.github.alshain01.flags.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

public class SectorCommand implements CommandExecutor {
    private enum SectorCommandType {
        DELETE('d'), DELETEALL('a');

        final char alias;

        SectorCommandType(char alias) {
            this.alias = alias;
        }

        public static SectorCommandType get(String name) {
            for(SectorCommandType t : SectorCommandType.values()) {
                if(name.toLowerCase().equals(t.toString().toLowerCase()) || name.toLowerCase().equals(String.valueOf(t.alias))) {
                    return t;
                }
            }
            return null;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!command.toString().equalsIgnoreCase("sector")) { return false; }

        if(!(sender instanceof Player)) {
            sender.sendMessage(Message.NoConsoleError.get());
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

        switch(cType) {
            case DELETE:
                if(!sender.hasPermission("flags.sector.delete")) {
                    sender.sendMessage(Message.FlagPermError.get().replaceAll("\\{Type\\}", "command"));
                    return true;
                }

                sender.sendMessage(Flags.getSectorManager().delete(((Player)sender).getLocation())
                    ? Message.DeleteSector.get()
                    : Message.NoSectorError.get());

                return true;
            case DELETEALL:
                if(!sender.hasPermission("flags.sector.deleteall")) {
                    sender.sendMessage(Message.FlagPermError.get().replaceAll("\\{Type\\}", "command"));
                    return true;
                }

                Flags.getSectorManager().clear();
                sender.sendMessage(Message.DeleteAllSectors.get());
                return true;
        }
        sender.sendMessage(getUsage(sender));
        return true;
    }

    private String getUsage(Permissible player) {
        StringBuilder permCommands = new StringBuilder();
        boolean first = true;

        if(player.hasPermission("flags.sector.delete")) {
            permCommands.append("delete");
            first = false;
        }

        if (!first) {
            permCommands.append(" | ");
        }

        if (player.hasPermission("flags.sector.deleteall")) {
            permCommands.append("deleteall");
        }
        return "/sector <" + permCommands.toString() + ">";
    }
}
