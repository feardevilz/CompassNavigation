package com.gmail.adamwoollen.CompassNavigation;

import org.bukkit.entity.Player;

public class LilypadHandler {

	public CompassNavigation plugin;
	
	public LilypadHandler(CompassNavigation plugin) {
		this.plugin = plugin;
	}
    
    public boolean connect(Player player, int slot) {
    	try {
    		lilypad.client.connect.api.Connect connect = (lilypad.client.connect.api.Connect) plugin.getServer().getServicesManager().getRegistration(lilypad.client.connect.api.Connect.class).getProvider();
    		connect.request(new lilypad.client.connect.api.request.impl.RedirectRequest(plugin.getConfig().getString(slot + ".Lilypad"), player.getName()));
    		return true;
    	} catch (Exception e) {}
    	return false;
    }
}