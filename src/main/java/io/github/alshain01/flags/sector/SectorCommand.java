package io.github.alshain01.flags.sector;

import io.github.alshain01.flags.Flags;
import io.github.alshain01.flags.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

public class SectorCommand implements CommandExecutor {
    private enum CommandType {
        INFO, DELETE, DELETEALL
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!command.toString().equals("sector")) { return false; }

        if(!(sender instanceof Player)) {
            sender.sendMessage(Message.NoConsoleError.get());
            return true;
        }

        if(args.length < 1) {
            sender.sendMessage(getUsage(sender));
            return true;
        }

        final CommandType cType = CommandType.valueOf(args[0].toUpperCase());
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
