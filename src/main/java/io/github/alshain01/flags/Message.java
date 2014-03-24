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

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Class for retrieving localized messages.
 */
public enum Message {
	// Errors
	NoConsoleError, InvalidFlagError, InvalidTrustError, NoFlagFound,
	SetTrustError, RemoveTrustError, RemoveAllFlagsError, SetMultipleFlagsError,
	AddBundleError, FlagPermError, AreaPermError, WildernessPermError, NoAreaError,
	EraseBundleError, BundlePermError, PricePermError, SubdivisionError, NoSystemError,
	PlayerFlagError, EconomyError, SQLDatabaseError, SubdivisionSupportError,
	// Commands
	SetFlag, GetFlag, RemoveFlag, SetTrust, GetTrust, RemoveTrust,
	GetAllFlags, RemoveAllFlags, GetBundle, SetBundle, RemoveBundle, UpdateBundle,
	EraseBundle, SetInherited,
	// Help
	HelpHeader, FlagHelpHeader, HelpTopic, HelpInfo,
	GroupHelpDescription, GroupHelpInfo, FlagDescription,
	// General Translations
	Flag, Bundle, Message, ValueColorTrue, ValueColorFalse, Index, Error, Command,
	// Economy
	SetPrice, GetPrice, LowFunds, Withdraw, Deposit,
    // Sector
    DeleteSector, NoSectorError, DeleteAllSectors, SectorOverlapError,
    CancelCreateSector, SectorCreated, SectorStarted, SubsectorCreated;

    String message;

	/**
	 * Gets the localized message for the enumeration
	 * 
	 * @return the message associated with the enumeration
	 */
	public final String get() {
        return this.message;
	}

    final void set(String message) {
        this.message = message;
    }

    static void load(ConfigurationSection messages) {
        for(Message m : io.github.alshain01.flags.Message.values()) {
            final String message = messages.getString(m.toString());
            if (message == null) {
                Flags.warn("ERROR: Invalid message.yml Message for " + m.toString());
                m.set("ERROR: Invalid message.yml Message. Please contact your server administrator.");
            } else {
                m.set(ChatColor.translateAlternateColorCodes('&', message));
            }
        }
    }
}