package com.gmail.adamwoollen.CompassNavigation;

import org.bukkit.entity.Player;

import lilypad.client.connect.api.Connect;
import lilypad.client.connect.api.request.impl.RedirectRequest;

public class LilypadHandler {

	public CompassNavigation plugin;
	
	public LilypadHandler(CompassNavigation plugin) {
		this.plugin = plugin;
	}
    
    public boolean connect(Player player, int slot) {
    	try {
    		Connect connect = (Connect) plugin.getServer().getServicesManager().getRegistration(Connect.class).getProvider();
    		connect.request(new RedirectRequest(plugin.getConfig().getString(slot + ".Lilypad"), player.getName()));
    		return true;
    	} catch (Exception e) {}
    	return false;
    }
}