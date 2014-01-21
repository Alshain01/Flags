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

import io.github.alshain01.Flags.Bundle;
import io.github.alshain01.Flags.Flag;
import io.github.alshain01.Flags.Flags;
import io.github.alshain01.Flags.Message;
import io.github.alshain01.Flags.area.Area;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

final class BundleCmd extends Common {
	protected static void get(Player player, ECommandLocation location, String bundleName) {
		Area area = getArea(player, location);
		Set<Flag> bundle = Flags.getDataStore().readBundle(bundleName);
		
		if(!Validate.isArea(player, area)
				|| !Validate.isBundle(player, bundle, bundleName)
				|| !Validate.isBundlePermitted(player, area)
				|| !Validate.isBundlePermitted(player, bundleName))
		{ return; }

		for(Flag flag : bundle) {
    		player.sendMessage(Message.GetBundle.get()
    				.replaceAll("\\{Bundle\\}", flag.getName())
    				.replaceAll("\\{Value\\}", getValue(area.getValue(flag, false))));
		}
	}
	
	protected static void set(Player player, ECommandLocation location, String bundleName, Boolean value) {
		boolean success = true;
		Area area = getArea(player, location);
		Set<Flag> bundle = Flags.getDataStore().readBundle(bundleName);
		
		if(!Validate.isArea(player, area)
				|| !Validate.isBundle(player, bundle, bundleName)
				|| !Validate.isBundlePermitted(player, area)
				|| !Validate.isBundlePermitted(player, bundleName))
		{ return; }
		
		for(Flag flag : bundle) {
        	if(!area.setValue(flag, value, player)) { success = false; }
        }
        
		player.sendMessage((success ? Message.SetBundle.get() : Message.SetMultipleFlagsError.get())
    			.replaceAll("\\{AreaType\\}", area.getAreaType().toLowerCase())
    			.replaceAll("\\{Bundle\\}", bundleName)
    			.replaceAll("\\{Value\\}", getValue(value).toLowerCase()));
	}
	
	protected static void remove(Player player, ECommandLocation location, String bundleName) {
		boolean success = true;
		Area area = getArea(player, location);
		Set<Flag> bundle = Flags.getDataStore().readBundle(bundleName);
		
		if(!Validate.isArea(player, area)
				|| !Validate.isBundle(player, bundle, bundleName)
				|| !Validate.isBundlePermitted(player, area)
				|| !Validate.isBundlePermitted(player, bundleName))
		{ return; }
		
		for (Flag flag : bundle) {
    		if (!area.setValue(flag, null, player)) { success = false; }
		}
		
		player.sendMessage((success ? Message.RemoveBundle.get() : Message.RemoveAllFlags.get())
				.replaceAll("\\{AreaType\\}", area.getAreaType().toLowerCase())
				.replaceAll("\\{Bundle\\}", bundleName));
	}

    protected static boolean trust(Player player, ECommandLocation location, String bundleName, Set<String> playerList) {
        if(playerList.size() == 0) { return false; }

        Area area = getArea(player, location);
        if(!Bundle.isBundle(bundleName)
                || !Validate.isArea(player, area)
                || !Validate.isBundlePermitted(player, bundleName)
                || !Validate.isPermitted(player, area))
        { return true; }

        boolean success = true;

        for(Flag f : Bundle.getBundle(bundleName)) {
            if(!f.isPlayerFlag()) { continue; }

            for(String p : playerList) {
                if(!area.setTrust(f, p, true, player)) { success = false; }
            }
        }

        player.sendMessage((success ? Message.SetTrust.get() : Message.SetTrustError.get())
                .replaceAll("\\{AreaType\\}", area.getAreaType().toLowerCase())
                .replaceAll("\\{Flag\\}", bundleName));
        return true;
    }

    protected static void distrust(Player player, ECommandLocation location, String bundleName, Set<String> playerList) {
        boolean success = true;
        Area area = getArea(player, location);

        if(!Bundle.isBundle(bundleName)
                || !Validate.isArea(player, area)
                || !Validate.isBundlePermitted(player, bundleName)
                || !Validate.isPermitted(player, area))
        { return; }

        for(Flag f : Bundle.getBundle(bundleName)) {
            if(!f.isPlayerFlag()) { continue; }

            Set<String> trustList = area.getTrustList(f);
            if(trustList == null || !trustList.isEmpty()) { continue; }

            //If playerList is empty, remove everyone
            for(String p : playerList.isEmpty() ? trustList : playerList) {
                if (!area.setTrust(f, p, false, player)) { success = false; }
            }
        }

        player.sendMessage((success ? Message.RemoveTrust.get() : Message.RemoveTrustError.get())
                .replaceAll("\\{AreaType\\}", area.getAreaType().toLowerCase())
                .replaceAll("\\{Flag\\}", bundleName));
    }
	
	protected static void add(CommandSender sender, String bundleName, Set<String> flags) {
		if(sender instanceof Player && !Validate.canEditBundle(sender)){ return; }
	
		Flag flag;
		Set<Flag> bundle = Flags.getDataStore().readBundle(bundleName);
		
		if(bundle == null) {
			Permission perm = new Permission("flags.bundle." + bundleName, 
					"Grants ability to use the bundle " + bundleName, PermissionDefault.FALSE);
			perm.addParent("flags.bundle", true);
			Bukkit.getServer().getPluginManager().addPermission(perm);
			
			bundle = new HashSet<Flag>();
		}
		
		for(String f : flags) {
			flag = Flags.getRegistrar().getFlagIgnoreCase(f);
        	if (flag == null) {
        		sender.sendMessage(Message.AddBundleError.get());
        		return;
       		}
        	bundle.add(flag);
		}
       	
		Flags.getDataStore().writeBundle(bundleName, bundle);
		sender.sendMessage(Message.UpdateBundle.get()
				.replaceAll("\\{Bundle\\}", bundleName));
	}
	
	protected static void delete(CommandSender sender, String bundleName, Set<String> flags) {
		if(sender instanceof Player && !Validate.canEditBundle(sender)){
            Flags.debug("Bundle permission error.");
            return; }
		
		boolean success = true;
		Set<Flag> bundle = Flags.getDataStore().readBundle(bundleName.toLowerCase());
		
		if(!Validate.isBundle(sender, bundle, bundleName)) {
            Flags.debug("Invalid Bundle");
            return; }

		for(String s : flags) {
            Flag flag = Flags.getRegistrar().getFlag(s);
            Flags.debug("Remvoing " + s + " from bundle.");
            if (flag == null || !bundle.remove(flag)) {
                Flags.debug("Failed to delete flag from bundle");
                success = false; }
		}
		Flags.getDataStore().writeBundle(bundleName, bundle);
		
		sender.sendMessage((success ? Message.UpdateBundle.get() : Message.RemoveAllFlagsError.get())
				.replaceAll("\\{Bundle\\}", bundleName));
	}
	
	protected static void erase(CommandSender sender, String bundleName) {
		if(sender instanceof Player && !Validate.canEditBundle(sender)){ return; }
		
		Set<String> bundles = Flags.getDataStore().readBundles();
		if (bundles == null || bundles.size() == 0 || !bundles.contains(bundleName)) {
			sender.sendMessage(Message.EraseBundleError.get());
			return;
		}
		
		Flags.getDataStore().writeBundle(bundleName, null);
		Bukkit.getServer().getPluginManager().removePermission("flags.bundle." + bundleName);
		
		sender.sendMessage(Message.EraseBundle.get()
				.replaceAll("\\{Bundle\\}", bundleName));
	}
	
	protected static void help (CommandSender sender, int page) {
		Set<String> bundles = Flags.getDataStore().readBundles();
		if (bundles == null || bundles.size() == 0) { 
			sender.sendMessage(Message.NoFlagFound.get()
					.replaceAll("\\{Type\\}", Message.Bundle.get()));
			return;
		}
		
		//Get total pages: 1 header per page
		//9 flags per page, except on the first which has a usage line and 8 flags
		int total = ((bundles.size() + 1) / 9);
		if ((bundles.size() + 1) % 9 != 0) { 
			total++; // Add the last page, if the last page is not full (less than 9 flags) 
		}
		
		//Check the page number requested
        if (page < 1 || page > total) {
        	page = 1;
        }
        
		sender.sendMessage(Message.HelpHeader.get()
				.replaceAll("\\{Type\\}", Message.Bundle.get())
                .replaceAll("\\{Group}", Message.Index.get())
				.replaceAll("\\{Page\\}", String.valueOf(page))
				.replaceAll("\\{TotalPages\\}", String.valueOf(total))
				.replaceAll("\\{Type\\}", Message.Bundle.get()));
		
		// Setup for only displaying 10 lines at a time
		int lineCount = 1;
		
		// Usage line.  Displays only on the first page.
		if (page == 1) {
			sender.sendMessage(Message.HelpInfo.get()
					.replaceAll("\\{Type\\}", Message.Bundle.get().toLowerCase()));
			lineCount++;
		}
		
		// Because the first page has 1 less flag count than the rest, 
		// manually initialize the loop counter by subtracting one from the 
		// start position of all pages other than the first.
		int loop = 0;
		if (page > 1) {
			loop = ((page-1)*9)-1;
		}
		
		String[] bundleArray = new String[bundles.size()];
		bundleArray = bundles.toArray(bundleArray);
		
		// Show the flags
		for (; loop < bundles.size(); loop++) {
			Set<Flag> flags = Flags.getDataStore().readBundle(bundleArray[loop]);
			if (flags == null) { continue; }
			StringBuilder description = new StringBuilder("");
			boolean first = true;

			for (Flag flag : flags) {
				if(!first){
					description.append(", ");
				} else {
					first = false;
				}
				
				description.append(flag.getName());
			}
			sender.sendMessage(Message.HelpTopic.get()
					.replaceAll("\\{Topic\\}", bundleArray[loop])
					.replaceAll("\\{Description\\}", description.toString()));

			lineCount++;
			
			if (lineCount > 9) {
				return; // Page is full, we're done
			}
		}
	}
}
