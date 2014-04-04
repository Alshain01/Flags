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

import io.github.alshain01.flags.area.*;
import io.github.alshain01.flags.economy.EconomyPurchaseType;
import io.github.alshain01.flags.sector.Sector;
import io.github.alshain01.flags.sector.SectorLocation;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.*;

/**
 * Class for handling SQL Database Storage
 */
final class DataStoreMySQL extends DataStore {
    private Connection connection = null;
    private final String url, user, password;

    DataStoreMySQL(String url, String user, String pw) {
        this.url = url;
        this.user = user;
        this.password = pw;

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

    public boolean isConnected() {
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
                .replace("%cuboid%", area.getCuboidType().toString())
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
            executeStatement("INSERT INTO Version (Major, Minor, Build) VALUES (1,4,3);");
            executeStatement("CREATE TABLE IF NOT EXISTS Sectors (Id CHAR(36), GreaterCorner VARCHAR(255), LesserCorner VARCHAR(255), INTEGER Depth, PRIMARY KEY (Id));");
            executeStatement("CREATE TABLE IF NOT EXISTS Bundle (BundleName VARCHAR(36), FlagName VARCHAR(36), CONSTRAINT pk_BundleEntry PRIMARY KEY (BundleName, FlagName));");
            executeStatement("CREATE TABLE IF NOT EXISTS Price (FlagName VARCHAR(36), ProductType VARCHAR(36), Cost DOUBLE, CONSTRAINT pk_FlagType PRIMARY KEY (FlagName, ProductType));");
            executeStatement("CREATE TABLE IF NOT EXISTS Flags (CuboidType VARCHAR(255), WorldId CHAR(36), AreaId CHAR(36), FlagName VARCHAR(36), Setting BOOLEAN, Message VARCHAR(255), CONSTRAINT pk_WorldFlag PRIMARY KEY (CuboidType, WorldId, AreaId, FlagName));");
            executeStatement("CREATE TABLE IF NOT EXISTS Trust (CuboidType VARCHAR(255), WorldId CHAR(36), AreaId CHAR(36), FlagName VARCHAR(36), TrusteeId VARCHAR(36), TrusteeName VARCHAR(255) CONSTRAINT pk_WorldFlag PRIMARY KEY (CuboidType, WorldId, AreaId, FlagName, TrusteeId));");
            executeStatement("CREATE TABLE IF NOT EXISTS PermissionTrust (CuboidType VARCHAR(255), WorldId CHAR(36), AreaId CHAR(36), FlagName VARCHAR(36), Permission VARCHAR(36) CONSTRAINT pk_WorldFlag PRIMARY KEY (CuboidType, WorldId, AreaId, FlagName, Permission));");
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
    public void update(JavaPlugin plugin) {
        Logger.info("No Database Updates Necessary.");
    }

    @Override
    public Set<String> readBundles() {
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
    public Set<Flag> readBundle(String name) {
        final ResultSet results = executeQuery("SELECT * FROM Bundle WHERE BundleName='" + name + "';");
        HashSet<Flag> flags = new HashSet<Flag>();

        try {
            while (results.next()) {
                String flagName = results.getString("FlagName");
                if (Flags.getRegistrar().getFlag(flagName) != null) {
                    flags.add(Flags.getRegistrar().getFlag(flagName));
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
    public void writeBundle(String bundleName, Set<Flag> flags) {
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
                + " WHERE CuboidType='%cuboid%' AND WorldId='%world%' AND AreaId='%area%' AND FlagName='%flag%';";

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
        String insertString = "INSERT INTO Flags (CuboidType, WorldId, AreaId, FlagName, Setting)"
                + " VALUES ('%cuboid%', '%world%', '%area%', '%flag%', %setting%)"
                + " ON DUPLICATE KEY UPDATE Setting=%setting%;";

        executeStatement(areaBuilder(insertString, area)
                .replace("%flag%", flag.getName())
                .replace("%setting%", String.valueOf(setting)));
    }

    @Override
    public String readMessage(Area area, Flag flag) {
        String selectString = "SELECT * FROM Flags"
                + "WHERE CuboidType='%cuboid%' AND WorldId='%world%' AND AreaId='%area%' AND FlagName='%flag%'";

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

        insertString = "INSERT INTO Flags (CuboidType, WorldId, AreaId, FlagName, Message)" +
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
        String selectString = "SELECT * FROM Trust WHERE CuboidType='%cuboid%' AND WorldId='%world%' AND AreaId='%area%' AND FlagName='%flag%';";

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
    public Set<Permission> readPermissionTrust(Area area, Flag flag) {
        String selectString = "SELECT * FROM PermissionTrust WHERE CuboidType='%cuboid%' AND WorldId='%world%' AND AreaId='%area%' AND FlagName='%flag%';";

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
        String deleteString = "DELETE FROM Trust WHERE CuboidType='%cuboid%' AND WorldId='%world%' AND AreaId='%area%' AND FlagName='%flag%';";

        executeStatement(areaBuilder(deleteString, area)
                .replace("%flag%", flag.getName()));

        String insertString = "INSERT INTO Trust (CuboidType, WorldId, AreaId, FlagName, TrusteeId, TrusteeName)"
                + "VALUES('%cuboid%', '%world%', '%area%', '%flag%', '%player%', '%playername%');";

        for (UUID u : players.keySet()) {
            executeStatement(areaBuilder(insertString, area)
                    .replace("%flag%", flag.getName())
                    .replace("%player%", u.toString())
                    .replace("%playername%", players.get(u)));
        }
    }

    @Override
    public void writePermissionTrust(Area area, Flag flag, Set<Permission> permissions) {
        // Delete the old list to be replaced
        String deleteString = "DELETE FROM PermissionTrust WHERE CuboidType='%cuboid%' AND WorldId='%world%' AND AreaId='%area%' AND FlagName='%flag%';";

        executeStatement(areaBuilder(deleteString, area)
                .replace("%flag%", flag.getName()));

        String insertString = "INSERT INTO Trust (CuboidType, WorldId, AreaId, FlagName, TrustId, TrustName)"
                + "VALUES('%cuboid%', '%world%', '%area%', '%flag%', '%player%', '%playername%');";

        for (Permission p : permissions) {
            executeStatement(areaBuilder(insertString, area)
                    .replace("%flag%", flag.getName())
                    .replace("%permission%", p.getName()));
        }
    }

    @Override
    public boolean readInheritance(Area area) {
        if (!(area instanceof Subdivision) || !((Subdivision) area).isSubdivision()) {
            return false;
        }

        String selectString = "SELECT * FROM Flags WHERE CuboidType='%cuboid%' AND WorldId='%world%' AND AreaId='%area%' AND FlagName='InheritParent';";

        ResultSet results = executeQuery(areaBuilder(selectString, area));

        try {
            return !results.next() || results.getBoolean("Setting");
        } catch (SQLException ex) {
            SqlError(ex.getMessage());
        }
        return true;
    }

    @Override
    public void writeInheritance(Area area, boolean setting) {
        if (!(area instanceof Subdivision) || !((Subdivision) area).isSubdivision()) {
            return;
        }

        String insertString = "INSERT INTO Flags (CuboidType, WorldId, AreaId, FlagName, Setting) "
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
                UUID parent = null;
                if (!results.getString("ParentId").equals("null")) {
                    parent = UUID.fromString(results.getString("ParentId"));
                }

                SectorLocation greater = new SectorLocation(results.getString("GreaterCorner"));
                SectorLocation lesser = new SectorLocation(results.getString("LesserCorner"));
                int depth = results.getInt("Depth");

                sectors.put(id, new Sector(id, greater, lesser, depth, parent));
            }
        } catch (SQLException ex) {
            Logger.error("Failed to read sectors from MySQL.");
        }
        return sectors;
    }

    @Override
    public void writeSector(Sector sector) {
        String insertString = "INSERT INTO Sectors (Id, ParentId, GreaterCorner, LesserCorner, Depth) "
                + "VALUES ('%id%', '%parent%', '%greater%', '%lesser%', %depth%) " +
                "ON DUPLICATE KEY UPDATE GreaterCorner=%greater%, LesserCorner=%lesser%, Depth=%depth%;";

        insertString = insertString.replace("%id%", sector.getID().toString()).replace("%depth%", String.valueOf(sector.getDepth()));
        insertString = insertString.replace("%greater%", sector.getGreaterCorner().toString()).replace("%lesser%", sector.getLesserCorner().toString());
        if (sector.getParentID() != null) {
            insertString = insertString.replace("%parent%", sector.getParentID().toString());
        } else {
            insertString = insertString.replace("%parent%", "null");
        }

        executeStatement(insertString);
    }

    @Override
    public void deleteSector(UUID sID) {
        String deleteString = "DELETE FROM Sectors WHERE Id='%id%';";
        executeStatement(deleteString.replace("%id%", sID.toString()));
    }

    @Override
    public void remove(Area area) {
        String deleteString = "DELETE FROM %table% WHERE CuboidType='%cuboid%' AND WorldId='%world%' AND AreaId='%area%';";
        executeStatement(areaBuilder(deleteString, area)
                .replace("%table%", "Flags"));

        executeStatement(areaBuilder(deleteString, area)
                .replace("%table%", "Trust"));

        executeStatement(areaBuilder(deleteString, area)
                .replace("%table%", "PermissionTrust"));
    }
}
