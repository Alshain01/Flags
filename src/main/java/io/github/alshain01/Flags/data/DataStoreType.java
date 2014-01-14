package io.github.alshain01.Flags.data;

import org.bukkit.plugin.java.JavaPlugin;

public enum DataStoreType {
    YAML("yaml", "YAML"), MSSQL("jtdc", "Microsoft SQL"), MySQL("mysql", "MySQL"), POSTGRESQL("postgresql", "PostgreSQL");

    String identifier;
    String niceName;

    private DataStoreType(String identifier, String niceName) {
        this.identifier = identifier;
        this.niceName = niceName;
    }

    public String getName() {
        return niceName;
    }

    public static DataStoreType get(String url) {
       for(DataStoreType d : DataStoreType.values()) {
           if(url.contains(d.identifier)) {
               return d;
           }
       }
       return DataStoreType.YAML;
    }

    public DataStore getDataStore(JavaPlugin plugin) {
        String url = plugin.getConfig().getString("Flags.Database.Url");
        String user = plugin.getConfig().getString("Flags.Database.User");
        String pw = plugin.getConfig().getString("Flags.Database.Password");

        switch (this) {
            case MySQL:
                return new MySQLDataStore(url, user, pw);
            case MSSQL:
                return new MSSQLDataStore(url, user, pw);
            case POSTGRESQL:
                return new SQLDataStore(url, user, pw);
            default:
                return new YamlDataStore(plugin);
        }
    }
}
