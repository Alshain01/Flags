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

import java.util.*;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permissible;

/**
 * Class for handling the registration of new flags
 */
@SuppressWarnings("unused, WeakerAccess") // API Class
public final class Registrar {
	private final Map<String, Flag> flagStore = new HashMap<String, Flag>();

	Registrar() {
	}

	/**
	 * Gets a flag based on it's case sensitive name.
	 * 
	 * @param flag
	 *            The flag to retrieve.
	 * @return The flag requested or null if it does not exist.
	 */
	public Flag getFlag(String flag) {
        Validate.notNull(flag);
        return isFlag(flag) ? flagStore.get(flag) : null;
	}

	/**
	 * Gets a set of all registered flag group names.
	 * 
	 * @return A list of names of all the flags registered.
	 */
	public Set<String> getFlagGroups() {
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
    public Set<String> getPermittedFlagGroups(Permissible p) {
        final Set<String> groups = new HashSet<String>();

        for (final Flag flag : flagStore.values()) {
            if(p.hasPermission(flag.getPermission()) && !groups.contains(flag.getGroup())) {
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
	public Flag getFlagIgnoreCase(String flag) {
        Validate.notNull(flag);
		for (final Flag f : getFlags()) {
			if (f.getName().equalsIgnoreCase(flag)) {
				return f;
			}
		}
		return null;
	}

	/**
	 * Gets a set of all registered flag names.
	 * 
	 * @return A list of names of all the flags registered.
	 */
	public Set<String> getFlagNames() {
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
    public Set<Flag> getGroup(String group) {
        Validate.notNull(group);
        final Set<Flag> flags = new HashSet<Flag>();

        for (final Flag flag : flagStore.values()) {
            if (group.equalsIgnoreCase(flag.getGroup())) {
                flags.add(flag);
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
    public Set<Flag> getPermittedGroup(Permissible p, String group) {
        Validate.notNull(p);
        final Set<Flag> flags = new HashSet<Flag>();

        for(Flag f : getGroup(group)) {
            if(p.hasPermission(f.getPermission())) {
                flags.add(f);
            }
        }

        return flags;
    }

    /**
     * Gets a map of flag sets ordered by group
     *
     * @return A map of all the flags for all groups.
     */
    public Map<String, Set<Flag>> getFlagsByGroup() {
        Map<String, Set<Flag>> flagMap = new HashMap<String, Set<Flag>>();
        for(Flag f : flagStore.values()) {
            if(flagMap.containsKey(f.getGroup())) {
                Set<Flag> flags = flagMap.get(f.getGroup());
                flags.add(f);
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
     * @param p The permissibile to check
     * @return A map of all the flags for all groups.
     */
    public Map<String, Set<Flag>> getPermittedFlagsByGroup(Permissible p) {
        Map<String, Set<Flag>> flagMap = new HashMap<String, Set<Flag>>();
        for(Flag f : flagStore.values()) {
            if(p.hasPermission(f.getPermission())) {
                if(flagMap.containsKey(f.getGroup())) {
                    Set<Flag> flags = flagMap.get(f.getGroup());
                    flags.add(f);
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
    public Set<Flag> getStandardFlags() {
        final Set<Flag> flags = new HashSet<Flag>();

        for (final Flag flag : flagStore.values()) {
            if (!flag.isPlayerFlag()) {
                flags.add(flag);
            }
        }

        return flags;
    }

    /**
     * Gets a set of flags defined as player flags
     *
     * @return A set of flags that are player flags
     */
    public Set<Flag> getPlayerFlags() {
        final Set<Flag> flags = new HashSet<Flag>();

        for (final Flag flag : flagStore.values()) {
            if (flag.isPlayerFlag()) {
                flags.add(flag);
            }
        }

        return flags;
    }

    /**
     * Gets a set of flags for the provided permissible
     *
     * @return A set of all the flags the permissible may change
     */
    public Set<Flag> getPermittedFlags(Permissible permissible) {
        Validate.notNull(permissible);
        final Set<Flag> flags = new HashSet<Flag>();
        for(final Flag flag : flagStore.values()) {
            if(permissible.hasPermission(flag.getPermission())) {
                flags.add(flag);
            }
        }
        return flags;
    }

    /**
     * Gets a set of flags for the provided permissible
     *
     * @return A set of all the flags the permissible may bypass
     */
    public Set<Flag> getBypassedFlags(Permissible permissible) {
        Validate.notNull(permissible);
        final Set<Flag> flags = new HashSet<Flag>();
        for(final Flag flag : flagStore.values()) {
            if(permissible.hasPermission(flag.getBypassPermission())) {
                flags.add(flag);
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
	public boolean isFlag(String flag) {
        Validate.notNull(flag);
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
	public Flag register(String name, String description, boolean def, String group) {
        Validate.notNull(name);
        Validate.notNull(description);
        Validate.notNull(group);
		if (flagStore.containsKey(name)) {
			return null;
		}
		final Flag flag = new Flag(name, description, def, group, false, null, null);

		Bukkit.getServer().getPluginManager().addPermission(flag.getPermission());
        Flags.debug("Registering " + name);
		flagStore.put(name, flag);
		return flag;
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
	 * @param worldMessage
	 *            The default message for worlds.
	 * @return The flag if the flag was successfully registered. Null otherwise.
	 */
	public Flag register(String name, String description, boolean def,
			String group, String areaMessage, String worldMessage) {
        Validate.notNull(name);
        Validate.notNull(description);
        Validate.notNull(group);
        Validate.notNull(areaMessage);
        Validate.notNull(worldMessage);

		if (flagStore.containsKey(name)) {
			return null;
		}
		final Flag flag = new Flag(name, description, def, group, true,	areaMessage, worldMessage);

		Bukkit.getServer().getPluginManager().addPermission(flag.getPermission());
		Bukkit.getServer().getPluginManager().addPermission(flag.getBypassPermission());

        Flags.debug("Registering " + name);
		flagStore.put(name, flag);
		return flag;
	}
	
	/**
	 * Registers a set of flags from a formatted yml file
	 * 
	 * @param yaml
	 *            The ModuleYML file containing the flags
	 * @param group
	 *            The group the flags belong in.
	 * @return The set of flags if the flags were successfully registered. May be null or empty.
	 */
	public Set<Flag> register(ModuleYML yaml, String group) {
        Validate.notNull(yaml);
        Validate.notNull(group);

		Set<Flag> flags = new HashSet<Flag>();
		for (final String f : yaml.getModuleData().getConfigurationSection("Flag").getKeys(false)) {
			final ConfigurationSection data = yaml.getModuleData().getConfigurationSection("Flag." + f);
	        Flags.debug("Registering " + f);
			// We don't want to register flags that aren't supported.
			// It would just muck up the help menu.
			// Null value is assumed to support all versions.
			final String api = data.getString("MinimumAPI");
			if (api != null && !Flags.checkAPI(api)) {
				continue;
			}
	
			// The description that appears when using help commands.
			final String desc = data.getString("Description");
			if(desc == null) {
				continue;
			}
	
			final boolean def = !data.isSet("Default") || data.getBoolean("Default");
			final boolean isPlayer = data.isSet("Player") && data.getBoolean("Player");
	
			// The default message players getType while in the area.
			final String area = data.getString("AreaMessage");
	
			// The default message players getType while in an world.
			final String world = data.getString("WorldMessage");
			
			if(isPlayer && (area == null || world == null)) {
				continue;
			}
			
			// Register it!
			// Be sure to send a plug-in name or group description for the help command!
			// It can be this.getName() or another string.
			if (isPlayer) {
				flags.add(register(f, desc, def, group, area, world));
			} else {
				flags.add(register(f, desc, def, group));
			}
		}
		return flags;
	}
}
