package io.github.alshain01.flags.commands;

import org.bukkit.permissions.Permissible;

enum SectorCommandType {
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
