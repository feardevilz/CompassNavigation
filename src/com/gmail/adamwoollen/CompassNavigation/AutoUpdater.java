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
  
  	public AutoUpdater(CompassNavigation plugin) {
  		this.plugin = plugin;
  		
		currentVersion = plugin.getDescription().getVersion();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
  	}
  
  	public void checkUpdates(Player player) {
  		try {
  			URL url = new URL("http://api.bukget.org/api2/bukkit/plugin/compass-navigation/latest");
  			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
  			connection.connect();
  			InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
  			JSONParser jsonParser = new JSONParser();
  			JSONObject jsonObject = (JSONObject) jsonParser.parse(streamReader);
  			String newVersion = (String) jsonObject.get("version");
  			if (!currentVersion.equals(newVersion)) {
  				streamReader.close();
  				player.sendMessage("§b[CompassNavigation] §rCompassNavigation v" + newVersion + " is now available!");
  				player.sendMessage("Your version: v" + currentVersion);
  			}
  		} catch (Exception e) {
  			plugin.getLogger().info(plugin.consolePrefix + "Couldn't run AutoUpdater for player " + player.getName() + ".");
  		}
  	}
  
  	@EventHandler
  	public void onPlayerJoin(PlayerJoinEvent event) {
  		Player player = (Player) event.getPlayer();
  		
  		if (player.hasPermission("compassnav.admin.update")) {
  			checkUpdates(player);
  		}
  	}
}