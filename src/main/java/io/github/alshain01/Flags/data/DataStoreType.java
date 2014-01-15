package io.github.alshain01.Flags.data;

import org.bukkit.plugin.java.JavaPlugin;

public enum DataStoreType {
    YAML("yaml", "YAML") {
        public DataStore getDataStore(JavaPlugin plugin) {
            return new YamlDataStore(plugin);
        }
    },

    MSSQL("jtdc", "Microsoft SQL") {
        public DataStore getDataStore(JavaPlugin plugin) {
            final String url = plugin.getConfig().getString("Flags.Database.Url");
            final String user = plugin.getConfig().getString("Flags.Database.User");
            final String pw = plugin.getConfig().getString("Flags.Database.Password");

            return new MSSQLDataStore(url, user, pw);
        }
    },
    MYSQL("mysql", "MYSQL") {
        public DataStore getDataStore(JavaPlugin plugin) {
            final String url = plugin.getConfig().getString("Flags.Database.Url");
            final String user = plugin.getConfig().getString("Flags.Database.User");
            final String pw = plugin.getConfig().getString("Flags.Database.Password");

            return new MySQLDataStore(url, user, pw);
        }
    },
    POSTGRESQL("postgresql", "PostgreSQL") {
        public DataStore getDataStore(JavaPlugin plugin) {
            final String url = plugin.getConfig().getString("Flags.Database.Url");
            final String user = plugin.getConfig().getString("Flags.Database.User");
            final String pw = plugin.getConfig().getString("Flags.Database.Password");

            return new SQLDataStore(url, user, pw);
        }
    };

    final String identifier;
    final String niceName;

    public abstract DataStore getDataStore(JavaPlugin plugin);

    private DataStoreType(String identifier, String niceName) {
        this.identifier = identifier;
        this.niceName = niceName;
    }

    public String getName() {
        return niceName;
    }

    public static DataStoreType getType(String url) {
       for(DataStoreType d : DataStoreType.values()) {
           if(url.contains(d.identifier)) {
               return d;
           }
       }
       return DataStoreType.YAML;
    }
/*
    public DataStore getDataStore(JavaPlugin plugin) {
        String url = plugin.getConfig().getString("Flags.Database.Url");
        String user = plugin.getConfig().getString("Flags.Database.User");
        String pw = plugin.getConfig().getString("Flags.Database.Password");

        switch (this) {
            case MYSQL:
                return new MySQLDataStore(url, user, pw);
            case MSSQL:
                return new MSSQLDataStore(url, user, pw);
            case POSTGRESQL:
                return new SQLDataStore(url, user, pw);
            default:
                return new YamlDataStore(plugin);
        }
    }*/
}
