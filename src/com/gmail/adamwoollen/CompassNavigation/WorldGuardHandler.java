package com.gmail.adamwoollen.CompassNavigation;

import org.bukkit.Location;

import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;

public class WorldGuardHandler {
	
	public WorldGuardPlugin worldGuard;
	public CompassFlag flag;

	public WorldGuardHandler(CompassNavigation plugin) {
		this.worldGuard = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
		this.flag = new CompassFlag();
		flag.addFlag();
	}

	public boolean canUseCompassHere(Location location) {
		ApplicableRegionSet regions = worldGuard.getGlobalRegionManager().get(location.getWorld()).getApplicableRegions(BukkitUtil.toVector(location));
		return flag.setAllowsFlag(regions);
	}
}