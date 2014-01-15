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

package io.github.alshain01.Flags.commands;

import io.github.alshain01.Flags.*;
import io.github.alshain01.Flags.area.Area;
import io.github.alshain01.Flags.area.Default;
import io.github.alshain01.Flags.area.World;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
/**
 * Top level class for command based functions
 * 
 * @author Alshain01
 */
abstract class Common {
	protected static String getValue(boolean value) {
        return (value) ? Message.ValueColorTrue.get() : Message.ValueColorFalse.get();
	}

	protected static Area getArea(CommandSender sender, ECommandLocation location) {
		if (location == ECommandLocation.DEFAULT) {
			return new Default((((Player)sender).getWorld()));
		} else if (location == ECommandLocation.WORLD) {
			return new World((((Player)sender).getWorld()));
		} else if (location == ECommandLocation.AREA) {
			Area area = io.github.alshain01.Flags.System.getActive().getAreaAt(((Player) sender).getLocation());
			return (area instanceof World) ? null : area;
		}
		// Invalid location selection
		return null;
	}
}
