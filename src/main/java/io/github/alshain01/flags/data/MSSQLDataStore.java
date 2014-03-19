package io.github.alshain01.flags.data;

import io.github.alshain01.flags.*;
import io.github.alshain01.flags.area.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;

class MSSQLDataStore extends SQLDataStore {
    public MSSQLDataStore(String url, String user, String password) {
        this.url = url;
        this.password = password;
        this.user = user;

        connect(url, user, password);
    }

    @Override
    void createSystemDB() {
        // BIT BASED BOOLEAN
        executeStatement("CREATE TABLE IF NOT EXISTS " + CuboidType.getActive().toString()
                + "Flags (WorldName VARCHAR(50), AreaID VARCHAR(50), AreaSubID VARCHAR(50), "
                + "FlagName VARCHAR(25), FlagValue BIT, FlagMessage VARCHAR(255), "
                + "CONSTRAINT pk_AreaFlag PRIMARY KEY (WorldName, AreaID, AreaSubID, FlagName));");

        executeStatement("CREATE TABLE IF NOT EXISTS " + CuboidType.getActive().toString()
                + "Trust (WorldName VARCHAR(50), AreaID VARCHAR(50), "
                + "AreaSubID VARCHAR(50), FlagName VARCHAR(25), Trustee VARCHAR(50), "
                + "CONSTRAINT pk_WorldFlag PRIMARY KEY (WorldName, AreaID, AreaSubID, FlagName, Trustee));");
    }

    @Override
    public void create(JavaPlugin plugin) {
        // BIT BASED BOOLEAN
        if(notExists()) {
            executeStatement("CREATE TABLE IF NOT EXISTS Version (Major INT, Minor INT, Build INT);");
            executeStatement("INSERT INTO Version (Major, Minor, Build) VALUES (1,3,0);");
            executeStatement("CREATE TABLE IF NOT EXISTS Bundle (BundleName VARCHAR(25), FlagName VARCHAR(25), CONSTRAINT pk_BundleEntry PRIMARY KEY (BundleName, FlagName));");
            executeStatement("CREATE TABLE IF NOT EXISTS Price (FlagName VARCHAR(25), ProductType VARCHAR(25), Cost DOUBLE, CONSTRAINT pk_FlagType PRIMARY KEY (FlagName, ProductType));");
            executeStatement("CREATE TABLE IF NOT EXISTS WorldFlags (WorldName VARCHAR(50), FlagName VARCHAR(25), FlagValue BIT, FlagMessage VARCHAR(255), CONSTRAINT pk_WorldFlag PRIMARY KEY (WorldName, FlagName));");
            executeStatement("CREATE TABLE IF NOT EXISTS WorldTrust (WorldName VARCHAR(50), FlagName VARCHAR(25), Trustee VARCHAR(50), CONSTRAINT pk_WorldFlag PRIMARY KEY (WorldName, FlagName, Trustee));");
            executeStatement("CREATE TABLE IF NOT EXISTS DefaultFlags (WorldName VARCHAR(50), FlagName VARCHAR(25), FlagValue BIT, FlagMessage VARCHAR(255), CONSTRAINT pk_DefaultFlag PRIMARY KEY (WorldName, FlagName));");
            executeStatement("CREATE TABLE IF NOT EXISTS DefaultTrust (WorldName VARCHAR(50), FlagName VARCHAR(25), Trustee VARCHAR(50), CONSTRAINT pk_DefaultTrust PRIMARY KEY (WorldName, FlagName, Trustee));");
        }
    }

    @Override
    boolean notExists() {
        // We always need to create the system specific table
        // in case it changed since the database was created.
        // i.e. Grief Prevention was removed and WorldGuard was installed.
        if(CuboidType.getActive() != CuboidType.WILDERNESS) {
            createSystemDB();
        }

        String[] connection = url.split("/");

        // TOP n ROW LIMITING
        ResultSet results =
                executeQuery("SELECT TOP 1 * FROM information_schema.tables "
                        + "WHERE table_schema = '%database%' AND table_name = 'Version';"
                        .replaceAll("%database%", connection[connection.length-1]));

        try {
            return !results.next();
        } catch (SQLException e) {
            SqlError(e.getMessage());
        }
        return true;
    }

    @Override
    public void writeFlag(Area area, Flag flag, Boolean value) {
        // BIT BASED BOOLEAN
        String insertString;

        if((area instanceof Wilderness) || (area instanceof Default)) {
            insertString = "INSERT INTO %table%Flags (WorldName, FlagName, FlagValue)"
                    + " VALUES ('%world%', '%flag%', %value%) ON DUPLICATE KEY UPDATE FlagValue=%value%;";
        } else {
            insertString = "INSERT INTO %table%Flags (WorldName, AreaID, AreaSubID, FlagName, FlagValue)"
                    + " VALUES ('%world%', '%area%', %sub%, '%flag%', %value%) ON DUPLICATE KEY UPDATE FlagValue=%value%;";
        }

        String bitValue = "null";
        if(value != null) {
            bitValue = String.valueOf(value ? 1 : 0);
        }

        executeStatement(areaBuilder(insertString, area)
                .replaceAll("%flag%", flag.getName())
                .replaceAll("%value%", bitValue));
    }

    //For SQL Importer
    @Override
    protected void writeAreaFlag(Boolean value, String flagName, String areaType, String worldName, String systemID, String systemSubID) {
        String insertString = "INSERT INTO %table%Flags (WorldName, AreaID, AreaSubID, FlagName, FlagValue)"
                + " VALUES ('%world%', '%area%', %sub%, '%flag%', %value%) ON DUPLICATE KEY UPDATE FlagValue=%value%;";

        String bitValue = "null";
        if(value != null) {
            bitValue = String.valueOf(value ? 1 : 0);
        }

        executeStatement(areaBuilder(insertString, areaType, worldName, systemID, systemSubID)
                .replaceAll("%flag%", flagName)
                .replaceAll("%value%", bitValue));
    }

    @Override
    public void writeInheritance(Area area, boolean value) {
        // BIT BASED BOOLEAN
        if(!(area instanceof Subdivision) || !((Subdivision)area).isSubdivision()) {
            return;
        }

        String bitValue = String.valueOf(value ? 1 : 0);

        String insertString = "INSERT INTO %table%Flags (WorldName, AreaID, AreaSubID, FlagName, FlagValue) "
                + "VALUES ('%world%', '%area%', %sub%, 'InheritParent', %value%) ON DUPLICATE KEY UPDATE FlagValue=%value%;";

        executeStatement(areaBuilder(insertString, area)
                .replaceAll("%value%", bitValue));
    }

    @Override
    protected void writeInheritance(boolean value, String areaType, String worldName, String systemID, String systemSubID) {
        String insertString = "INSERT INTO %table%Flags (WorldName, AreaID, AreaSubID, FlagName, FlagValue) "
                + "VALUES ('%world%', '%area%', %sub%, 'InheritParent', %value%) ON DUPLICATE KEY UPDATE FlagValue=%value%;";

        String bitValue = String.valueOf(value ? 1 : 0);

        executeStatement(areaBuilder(insertString, areaType, worldName, systemID, systemSubID)
                .replaceAll("%value%", bitValue));
    }

    @Override
    public DataStoreType getType() {
        return DataStoreType.MSSQL;
    }
}
