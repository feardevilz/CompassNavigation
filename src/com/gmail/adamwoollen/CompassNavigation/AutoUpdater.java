package com.gmail.adamwoollen.CompassNavigation;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class AutoUpdater implements Listener {
	
	public CompassNavigation plugin;
	public String currentVersion;
	public String newVersion;
  
  	public AutoUpdater(CompassNavigation plugin) {
  		this.plugin = plugin;
		currentVersion = plugin.getDescription().getVersion();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
  	}
  
  	public boolean updatesAvailable(Player player) {
  		try {
  			URL url = new URL("http://api.bukget.org/api2/bukkit/plugin/compass-navigation/latest");
  			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
  			connection.connect();
  			InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
  			JSONParser jsonParser = new JSONParser();
  			JSONObject jsonObject = (JSONObject) jsonParser.parse(streamReader);
  			newVersion = (String) jsonObject.get("version");
  			
  			if (!currentVersion.equals(newVersion)) {
  				return true;
  			}
  			
  			streamReader.close();
  			connection.disconnect();
  		} catch (Exception e) {
  			plugin.getLogger().info(plugin.consolePrefix + "Couldn't run AutoUpdater for player " + player.getName() + ".");
  		}
  		return false;
  	}
  
  	@EventHandler
  	public void onPlayerJoin(PlayerJoinEvent event) {
  		Player player = event.getPlayer();
  		
  		if (player.hasPermission("compassnav.admin.update")) {
  			if (updatesAvailable(player)) {
  				player.sendMessage("§b[CompassNavigation] §rCompassNavigation v" + newVersion + " is now available!");
  				player.sendMessage("Your version: v" + currentVersion);
  			}
  		}
  	}
}