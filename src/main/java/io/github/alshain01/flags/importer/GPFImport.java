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

package io.github.alshain01.flags.importer;

import io.github.alshain01.flags.Flag;
import io.github.alshain01.flags.Flags;
import io.github.alshain01.flags.area.Area;
import io.github.alshain01.flags.area.Default;
import io.github.alshain01.flags.area.GriefPreventionClaim78;
import io.github.alshain01.flags.area.Subdivision;
import io.github.alshain01.flags.area.World;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;

public final class GPFImport {
	private static final String dataFolder = "plugins\\GriefPreventionFlags\\";
	private static ImportYML world = null;
	private static ImportYML data = null;

	private static boolean dataExists() {
		File fileObject = new File(dataFolder + "data.yml");
		if (!fileObject.exists()) {
			return false;
		}
		data = new ImportYML(fileObject.getAbsolutePath());

		fileObject = new File(dataFolder + "world.yml");
		if (fileObject.exists()) {
			world = new ImportYML(fileObject.getAbsolutePath());
		}

		return true;
	}

	private static String getVersion() {
		return data.getCustomConfig().getString("data.database.version");
	}

	private static void importData(ImportYML data, String header) {
		if (data.getCustomConfig().getString(header) != null) {
			// Get a deep list of all the values to parse through.
			final Set<String> keys = data.getCustomConfig()
					.getConfigurationSection(header).getKeys(true);

			for (final String k : keys) {
				if (k.toLowerCase().contains("database")) {
					continue;
				} // Database version for GPFlags, not used by Flags.
				final String[] path = k.split("\\.");

				// Acquire the claim

				Area area;
				if (path.length == 3) {
					if (header.equalsIgnoreCase("data")) {
						// Subdivision
						area = new GriefPreventionClaim78(Long.valueOf(path[0]), Long.valueOf(path[1]));
					} else if (header.equalsIgnoreCase("world")
							&& path[1].equalsIgnoreCase("unclaimed")) {
						// World
						area = new World(Bukkit.getServer().getWorld(path[0]));
					} else if (header.equalsIgnoreCase("world")
							&& path[1].equalsIgnoreCase("global")) {
						// Default
						area = new Default(Bukkit.getServer().getWorld(path[0]));
					} else {
						continue;
					}
				} else if (path.length == 2) {
					if (header.equalsIgnoreCase("data")) {
						// Claim
						area = new GriefPreventionClaim78(Long.valueOf(path[0]));
					} else {
						continue;
					}
				} else {
					continue;
				}

				if (!area.isArea()) {
					continue;
				}

				// Acquire the flag name
				String flagName = path[path.length - 1].toLowerCase();

				// Parse a trust list
				if (flagName.contains("trust")) {
					flagName = flagName.replace("trust", "");
					final Flag flag = Flags.getRegistrar().getFlagIgnoreCase(
							flagName);
					if (flag == null) {
						continue;
					}

					final List<String> players = 
							readList(data, header + "."	+ k);
					if (players != null) {
						for (final String p : players) {
							area.setTrust(flag, p, true, null);
						}
					}
					continue;
				}

				// Parse a message
				if (flagName.contains("message")) {
					flagName = flagName.replace("message", "");
					final Flag flag = Flags.getRegistrar().getFlagIgnoreCase(flagName);
					if (flag == null) {
						continue;
					}

					final String message = data.getCustomConfig().getString(
							header + "." + k);
					area.setMessage(flag, message, null);
					continue;
				}

				if (flagName.contains("inheritparent")) {
					if (area instanceof Subdivision
							&& ((Subdivision) area).isSubdivision()) {
						((Subdivision) area).setInherited(true);
					}
					continue;
				}

				final Flag flag = Flags.getRegistrar().getFlagIgnoreCase(flagName);
				if (flag == null) {
					continue;
				}
				final boolean value = Boolean.valueOf(data.getCustomConfig()
						.getString(header + "." + k));
				area.setValue(flag, value, null);
			}
		}
	}

	public static void importGPF() {
		if (dataExists() && getVersion().equals("1.6.0")) {
			Flags.log("Importing GriefPreventionFlags Database");
			importData(data, "data");
			if (world != null) {
				importData(world, "world");
			}
			final File gpFolder = new File(dataFolder);
            File[] files = gpFolder.listFiles();
            if(files != null && files.length > 0) {
                for (final File file : files) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
                //noinspection ResultOfMethodCallIgnored
                new File(dataFolder).delete();
            }
		}
	}

	private static List<String> readList(ImportYML data, String path) {
		final List<?> listData = data.getCustomConfig().getList(path);
		if (listData == null) {
			return null;
		}

		final List<String> stringData = new ArrayList<String>();
		for (final Object o : listData) {
			stringData.add(((String) o).toLowerCase());
		}
		return stringData;
	}

	private GPFImport() {
	}
}
