/* Copyright 2013 Kevin Seiden. All rights reserved.

 This works is licensed under the Creative Commons Attribution-NonCommercial 3.0

 You are Free to:
    to Share: to copy, distribute and transmit the work
    to Remix: to adapt the work

 Under the following conditions:
    Attribution: You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
    Non-commercial: You may not use this work for commercial purposes.

 With the understanding that:
    Waiver: Any of the above conditions can be waived if you get permission from the copyright holder.
    Public Domain: Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
    Other Rights: In no way are any of the following rights affected by the license:
        Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
        The author's moral rights;
        Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.

 Notice: For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
 http://creativecommons.org/licenses/by-nc/3.0/
 */

package io.github.alshain01.flags;

import io.github.alshain01.flags.api.Flag;
import io.github.alshain01.flags.api.FlagsAPI;
import io.github.alshain01.flags.api.area.Area;
import io.github.alshain01.flags.api.area.Subdividable;
import io.github.alshain01.flags.api.economy.EconomyPurchaseType;
import io.github.alshain01.flags.api.sector.Sector;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * Class for handling SQL Database Storage
 */
final class DataStoreMySQL extends DataStore {
    private Connection connection = null;
    private final String url, user, password;

    DataStoreMySQL(Plugin plugin) {
        File sqlConfigFile = new File(plugin.getDataFolder(), "dataConfig.yml");
        YamlConfiguration sqlConfig =YamlConfiguration.loadConfiguration(sqlConfigFile);
        if(!sqlConfig.isString("MYSQL.Url") || !sqlConfig.isString("MYSQL.User") || !sqlConfig.isString("MYSQL.Password")) {
            sqlConfig.set("MYSQL.Url", "jdbc:mysql://localhost/flags");
            sqlConfig.set("MYSQL.User", "MyUserName");
            sqlConfig.set("MYSQL.Password", "MyPassword");
            try {
                sqlConfig.save(sqlConfigFile);
            } catch (IOException ex) {
                Logger.warning("Failed to write default MySQL Configuration file.");
            }
        }

        this.url = sqlConfig.getString("MYSQL.Url");
        this.user = sqlConfig.getString("MYSQL.User");
        this.password = sqlConfig.getString("MYSQL.Password");

        connect(url, user, password);
    }

    void connect(String url, String user, String password) {
        // Connect to the database.
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            SqlError(e.getMessage());
        }
    }

    void SqlError(String error) {
        Logger.error("[SQL DataStore Error] " + error);
    }

    boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            SqlError(e.getMessage());
            return false;
        }
    }

    void executeStatement(String statement) {
        try {
            Statement SQL = connection.createStatement();
            Logger.debug(statement);
            SQL.execute(statement);
        } catch (SQLException e) {
            SqlError(e.getMessage());
        }
    }

    ResultSet executeQuery(String query) {
        try {
            Statement SQL = connection.createStatement();
            Logger.debug(query);
            return SQL.executeQuery(query);
        } catch (SQLException e) {
            SqlError(e.getMessage());
            return null;
        }
    }

    String areaBuilder(String query, Area area) {
        return query
                .replace("%cuboid%", area.getCuboidPlugin().getCuboidName())
                .replace("%world%", area.getWorld().getName())
                .replace("%area%", area.getId());
    }

    private boolean notExists() {
        String[] connection = url.split("/");

        // Result Limiting, requires MYSQL exclusively
        ResultSet results =
                executeQuery("SELECT * FROM information_schema.tables "
                        + "WHERE table_schema = '%database%' AND table_name = 'Version' LIMIT 1;"
                        .replace("%database%", connection[connection.length - 1]));

        try {
            return !results.next();
        } catch (SQLException e) {
            SqlError(e.getMessage());
        }
        return true;
    }

    private void writeVersion(DataStoreVersion version) {
        executeQuery("UPDATE Version SET Major=" + version.getMajor() + ", Minor=" + version.getMinor() + ", Build=" + version.getBuild() + ";");
    }

    @Override
    public void close() {
        try {
            if (isConnected()) {
                connection.close();
            }
        } catch (SQLException e) {
            SqlError(e.getMessage());
        }
    }

    @Override
    public void reload() {
        // Close the connection and reconnect.
        try {
            if (isConnected()) {
                connection.close();
            }
        } catch (SQLException e) {
            SqlError(e.getMessage());
            return;
        }

        connect(url, user, password);
    }

    @Override
    public void create(JavaPlugin plugin) {
        if (notExists()) {
            executeStatement("CREATE TABLE IF NOT EXISTS Version (Major INT, Minor INT, Build INT);");
            executeStatement("INSERT INTO Version (Major, Minor, Build) VALUES (2,0,0);");
            executeStatement("CREATE TABLE IF NOT EXISTS Sectors (Id CHAR(36), Name VARCHAR(255), World CHAR(36), GX INT, GY INT, GZ INT, LX INT, LY INT, LZ INT, Depth INT, PRIMARY KEY (Id));");
            executeStatement("CREATE TABLE IF NOT EXISTS Bundle (BundleName VARCHAR(36), FlagName VARCHAR(36), CONSTRAINT pk_BundleEntry PRIMARY KEY (BundleName, FlagName));");
            executeStatement("CREATE TABLE IF NOT EXISTS Price (FlagName VARCHAR(36), ProductType VARCHAR(36), Cost DOUBLE, CONSTRAINT pk_FlagType PRIMARY KEY (FlagName, ProductType));");
            executeStatement("CREATE TABLE IF NOT EXISTS Flags (AreaPlugin VARCHAR(255), WorldId CHAR(36), AreaId CHAR(36), FlagName VARCHAR(36), Setting BOOLEAN, Message VARCHAR(255), CONSTRAINT pk_WorldFlag PRIMARY KEY (AreaPlugin, WorldId, AreaId, FlagName));");
            executeStatement("CREATE TABLE IF NOT EXISTS Trust (AreaPlugin VARCHAR(255), WorldId CHAR(36), AreaId CHAR(36), FlagName VARCHAR(36), TrusteeId VARCHAR(36), TrusteeName VARCHAR(255) CONSTRAINT pk_WorldFlag PRIMARY KEY (AreaPlugin, WorldId, AreaId, FlagName, TrusteeId));");
            executeStatement("CREATE TABLE IF NOT EXISTS PermissionTrust (AreaPlugin VARCHAR(255), WorldId CHAR(36), AreaId CHAR(36), FlagName VARCHAR(36), Permission VARCHAR(36) CONSTRAINT pk_WorldFlag PRIMARY KEY (AreaPlugin, WorldId, AreaId, FlagName, Permission));");
        }
    }

    @Override
    public DataStoreVersion readVersion() {
        ResultSet results = executeQuery("SELECT * FROM Version;");
        try {
            results.next();
            return new DataStoreVersion(results.getInt("Major"), results.getInt("Minor"), results.getInt("Build"));
        } catch (SQLException ex) {
            SqlError(ex.getMessage());
        }
        return new DataStoreVersion(0, 0, 0);
    }

    @Override
    public DataStoreType getType() {
        return DataStoreType.MYSQL;
    }

    @Override
    public boolean update(JavaPlugin plugin) {
        Logger.info("No Database Updates Necessary.");
        return true;
    }

    @Override
    public Collection<String> readBundles() {
        final ResultSet results = executeQuery("SELECT DISTINCT BundleName FROM Bundle;");
        Set<String> bundles = new HashSet<String>();

        try {
            while (results.next()) {
                bundles.add(results.getString("BundleName"));
            }
        } catch (SQLException ex) {
            SqlError(ex.getMessage());
            return new HashSet<String>();
        }
        return bundles;
    }

    @Override
    public Collection<Flag> readBundle(String name) {
        final ResultSet results = executeQuery("SELECT * FROM Bundle WHERE BundleName='" + name + "';");
        HashSet<Flag> flags = new HashSet<Flag>();

        try {
            while (results.next()) {
                String flagName = results.getString("FlagName");
                if (FlagsAPI.getRegistrar().getFlag(flagName) != null) {
                    flags.add(FlagsAPI.getRegistrar().getFlag(flagName));
                }
            }
        } catch (SQLException ex) {
            SqlError(ex.getMessage());
            return new HashSet<Flag>();
        }
        return flags;
    }

    private void deleteBundle(String name) {
        executeStatement("DELETE FROM Bundle WHERE BundleName='" + name + "';");
    }

    @Override
    public void writeBundle(String bundleName, Collection<Flag> flags) {
        StringBuilder values = new StringBuilder();

        // Clear out any existing version of this bundle.
        // If no flags are provided, assume we are deleting it.
        deleteBundle(bundleName);
        if (flags == null || flags.size() == 0) {
            return;
        }

        Iterator<Flag> iterator = flags.iterator();
        while (iterator.hasNext()) {
            Flag flag = iterator.next();
            values.append("('").append(bundleName).append("','").append(flag.getName()).append("')");
            if (iterator.hasNext()) {
                values.append(",");
            }
        }

        executeStatement("INSERT INTO Bundle (BundleName, FlagName) VALUES " + values + ";");
    }

    @Override
    public Boolean readFlag(Area area, Flag flag) {
        String selectString = "SELECT * FROM Flags"
                + " WHERE AreaPlugin='%cuboid%' AND WorldId='%world%' AND AreaId='%area%' AND FlagName='%flag%';";

        ResultSet results = executeQuery(areaBuilder(selectString, area)
                .replace("%flag%", flag.getName()));

        try {
            if (results.next()) {
                boolean value = results.getBoolean("Setting");
                if (results.wasNull()) {
                    return null;
                }
                return value;
            }
            return null;
        } catch (SQLException ex) {
            SqlError(ex.getMessage());
        }
        return null;
    }

    @Override
    public void writeFlag(Area area, Flag flag, Boolean setting) {
        String insertString = "INSERT INTO Flags (AreaPlugin, WorldId, AreaId, FlagName, Setting)"
                + " VALUES ('%cuboid%', '%world%', '%area%', '%flag%', %setting%)"
                + " ON DUPLICATE KEY UPDATE Setting=%setting%;";

        executeStatement(areaBuilder(insertString, area)
                .replace("%flag%", flag.getName())
                .replace("%setting%", String.valueOf(setting)));
    }

    @Override
    public String readMessage(Area area, Flag flag) {
        String selectString = "SELECT * FROM Flags"
                + "WHERE AreaPlugin='%cuboid%' AND WorldId='%world%' AND AreaId='%area%' AND FlagName='%flag%'";

        ResultSet results = executeQuery(areaBuilder(selectString, area)
                .replace("%flag%", flag.getName()));

        try {
            if (results.next()) {
                String message = results.getString("Message");
                if (message == null) {
                    return null;
                }
                return message.replace("''", "'");
            }
            return null;
        } catch (SQLException ex) {
            SqlError(ex.getMessage());
        }
        return null;
    }

    @Override
    public void writeMessage(Area area, Flag flag, String message) {
        String insertString;
        if (message == null) {
            message = "null";
        } else {
            // We add the single quote here so null will be the actual null value
            // Instead of a string containing the word "null".
            message = "'" + message.replace("'", "''") + "'";
        }

        insertString = "INSERT INTO Flags (AreaPlugin, WorldId, AreaId, FlagName, Message)" +
                " VALUES ('%cuboid%', '%world%', '%area%', '%flag%', %message%) ON DUPLICATE KEY UPDATE FlagMessage=%message%;";

        executeStatement(areaBuilder(insertString, area)
                .replace("%flag%", flag.getName())
                .replace("%message%", message));
    }

    @Override
    public double readPrice(Flag flag, EconomyPurchaseType type) {
        String selectString = "SELECT * FROM Price WHERE FlagName='%flag%' AND ProductType='%type%';";
        ResultSet results = executeQuery(selectString
                .replace("%flag%", flag.getName())
                .replace("%type%", type.toString()));

        try {
            if (results.next()) {
                return results.getDouble("Cost");
            }
            return 0;
        } catch (SQLException ex) {
            SqlError(ex.getMessage());
        }
        return 0;
    }

    @Override
    public void writePrice(Flag flag, EconomyPurchaseType type, double price) {
        String insertString = "INSERT INTO Price (FlagName, ProductType, Cost) VALUES ('%flag%', '%product%', %price%) ON DUPLICATE KEY UPDATE Cost=%price%;";
        executeStatement(insertString
                .replace("%flag%", flag.getName())
                .replace("%product%", type.toString())
                .replace("%price%", String.valueOf(price)));
    }

    @Override
    public Map<UUID, String> readPlayerTrust(Area area, Flag flag) {
        String selectString = "SELECT * FROM Trust WHERE AreaPlugin='%cuboid%' AND WorldId='%world%' AND AreaId='%area%' AND FlagName='%flag%';";

        ResultSet results = executeQuery(areaBuilder(selectString, area)
                .replace("%flag%", flag.getName()));

        Map<UUID, String> trustList = new HashMap<UUID, String>();
        try {
            while (results.next()) {
                trustList.put(UUID.fromString(results.getString("TrusteeId")), results.getString("TrusteeName"));
            }
        } catch (SQLException ex) {
            SqlError(ex.getMessage());
        }
        return trustList;
    }

    @Override
    public Collection<Permission> readPermissionTrust(Area area, Flag flag) {
        String selectString = "SELECT * FROM PermissionTrust WHERE AreaPlugin='%cuboid%' AND WorldId='%world%' AND AreaId='%area%' AND FlagName='%flag%';";

        ResultSet results = executeQuery(areaBuilder(selectString, area)
                .replace("%flag%", flag.getName()));

        Set<Permission> trustList = new HashSet<Permission>();
        try {
            while (results.next()) {
                trustList.add(new Permission(results.getString("Permission")));
            }
        } catch (SQLException ex) {
            SqlError(ex.getMessage());
        }
        return trustList;
    }

    @Override
    public void writePlayerTrust(Area area, Flag flag, Map<UUID, String> players) {
        // Delete the old list to be replaced
        String deleteString = "DELETE FROM Trust WHERE AreaPlugin='%cuboid%' AND WorldId='%world%' AND AreaId='%area%' AND FlagName='%flag%';";

        executeStatement(areaBuilder(deleteString, area)
                .replace("%flag%", flag.getName()));

        String insertString = "INSERT INTO Trust (AreaPlugin, WorldId, AreaId, FlagName, TrusteeId, TrusteeName)"
                + "VALUES('%cuboid%', '%world%', '%area%', '%flag%', '%player%', '%playername%');";

        for (UUID u : players.keySet()) {
            executeStatement(areaBuilder(insertString, area)
                    .replace("%flag%", flag.getName())
                    .replace("%player%", u.toString())
                    .replace("%playername%", players.get(u)));
        }
    }

    @Override
    public void writePermissionTrust(Area area, Flag flag, Collection<Permission> permissions) {
        // Delete the old list to be replaced
        String deleteString = "DELETE FROM PermissionTrust WHERE AreaPlugin='%cuboid%' AND WorldId='%world%' AND AreaId='%area%' AND FlagName='%flag%';";

        executeStatement(areaBuilder(deleteString, area)
                .replace("%flag%", flag.getName()));

        String insertString = "INSERT INTO Trust (AreaPlugin, WorldId, AreaId, FlagName, TrustId, TrustName)"
                + "VALUES('%cuboid%', '%world%', '%area%', '%flag%', '%player%', '%playername%');";

        for (Permission p : permissions) {
            executeStatement(areaBuilder(insertString, area)
                    .replace("%flag%", flag.getName())
                    .replace("%permission%", p.getName()));
        }
    }

    @Override
    public boolean readInheritance(Subdividable area) {
        if (!area.isSubdivision()) {
            return false;
        }

        String selectString = "SELECT * FROM Flags WHERE AreaPlugin='%cuboid%' AND WorldId='%world%' AND AreaId='%area%' AND FlagName='InheritParent';";

        ResultSet results = executeQuery(areaBuilder(selectString, area));

        try {
            return !results.next() || results.getBoolean("Setting");
        } catch (SQLException ex) {
            SqlError(ex.getMessage());
        }
        return true;
    }

    @Override
    public void writeInheritance(Subdividable area, boolean setting) {
        if (!area.isSubdivision()) {
            return;
        }

        String insertString = "INSERT INTO Flags (AreaPlugin, WorldId, AreaId, FlagName, Setting) "
                + "VALUES ('%cuboid%', '%world%', '%area%', '%sub%', 'InheritParent', %setting%) ON DUPLICATE KEY UPDATE FlagValue=%setting%;";

        executeStatement(areaBuilder(insertString, area)
                .replace("%setting%", String.valueOf(setting)));
    }

    public Map<UUID, Sector> readSectors() {
        ResultSet results = executeQuery("SELECT * FROM Sectors;");
        Map<UUID, Sector> sectors = new HashMap<UUID, Sector>();
        try {
            while (results.next()) {
                UUID id = UUID.fromString(results.getString("Id"));
                HashMap<String, Object> sector = new HashMap<String, Object>();
                sector.put("Parent", results.getString("ParentId"));
                sector.put("Name", results.getString("Name"));
                sector.put("Depth", results.getInt("Depth"));

                HashMap<String, Object> greater = new HashMap<String, Object>();
                greater.put("World", results.getString("World"));
                greater.put("X", results.getString("GX"));
                greater.put("Y", results.getString("GY"));
                greater.put("Z", results.getString("GZ"));
                sector.put("GreaterCorner", greater);

                HashMap<String, Object> lesser = new HashMap<String, Object>();
                greater.put("World", results.getString("World"));
                greater.put("X", results.getString("LX"));
                greater.put("Y", results.getString("LY"));
                greater.put("Z", results.getString("LZ"));
                sector.put("LesserCorner", lesser);

                sectors.put(id, new SectorBase(id, sector));
            }
        } catch (SQLException ex) {
            Logger.error("Failed to read sectors from MySQL.");
        }
        return sectors;
    }

    @Override
    public void writeSector(Sector sector) {
        String insertString = "INSERT INTO Sectors (Id, Name, ParentId, World, GX, GY, GZ, LX, LY, LZ, Depth) "
                + "VALUES ('%id%', %name%, '%parent%', '%world%', '%gx%', '%gy%', '%gz%', '%lx%', '%ly%', '%lz%', %depth%) " +
                "ON DUPLICATE KEY UPDATE GreaterCorner=%greater%, LesserCorner=%lesser%, Depth=%depth%;";

        insertString = insertString
                .replace("%id%", sector.getID().toString())
                .replace("%name%", sector.getName())
                .replace("%depth%", String.valueOf(sector.getDepth()))
                .replace("%world%", sector.getWorld().getUID().toString())
                .replace("%gx%", String.valueOf(sector.getGreaterCorner().getX()))
                .replace("%gy%", String.valueOf(sector.getGreaterCorner().getY()))
                .replace("%gz%", String.valueOf(sector.getGreaterCorner().getZ()))
                .replace("%lx%", String.valueOf(sector.getLesserCorner().getX()))
                .replace("%ly%", String.valueOf(sector.getLesserCorner().getY()))
                .replace("%lz%", String.valueOf(sector.getLesserCorner().getZ()))
                .replace("%parent%", sector.getParentID() != null ? sector.getParentID().toString() : "null");
        executeStatement(insertString);
    }

    @Override
    public void deleteSector(UUID sID) {
        String deleteString = "DELETE FROM Sectors WHERE Id='%id%';";
        executeStatement(deleteString.replace("%id%", sID.toString()));
    }

    @Override
    public void remove(AreaRemovable area) {
        String deleteString = "DELETE FROM %table% WHERE AreaPlugin='%cuboid%' AND WorldId='%world%' AND AreaId='%area%';";
        executeStatement(areaBuilder(deleteString, area)
                .replace("%table%", "Flags"));

        executeStatement(areaBuilder(deleteString, area)
                .replace("%table%", "Trust"));

        executeStatement(areaBuilder(deleteString, area)
                .replace("%table%", "PermissionTrust"));
    }

    @Override
    Collection<String> getAllAreaIds(World world) {
        Set<String> areas = new HashSet<String>();
        ResultSet results = executeQuery("SELECT DISTINCT AreaId FROM Flags;");
        try {
            while (results.next()) {
                areas.add(results.getString("AreaId"));
            }
        } catch (SQLException ex) {
            Logger.warning("Failed to read Area IDs from MySQL.");
        }

        results = executeQuery("SELECT DISTINCT AreaId FROM Trust;");
        try {
            while (results.next()) {
                areas.add(results.getString("AreaId"));
            }
        } catch (SQLException ex) {
            Logger.warning("Failed to read Area IDs from MySQL.");
        }
        return areas;
    }
}
