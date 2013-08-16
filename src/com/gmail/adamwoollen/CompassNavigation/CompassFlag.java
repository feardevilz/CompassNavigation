package com.gmail.adamwoollen.CompassNavigation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;

public class CompassFlag extends StateFlag {
	
	public CompassNavigation plugin;
	public CompassFlag flag = new CompassFlag();

	public CompassFlag() {
		super("compass", true);
	}
	
	public List<Flag<?>> elements() {
		List<Flag<?>> elements = new ArrayList<Flag<?>>(Arrays.asList(DefaultFlag.getFlags()));
		elements.add(flag);
		return elements;
	}

	public boolean setAllowsFlag(ApplicableRegionSet set) {
		return set.allows(flag);
	}

	public void addFlag() {
		try {
			Field field = DefaultFlag.class.getDeclaredField("flagsList");

			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

			field.setAccessible(true);

			List<Flag<?>> elements = elements();

			Flag<?> list[] = new Flag<?>[elements.size()];
			for (int i = 0; i < elements.size(); i++) {
				list[i] = elements.get(i);
			}

			field.set(null, list);

			Field grm = WorldGuardPlugin.class.getDeclaredField("globalRegionManager");
			grm.setAccessible(true);
			
			GlobalRegionManager globalRegionManager = (GlobalRegionManager) grm.get(Bukkit.getServer().getPluginManager().getPlugin("WorldGuard"));

			globalRegionManager.preload();
		} catch (Exception e) {
			
		}
	}
}