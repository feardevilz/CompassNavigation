package com.gmail.adamwoollen.CompassNavigation;

import java.io.File;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.earth2me.essentials.Warps;

public class EssentialsHandler {

	public Warps warps;
	
	public EssentialsHandler(CompassNavigation plugin) {
		try {
			File directory = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll("%20", ""));
			File essentials = new File(directory.getParentFile().getPath(), "Essentials");
			warps = new Warps(plugin.getServer(), essentials);
		} catch (Exception ex) {
			plugin.getLogger().severe("Couldn't set up Essentials!");
		}
	}
	
	public Location getWarp(Player player, String warp) {
		try {
			warps.reloadConfig();
			return warps.getWarp(warp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
}