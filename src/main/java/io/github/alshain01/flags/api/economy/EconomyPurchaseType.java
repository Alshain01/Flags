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
package io.github.alshain01.flags.api.economy;

import io.github.alshain01.flags.Flags;
import io.github.alshain01.flags.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

/**
 * Enumeration for handling the purchasable product type
 */
public enum EconomyPurchaseType {
	FLAG, MESSAGE;

	public static EconomyPurchaseType get(String name) {
		for (final EconomyPurchaseType p : EconomyPurchaseType.values()) {
			if (name.toLowerCase().equals(p.toString().toLowerCase())
					|| name.toLowerCase().equals(String.valueOf(p.toString().substring(0,1).toLowerCase()))) {
				return p;
			}
		}
		return null;
	}

    private String localization;
    private final boolean refundable;

	EconomyPurchaseType() {
        this.refundable = Bukkit.getServer().getPluginManager().getPlugin("Flags").getConfig()
                .getBoolean("Economy.Refund." + this.toString().substring(0, 1).toUpperCase() + this.toString().substring(1).toLowerCase());
	}

	/**
	 * @return the localized name of the purchase type
	 */
	public String getLocalized() {
		return localization;
	}

	/**
	 * @return true if the refund setting is true for this type
	 */
	public boolean isRefundable() {
		return refundable;
	}

    /**
     * Instruct the enum to reload the localization from the yaml file.
     */
    public static void loadLocalized() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Flags");
        File messageFile = new File(plugin.getDataFolder(), "message.yml");
        if (!messageFile.exists()) {
            plugin.saveResource("message.yml", false);
        }

        YamlConfiguration defaults = ((Flags)plugin).getResourceConfig("message.yml");
        YamlConfiguration messages = YamlConfiguration.loadConfiguration(messageFile);
        messages.setDefaults(defaults);

        for (EconomyPurchaseType t : EconomyPurchaseType.values()) {
            try {
                t.localization = ChatColor.translateAlternateColorCodes('&', messages.getString(t.toString()));
            } catch (NullPointerException ex) {
                Logger.warning("Failed to load message " + t.toString());
            }
        }
    }
}
