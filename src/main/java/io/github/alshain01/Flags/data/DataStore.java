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

package io.github.alshain01.Flags.data;

import io.github.alshain01.Flags.Flag;
import io.github.alshain01.Flags.area.Area;
import io.github.alshain01.Flags.economy.EPurchaseType;

import java.util.Set;

import org.bukkit.plugin.java.JavaPlugin;

public interface DataStore {
	public boolean create(JavaPlugin plugin);

	public Set<Flag> readBundle(String bundleName);

	public Set<String> readBundles();

	public Boolean readFlag(Area area, Flag flag);

	public boolean readInheritance(Area area);

	public String readMessage(Area area, Flag flag);

	public double readPrice(Flag flag, EPurchaseType type);

	public Set<String> readTrust(Area area, Flag flag);

    public Set<String> readPlayerTrust(Area area, Flag flag);

    public Set<String> readPermissionTrust(Area area, Flag flag);

	public DBVersion readVersion();

	public boolean reload();

	public void remove(Area area);

	public void update(JavaPlugin plugin);

	public void writeBundle(String bundleName, Set<Flag> flags);

	public void writeFlag(Area area, Flag flag, Boolean value);

	public void writeInheritance(Area area, boolean value);

	public void writeMessage(Area area, Flag flag, String message);

	public void writePrice(Flag flag, EPurchaseType type, double price);

	public void writeTrust(Area area, Flag flag, Set<String> players);

    public DataStoreType getType();
}
