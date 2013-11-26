package com.gmail.adamwoollen.CompassNavigation;

import lilypad.client.connect.api.Connect;
import lilypad.client.connect.api.request.impl.RedirectRequest;
import lilypad.client.connect.api.result.FutureResultListener;
import lilypad.client.connect.api.result.StatusCode;
import lilypad.client.connect.api.result.impl.RedirectResult;

import org.bukkit.entity.Player;

public class LilypadHandler {

	public CompassNavigation plugin;
	public String whoami;
	public boolean result = false;
	
	public LilypadHandler(CompassNavigation plugin) {
		this.plugin = plugin;
	}
	
	// there's a flaw in lilypad, check if we're connected first -feardevilz
	public boolean connected(String server) {
		result = false;
		
		Connect connect = (Connect) plugin.getServer().getServicesManager().getRegistration(Connect.class).getProvider();
		whoami = connect.getSettings().getUsername();
		if(whoami.equalsIgnoreCase(server)) {
			result = true;
		}
		return result;
	}

	public boolean connect(Player player, String server) {
		result = false;
		
		try {
			Connect connect = (Connect) plugin.getServer().getServicesManager().getRegistration(Connect.class).getProvider();
			connect.request(new RedirectRequest(server, player.getName())).registerListener(new FutureResultListener<RedirectResult>() {
				public void onResult(RedirectResult redirectResult) {
					if (redirectResult.getStatusCode() == StatusCode.SUCCESS) {
						result = true;
					}
				}
			});
	    } catch (Exception e) {}
		
		return result;
	}
}