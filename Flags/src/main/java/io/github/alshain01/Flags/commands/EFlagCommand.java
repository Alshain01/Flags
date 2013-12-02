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

enum EFlagCommand {
	SET('s', 3, 1, true, true, "Set <area|world|default> <flag> [true|false]"),
	GET('g', 2, 1, true, false, "Get <area|world|default> [flag]"),
	REMOVE ('r', 2, 1, true, false, "Remove <area|world|default> [flag]"),
	TRUST('t', 4, -1, true, true, "Trust <area|world|default> <flag> <player> [player]..."),
	DISTRUST('d', 3, -1, true, true, "Distrust <area|world|default> <flag> [player] [player]..."),
	VIEWTRUST('v', 3, 0, true, true, "ViewTrust <area|world|default> <flag>"),
	MESSAGE('m', 4, -1, true, true, "Message <area|world|default> <flag> <message>"),
	PRESENTMESSAGE('p', 3, 0, true, true, "PresentMessage <area|world|default> <flag>"),
	ERASEMESSAGE('e', 3, 0, true, true, "EraseMessage <area|world|default> <flag>"),
	CHARGE('c', 3, 1, false, true, "Charge <flag|message> <flag> [price]"),
	HELP ('h', 1, 2, false, null, "Help [group] [page]"),
	INHERIT('i', 1, 1, false, null, "Inherit [true|false]");
	
	char alias;
	int requiredArgs;
	int optionalArgs; //-1 for infinite
	boolean requiresLocation;
	Boolean requiresFlag; // null if flag isn't even an optional arg.
	String help;
	
	//Note: requiredArgs INCLUDES the command action
	EFlagCommand(char alias, int requiredArgs, int optionalArgs, boolean hasLocation, Boolean requiresFlag, String help) {
		this.alias = alias;
		this.requiredArgs = requiredArgs;
		this.optionalArgs = optionalArgs;
		this.help = help;
		this.requiresLocation = hasLocation;
		this.requiresFlag = requiresFlag;
	}
	
	protected static EFlagCommand get(String name) {
		for(EFlagCommand c : EFlagCommand.values()) {
			if(name.toLowerCase().equals(c.toString().toLowerCase()) || name.toLowerCase().equals(String.valueOf(c.alias))) {
				return c;
			}
		}
		return null;
	}
	
	protected String getHelp() {
		return "/flag " + this.help;
	}
}
