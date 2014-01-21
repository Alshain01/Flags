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

enum EBundleCommand {
	SET('s', 4, 0, true, true, "Set <area|world|default> <bundle> <true|false>"),
	GET('g', 3, 0, true, true, "Get <area|world|default> <bundle>"),
	REMOVE('r', 3, 0, true, true, "Remove <area|world|default> <bundle>"),
    TRUST('t', 4, -1, true, true, "Trust <area|world|default> <bundle> <player> [player]..."),
    DISTRUST('d', 3, -1, true, true, "Distrust <area|world|default> <bundle> [player] [player]..."),
	HELP('h', 1, 1, false, null, "Help [page]"),
	ADD('a', 3, -1, false, true, "Add <bundle> <flag> [flag]..."),
	CUT ('c', 3, -1, false, true, "Cut <bundle> <flag> [flag]..."),
	ERASE ('e', 2, 0, false, true, "Erase <bundle>");

	final char alias;
	final int requiredArgs;
	final int optionalArgs; //-1 for infinite
	final boolean requiresLocation;
	final Boolean requiresBundle; // null if bundle isn't even an optional arg.
	final String help;
	
	EBundleCommand(char alias, int requiredArgs, int optionalArgs, boolean hasLocation, Boolean requiresBundle, String help) {
		this.alias = alias;
		this.requiredArgs = requiredArgs;
		this.optionalArgs = optionalArgs;
		this.help = help;
		this.requiresLocation = hasLocation;
		this.requiresBundle = requiresBundle;
	}
	
	protected static EBundleCommand get(String name) {
		for(EBundleCommand c : EBundleCommand.values()) {
			if(name.toLowerCase().equals(c.toString().toLowerCase()) || name.toLowerCase().equals(String.valueOf(c.alias))) {
				return c;
			}
		}
		return null;
	}
	
	protected String getHelp() {
		return "/bundle " + this.help;
	}
}
