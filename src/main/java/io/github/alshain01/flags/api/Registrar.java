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

package io.github.alshain01.flags.api;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permissible;

import javax.annotation.Nonnull;

/**
 * Class for handling the registration of new flags
 */
@SuppressWarnings("unused")
public final class Registrar {
	private final Map<String, Flag> flagStore = new HashMap<String, Flag>();

	Registrar() { }

	/**
	 * Gets a flag based on it's case sensitive name.
	 * 
	 * @param flag
	 *            The flag to retrieve.
	 * @return The flag requested or null if it does not exist.
	 */
	public Flag getFlag(@Nonnull String flag) {
        return isFlag(flag) ? flagStore.get(flag).clone() : null;
	}

	/**
	 * Gets a set of all registered flag group names.
	 * 
	 * @return A list of names of all the flags registered.
	 */
	public Collection<String> getFlagGroups() {
		final Set<String> groups = new HashSet<String>();

		for (final Flag flag : flagStore.values()) {
			if (!groups.contains(flag.getGroup())) {
				groups.add(flag.getGroup());
			}
		}
		return groups;
	}

    /**
     * Gets a set of all registered flag group names that
     * contain flags the permissible is allowed to use.
     *
     * @return A list of names of all the flags registered.
     */
    public Collection<String> getPermittedFlagGroups(@Nonnull Permissible permissible) {
        final Set<String> groups = new HashSet<String>();

        for (final Flag flag : flagStore.values()) {
            if(permissible.hasPermission(flag.getPermission()) && !groups.contains(flag.getGroup())) {
                groups.add(flag.getGroup());
            }
        }
        return groups;
    }

	/**
	 * Gets a flag, ignoring the case.
	 * 
	 * This is an less efficient method, use it only when absolutely necessary.
	 * 
	 * @param flag
	 *            The flag to retrieve.
	 * @return The flag requested or null if it does not exist.
	 */
	public Flag getFlagIgnoreCase(@Nonnull String flag) {
		for (final Flag f : getFlags()) {
			if (f.getName().equalsIgnoreCase(flag)) {
				return f.clone();
			}
		}
		return null;
	}

	/**
	 * Gets a set of all registered flag names.
	 * 
	 * @return A list of names of all the flags registered.
	 */
	public Collection<String> getFlagNames() {
		return flagStore.keySet();
	}

	/**
	 * Gets a collection of all registered flags.
	 * 
	 * @return A collection of all the flags registered.
	 */
	public Collection<Flag> getFlags() {
		return flagStore.values();
	}

    /**
     * Gets a set of flags for the provided group
     *
     * @return A set of all the flags in the group.
     */
    public Collection<Flag> getGroup(@Nonnull String group) {
        final Set<Flag> flags = new HashSet<Flag>();

        for (final Flag flag : flagStore.values()) {
            if (group.equalsIgnoreCase(flag.getGroup())) {
                flags.add(flag.clone());
            }
        }
        return flags;
    }

    /**
     * Gets a set of flags for the provided group
     * that the permissible has permission to use.
     *
     * @return A set of all the flags in the group.
     */
    public Collection<Flag> getPermittedFlagGroup(@Nonnull Permissible permissible, @Nonnull String group) {
        final Set<Flag> flags = new HashSet<Flag>();

        for(Flag f : getGroup(group)) {
            if(permissible.hasPermission(f.getPermission())) {
                flags.add(f.clone());
            }
        }

        return flags;
    }

    /**
     * Gets a map of flag sets ordered by group
     *
     * @return A map of all the flags for all groups.
     */
    public Map<String, Collection<Flag>> getFlagsByGroup() {
        Map<String, Collection<Flag>> flagMap = new HashMap<String, Collection<Flag>>();
        for(Flag f : flagStore.values()) {
            if(flagMap.containsKey(f.getGroup())) {
                Collection<Flag> flags = flagMap.get(f.getGroup());
                flags.add(f.clone());
                flagMap.put(f.getGroup(), flags);
            } else {
                flagMap.put(f.getGroup(), new HashSet<Flag>(Arrays.asList(f)));
            }
        }
        return flagMap;
    }

    /**
     * Gets a map of flag sets ordered by group that are permitted for use
     *
     * @param permissible The permissibile to check
     * @return A map of all the flags for all groups.
     */
    public Map<String, Collection<Flag>> getPermittedFlagsByGroup(@Nonnull Permissible permissible) {
        Map<String, Collection<Flag>> flagMap = new HashMap<String, Collection<Flag>>();
        for(Flag f : flagStore.values()) {
            if(permissible.hasPermission(f.getPermission())) {
                if(flagMap.containsKey(f.getGroup())) {
                    Collection<Flag> flags = flagMap.get(f.getGroup());
                    flags.add(f.clone());
                    flagMap.put(f.getGroup(), flags);
                } else {
                    flagMap.put(f.getGroup(), new HashSet<Flag>(Arrays.asList(f)));
                }
            }
        }
        return flagMap;
    }

    /**
     * Gets a set of flags not defined as player flags
     *
     * @return A set of flags that are not player flags
     */
    public Collection<Flag> getStandardFlags() {
        final Set<Flag> flags = new HashSet<Flag>();

        for (final Flag flag : flagStore.values()) {
            if (!flag.isPlayerFlag()) {
                flags.add(flag.clone());
            }
        }

        return flags;
    }

    /**
     * Gets a set of flags defined as player flags
     *
     * @return A set of flags that are player flags
     */
    public Collection<Flag> getPlayerFlags() {
        final Set<Flag> flags = new HashSet<Flag>();

        for (final Flag flag : flagStore.values()) {
            if (flag.isPlayerFlag()) {
                flags.add(flag.clone());
            }
        }

        return flags;
    }

    /**
     * Gets a set of flags for the provided permissible
     *
     * @return A set of all the flags the permissible may change
     */
    public Collection<Flag> getPermittedFlags(@Nonnull Permissible permissible) {
        final Set<Flag> flags = new HashSet<Flag>();
        for(final Flag flag : flagStore.values()) {
            if(permissible.hasPermission(flag.getPermission())) {
                flags.add(flag.clone());
            }
        }
        return flags;
    }

    /**
     * Gets a set of flags for the provided permissible
     *
     * @return A set of all the flags the permissible may bypass
     */
    public Collection<Flag> getBypassedFlags(@Nonnull Permissible permissible) {
        final Set<Flag> flags = new HashSet<Flag>();
        for(final Flag flag : flagStore.values()) {
            if(permissible.hasPermission(flag.getBypassPermission())) {
                flags.add(flag.clone());
            }
        }
        return flags;
    }

	/**
	 * Checks if a flag name has been registered.
	 * 
	 * @param flag
	 *            The flag name
	 * @return True if the flag name has been registered
	 */
	public boolean isFlag(@Nonnull String flag) {
		return flagStore.containsKey(flag);
	}

	/**
	 * Registers a non-player flag
	 * 
	 * @param name
	 *            The name of the flag
	 * @param description
	 *            A brief description of the flag
	 * @param def
	 *            The flag's default state
	 * @param group
	 *            The group the flag belongs in.
	 * @return The flag if the flag was successfully registered. Null otherwise.
	 */
	public Flag registerFlag(@Nonnull String name, @Nonnull String description, boolean def, @Nonnull String group) {
        if(name.length() > 36) { name = name.substring(0, 35); }

		if (flagStore.containsKey(name)) {
			return null;
		}
		final Flag flag = new Flag(name, description, def, group, false, null, null);

		Bukkit.getServer().getPluginManager().addPermission(flag.getPermission());
		flagStore.put(name, flag);
		return flag.clone();
	}

	/**
	 * Registers a player flag
	 * 
	 * @param name
	 *            The name of the flag
	 * @param description
	 *            A brief description of the flag
	 * @param def
	 *            The flag's default state
	 * @param group
	 *            The group the flag belongs in.
	 * @param areaMessage
	 *            The default message for areas.
	 * @param wildernessMessage
	 *            The default message for wilderness areas.
	 * @return The flag if the flag was successfully registered. Null otherwise.
	 */
	public Flag registerFlag(@Nonnull String name, @Nonnull String description, boolean def,
                             @Nonnull String group, @Nonnull String areaMessage, @Nonnull String wildernessMessage) {
        if(name.length() > 36) { name = name.substring(0, 35); }

		if (flagStore.containsKey(name)) {
			return null;
		}
		final Flag flag = new Flag(name, description, def, group, true,	areaMessage, wildernessMessage);

		Bukkit.getServer().getPluginManager().addPermission(flag.getPermission());
		Bukkit.getServer().getPluginManager().addPermission(flag.getBypassPermission());

		flagStore.put(name, flag);
		return flag.clone();
	}

    /**
     * Registers a set of flags from a formatted ConfigurationSection
     *
     * @param data
     *            The configuration section file containing the flag keys
     * @param group
     *            The group the flags belong in.
     * @return The set of flags if the flags were successfully registered. May be null or empty.
     */
    public Collection<Flag> registerFlag(@Nonnull ConfigurationSection data, @Nonnull String group) {
        Set<Flag> flags = new HashSet<Flag>();
        for (final String f : data.getKeys(false)) {
            final ConfigurationSection flagSection = data.getConfigurationSection(f);
            // We don't want to register flags that aren't supported.
            // It would just muck up the help menu.
            // Null value is assumed to support all versions.
            final String api = flagSection.getString("MinimumAPI");
            if (api != null && !FlagsAPI.checkAPI(api)) {
                continue;
            }

            // The description that appears when using help commands.
            final String desc = flagSection.getString("Description");
            if(desc == null) {
                continue;
            }

            final boolean def = !flagSection.isSet("Default") || flagSection.getBoolean("Default");
            final boolean isPlayer = flagSection.isSet("Player") && flagSection.getBoolean("Player");

            // The default message players getType while in the area.
            final String area = flagSection.getString("AreaMessage");

            // The default message players getType while in the wilderness.
            String wilderness = flagSection.getString("WildernessMessage");
            if(wilderness == null) {
                // Backward compatibility
                wilderness = flagSection.getString("WorldMessage");
            }

            if(isPlayer && (area == null || wilderness == null)) {
                continue;
            }

            // Register it!
            // Be sure to send a plug-in name or group description for the help command!
            // It can be this.getName() or another string.
            Flag flag;
            if (isPlayer) {
                flag = registerFlag(f, desc, def, group, area, wilderness);
            } else {
                flag = registerFlag(f, desc, def, group);
            }

            if(flag != null)
                flags.add(flag);
        }
        return flags;
    }
}
