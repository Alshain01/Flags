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

import io.github.alshain01.flags.api.AreaPlugin;
import io.github.alshain01.flags.api.Flag;
import io.github.alshain01.flags.api.FlagsAPI;
import io.github.alshain01.flags.api.area.Area;
import io.github.alshain01.flags.api.area.Subdividable;
import io.github.alshain01.flags.api.economy.EconomyPurchaseType;

import java.util.*;

import io.github.alshain01.flags.api.sector.Sector;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Defines the Data Storage for Flags.  Not directly accessible externally.
 */
public abstract class DataStore {
    DataStore() { }

    public final class DataStoreVersion {
        private final int major;
        private final int minor;
        private final int build;

        public DataStoreVersion(int major, int minor, int build) {
            this.major = major;
            this.minor = minor;
            this.build = build;
        }

        public int getMajor() { return major; }
        public int getMinor() { return minor; }
        public int getBuild() { return build; }
    }

    public enum DataStoreType {
        YAML("YAML"),
        MYSQL("MySQL");

        private final String niceName;

        private DataStoreType(String niceName) {
            this.niceName = niceName;
        }

        /**
         * Gets the formatted name of this DataStore type
         * @return The DataStore type name
         */
        public String getName() {
            return niceName;
        }

        public static DataStoreType fromString(String value) {
            return valueOf(value.toUpperCase());
        }
    }

    public abstract void create(JavaPlugin plugin);

    public abstract void reload();

    public abstract void close();

    public abstract DataStoreVersion readVersion();

    public abstract DataStoreType getType();

    public abstract boolean update(JavaPlugin plugin);

    public abstract Set<String> readBundles();

	public abstract Set<Flag> readBundle(String bundleName);

    public abstract void writeBundle(String bundleName, Collection<Flag> flags);

	public abstract Boolean readFlag(Area area, Flag flag);

    public abstract void writeFlag(Area area, Flag flag, Boolean value);

	public abstract String readMessage(Area area, Flag flag);

    public abstract void writeMessage(Area area, Flag flag, String message);

	public abstract double readPrice(Flag flag, EconomyPurchaseType type);

    public abstract void writePrice(Flag flag, EconomyPurchaseType type, double price);

    public abstract Set<OfflinePlayer> readPlayerTrust(Area area, Flag flag);

    public abstract Set<Permission> readPermissionTrust(Area area, Flag flag);

    public abstract void writePlayerTrust(Area area, Flag flag, Set<OfflinePlayer> players);

    public abstract void writePermissionTrust(Area area, Flag flag, Set<Permission> permissions);

    public abstract boolean readInheritance(Subdividable area);

	public abstract void writeInheritance(Subdividable area, boolean value);

    public abstract Map<UUID, Sector> readSectors();

    public abstract void writeSector(Sector sector);

    public abstract void deleteSector(UUID sID);

    public abstract void remove(AreaRemovable area);

    public void importDataStore(DataStore source) {
        migrate(source, this);
    }

    abstract Set<String> getAllAreaIds(World world);

    private static void migrate(DataStore source, DataStore target) {
        Logger.info("Beginning data migration from " + source.getType().getName() + " to " + target.getType().getName() + ".");
        //Convert the bundles
        for(String b : source.readBundles()) {
            target.writeBundle(b, source.readBundle(b));
        }

        //Convert the prices
        for(Flag f : FlagsAPI.getRegistrar().getFlags()) {
            for(EconomyPurchaseType t : EconomyPurchaseType.values()) {
                double price = source.readPrice(f, t);
                if (price > (double) 0) {
                    target.writePrice(f, t, price);
                }
            }
        }

        //Convert the sectors
        if(FlagsAPI.getAreaPlugin() == AreaPlugin.FLAGS) {
            for(Sector s : source.readSectors().values()) {
                target.writeSector(s);
            }
        }

        //Gather the areas to convert
        Set<Area> areas = new HashSet<Area>();
        for(World w : Bukkit.getWorlds()) {
            areas.add(new AreaWilderness(w));
            areas.add(new AreaDefault(w));
            for(String id : source.getAllAreaIds(w)) {
                Area area = FactoryArea.getArea(FlagsAPI.getAreaPlugin(), id);
                if(area != null && area.isArea()) {
                    areas.add(area);
                }
            }
        }

        // Perform the area conversion
        for(Flag f : FlagsAPI.getRegistrar().getFlags()) {
            for(Area a : areas) {
                //Flags
                Boolean value = source.readFlag(a, f);
                if (value != null) {
                    target.writeFlag(a, f, value);
                }

                //Messages
                String message = source.readMessage(a, f);
                if(message != null) {
                    target.writeMessage(a, f, message);
                }

                //Player Trust Lists
                Set<OfflinePlayer> trust = source.readPlayerTrust(a, f);
                if(!trust.isEmpty()) {
                    target.writePlayerTrust(a, f, trust);
                }

                //Permission Trust Lists
                Set<Permission> permtrust = source.readPermissionTrust(a, f);
                if(!trust.isEmpty()) {
                    target.writePermissionTrust(a, f, permtrust);
                }
            }
        }
        Logger.info("Data migration complete.");
    }
}
