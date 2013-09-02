package com.gmail.adamwoollen.CompassNavigation;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class WarmupTimer extends BukkitRunnable {

	public EventListener listener;
	public Player player;
	public int slot = 0;
	
	public WarmupTimer(CompassNavigation plugin, EventListener listener, Player player, int slot) {
		this.listener = listener;
		this.player = player;
		this.slot = slot;
	}
	
	public void run() {
		listener.checkMoney(player, slot);
		listener.timers.remove(player.getName());
		cancel();
	}
}