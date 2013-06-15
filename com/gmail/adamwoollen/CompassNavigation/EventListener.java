package com.gmail.adamwoollen.CompassNavigation;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EventListener implements Listener{
	
	public CompassNavigation plugin;
    public Inventory chest;
    public List<String> Ls = new ArrayList<String>();
    
    public EventListener(CompassNavigation p) {
    	plugin = p;
    }
    
    public boolean sectionExists(int slot, String path){
    	if (plugin.getConfig().contains(slot + path)) {
    		return true;
    	}
    	return false;
    }
	
    public void handleSlot(Player player, int slot, Inventory chest) {
    	if (this.sectionExists(slot, ".Enabled")) {
    		if(plugin.getConfig().getString(slot + ".Enabled") == "true") {
    			Ls.clear();
    			String Name = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(slot + ".Name"));
    			String Item = plugin.getConfig().getString(slot + ".Item");
    			short Damage = 0;
    			String[] Meta = Item.split(":");
    			int ID = Integer.parseInt(Meta[0]);
    			if (Meta.length == 2) {
    				Damage = Short.parseShort(Meta[1]);
    			}
				if (this.sectionExists(slot, ".Desc")) {
					Ls.add(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(slot + ".Desc")));
				}
				if (!player.hasPermission("compassnav.perks.free." + slot)) {
					if (plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
						if (this.sectionExists(slot, ".Price")) {
							Ls.add("§2Price: §a$" + plugin.getConfig().getDouble(slot + ".Price"));
						}
					}
				}
				if (ID == 0) {
					ID = 36;
					Damage = 0;
				}
				if (player.hasPermission("compassnav.slot." + slot)) {
					chest.setItem(slot - 1, setName(new ItemStack(ID, 1, Damage), Name, Ls));
				} else {
					Ls.add("§4No permission");
					chest.setItem(slot - 1, setName(new ItemStack(36, 1), Name, Ls));
				}
    		}
    	}
    }
    
    public void checkMoney(Player player, int slot) {
		if (this.sectionExists(slot, ".Price")) {
			Double amount = plugin.getConfig().getDouble(slot + ".Price");
			String name = player.getName();
			if (plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
				if (!player.hasPermission("compassnav.perks.free." + slot)) {
					if (plugin.getVault().hasEnough(name, amount)) {
						plugin.getVault().subtract(name, amount);
						player.sendMessage(plugin.prefix + "§6Charged §a$" + Double.toString(amount) + " §6from you!");
						this.checkBungee(player, slot);
					} else {
						player.sendMessage(plugin.prefix + "§6You do not have enough money!");
						player.closeInventory();
					}
				}
			} else {
				this.checkBungee(player, slot);
			}
		} else {
			this.checkBungee(player, slot);
		}
    }
    
    public void checkBungee(Player player, int slot) {
		if (sectionExists(slot, ".Bungee")) {
		     Bukkit.getMessenger().registerOutgoingPluginChannel(this.plugin, "BungeeCord");
             
		     ByteArrayOutputStream b = new ByteArrayOutputStream();
		     DataOutputStream out = new DataOutputStream(b);
		                      
		     try {
		           out.writeUTF("Connect");
		           out.writeUTF(plugin.getConfig().getString(slot + ".Bungee"));
		      } catch (IOException ex) {}
		     player.sendPluginMessage(this.plugin, "BungeeCord", b.toByteArray());
		} else {
			this.checkWarp(player, slot);
		}
    }
    
    public void checkWarp(Player player, int slot) {
    	if (sectionExists(slot, ".Warp")) {
    		if (plugin.getServer().getPluginManager().isPluginEnabled("Essentials")) {
    			Bukkit.dispatchCommand(player, "warp " + plugin.getConfig().getString(slot + ".Warp"));
    			player.closeInventory();
    		} else {
    			plugin.getServer().getLogger().severe("[CN] Essentials not found. Using coordinates from the configuration for slot " + slot + ".");
    			this.checkCoords(player, slot);
    		}
    	} else {
    		this.checkCoords(player, slot);
    	}
    }
    
    public void checkCoords(Player player, int slot) {
    	if (sectionExists(slot, ".X")) {
    		Location location = player.getLocation();
    		location.setWorld(Bukkit.getServer().getWorld(plugin.getConfig().getString(slot + ".World")));
    		location.setX(plugin.getConfig().getDouble(slot + ".X"));
    		location.setY(plugin.getConfig().getDouble(slot + ".Y"));
    		location.setZ(plugin.getConfig().getDouble(slot + ".Z"));
    		location.setYaw(plugin.getConfig().getInt(slot + ".Yaw"));
    		location.setPitch(plugin.getConfig().getInt(slot + ".Pitch"));
			player.teleport(location);
			player.closeInventory();
    	} else {
    		plugin.getServer().getLogger().severe("[CN] Could not find a valid destination for slot " + slot +"!");
    		player.sendMessage(plugin.prefix + "§6Could not find a valid destination.");
    		player.closeInventory();
    	}
    }
    
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e){
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
		    Player player = e.getPlayer();
		    if (player.getItemInHand().getTypeId() == plugin.getConfig().getInt("Item")) {
		    	if (player.hasPermission("compassnav.use")) {
					if (plugin.getConfig().getList("DisabledWorlds").contains(player.getWorld().getName()) && (!player.hasPermission("compassnav.perks.use.world"))) {
		    			player.sendMessage(plugin.prefix + "§6You can't teleport from this world.");
		    		} else if (!plugin.canUseCompassHere(player.getLocation()) && (!player.hasPermission("compassnav.perks.use.region"))) {
		    			player.sendMessage(plugin.prefix + "§6You can't teleport in this region!");
		    		} else {
		    			Inventory chest = Bukkit.createInventory(null, (plugin.getConfig().getInt("Rows") * 9), plugin.getConfig().getString("GUIName"));
		    		
		    			int[] row1 = {1,2,3,4,5,6,7,8,9};
			   			int[] row2 = {10,11,12,13,14,15,16,17,18};
			   			int[] row3 = {19,20,21,22,23,24,25,26,27};
			   			int[] row4 = {28,29,30,31,32,33,34,35,36};
			   			int[] row5 = {37,38,39,40,41,42,43,44,45};
			   			int[] row6 = {46,47,48,49,50,51,52,53,54};
	    				
			   			for (int slot : row1) {
			   				this.handleSlot(player, slot, chest);
			   			}
	    			
		    			if (plugin.getConfig().getInt("Rows") >= 2) {	
		    				for (int slot : row2) {
		    					this.handleSlot(player, slot, chest);
		    				}
		    			}
		    			
		    			if (plugin.getConfig().getInt("Rows") >= 3) {	
		    				for (int slot : row3) {
		    					this.handleSlot(player, slot, chest);
		    				}
		    			}
		    		
		    			if (plugin.getConfig().getInt("Rows") >= 4) {	
		    				for (int slot : row4) {
		    					this.handleSlot(player, slot, chest);
		    				}
		    			}
		   			
		    			if (plugin.getConfig().getInt("Rows") >= 5) {	
		    				for (int slot : row5) {
		    					this.handleSlot(player, slot, chest);
		    				}
		    			}
		    			
		    			if (plugin.getConfig().getInt("Rows") >= 6) {	
		    				for (int slot : row6) {
		    					this.handleSlot(player, slot, chest);
		    				}
		    			}
		    			
		    			player.openInventory(chest);
		    			e.setCancelled(true);
		    		}
		    	}
		    }
		}
	}
	
	@EventHandler
	public void onInventoryInteract(InventoryClickEvent e){
		Player player = (Player) e.getWhoClicked();
		if (player.getItemInHand().getTypeId() == plugin.getConfig().getInt("Item")) {
			if (e.getInventory().getTitle() == plugin.getConfig().getString("GUIName")) {
				if (e.getSlotType() == SlotType.CONTAINER) {
					if (e.isLeftClick()) {
						if (e.isShiftClick() == false) {
							int[] rows = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54};
							for (int slot : rows) {
								if (e.getRawSlot() == slot - 1) {
									if (this.sectionExists(slot, ".Enabled")) {
										if (plugin.getConfig().getString(slot + ".Enabled") == "true") {
											if (player.hasPermission("compassnav.slot." + slot)) {
												this.checkMoney(player, slot);
											}
										}
									}
								}
							}
						}
					}
				}
			e.setCancelled(true);
			}
		}
	}
	
	private ItemStack setName(ItemStack is, String name, List<String> lore){
		ItemMeta IM = is.getItemMeta();
		if (name != null) {
			IM.setDisplayName(name);
		}
		if (lore != null) {
			IM.setLore(lore);
		}
		is.setItemMeta(IM);
		return is;
	}
	
}
