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

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Modified YAML manager from http://wiki.bukkit.org/Configuration_API_Reference
 * Handles reading YAML from a source file without saving it to disk.
 */
public final class ModuleYML {
	private final JavaPlugin plugin;
	private final String fileName;
	private FileConfiguration fileConfig = null;

	/**
	 * Create an instance of ModuleYML
	 * 
	 * @param plugin
	 *            The plugin that contains the yml file as a resource
	 * @param dataFile
	 *            The file name
	 */
	public ModuleYML(JavaPlugin plugin, String dataFile) {
		this.plugin = plugin;
		fileName = dataFile;
	}

	/**
	 * Retrieves the file configuration for the module data.
	 * 
	 * @return the custom file configuration.
	 */
	public FileConfiguration getModuleData() {
		if (fileConfig == null) {
			reloadModuleData();
		}
		return fileConfig;
	}

	/**
	 * Reloads the file to the MemorySection
	 */
	public void reloadModuleData() {
		try {
			fileConfig = new YamlConfiguration();
			fileConfig.load(plugin.getResource(fileName));
		} catch (final Exception e) {
			plugin.getLogger().severe("Could not load data from " + fileName + " in plugin " + plugin.getName() + ": " + e.getMessage());
		}
	}
}
