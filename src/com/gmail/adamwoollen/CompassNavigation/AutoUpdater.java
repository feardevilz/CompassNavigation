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
	
	public String currentVersion;
  
  	public AutoUpdater(CompassNavigation plugin) {
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
  			Object object = jsonParser.parse(streamReader);
  			JSONObject jsonObject = (JSONObject) object;
  			String newVersion = (String) jsonObject.get("version");
  			if (!currentVersion.equals(newVersion)) {
  				streamReader.close();
  				player.sendMessage("§b[CompassNavigation] §9New version available!");
  				player.sendMessage("§9See §3" + jsonObject.get("link") + " §9for more information.");
  			}
  		} catch (Exception e) {}
  	}
  
  	@EventHandler
  	public void onPlayerJoin(PlayerJoinEvent event) {
  		Player player = (Player) event.getPlayer();
  		
  		if (player.hasPermission("compassnav.update")) {
  			checkUpdates(player);
  		}
  	}
}