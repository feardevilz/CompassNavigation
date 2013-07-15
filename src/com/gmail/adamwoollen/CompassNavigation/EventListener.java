package com.gmail.adamwoollen.CompassNavigation;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EventListener implements Listener {
	
	public CompassNavigation plugin;
    public Inventory chest;
    public List<String> lore = new CopyOnWriteArrayList<String>();
    public ItemStack compassItem;
    public List<Player> giveCompassBack = new CopyOnWriteArrayList<Player>();
    
    public EventListener(CompassNavigation plugin) {
    	this.plugin = plugin;
    }
    
    public boolean sectionExists(int slot, String path) {
    	if (plugin.getConfig().contains(slot + path)) {
    		return true;
    	}
    	return false;
    }
	
    public void handleSlot(Player player, int slot, Inventory chest) {
    	if (sectionExists(slot, ".Enabled")) {
    		if (plugin.getConfig().getBoolean(slot + ".Enabled") == true) {
    			lore.clear();
    			String Name = null;
    			try {
    				Name = "§r" + ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(slot + ".Name"));
    			} catch (Exception e) {}
    			String Item = plugin.getConfig().getString(slot + ".Item");
    			short Damage = 0;
    			int Amount = 1;
    			int ID = Integer.parseInt(Item.split(":")[0]);
    			if (Item.split(":").length == 2) {
    				Damage = Short.parseShort(Item.split(":")[1]);
    			}
    			if (sectionExists(slot, ".Amount")) {
    				if ((plugin.getConfig().getInt(slot + ".Amount") <= 64) && (plugin.getConfig().getInt(slot + ".Amount") >= 1)) {
    					Amount = plugin.getConfig().getInt(slot + ".Amount");
    				}
    			}
				if (sectionExists(slot, ".Desc")) {
					for (String iLore : plugin.getConfig().getStringList(slot + ".Desc")) {
						lore.add(ChatColor.translateAlternateColorCodes('&', iLore));
					}
				}
				ItemStack stack = new ItemStack(ID, Amount, Damage);
				if (sectionExists(slot, ".Enchanted")) {
					if ((plugin.getConfig().getBoolean(slot + ".Enchanted") == true) && (plugin.getServer().getPluginManager().isPluginEnabled("ProtocolLib"))) {
						stack.addUnsafeEnchantment(Enchantment.WATER_WORKER, 1337);
					}
				}
				if (!player.hasPermission("compassnav.perks.free")) {
					if (!player.hasPermission("compassnav.perks.free." + slot)) {
						if (plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
							if (sectionExists(slot, ".Price")) {
								lore.add("§2Price: §a$" + plugin.getConfig().getDouble(slot + ".Price"));
							}
						}
					}
				}
				if ((player.hasPermission("compassnav.use")) && ((!player.hasPermission("compassnav.deny.slot." + slot) || player.isOp() || player.hasPermission("compassnav.admin")))) {
					chest.setItem(slot - 1, setName(stack, Name, lore));
				} else {
					lore.add("§4No permission");
					chest.setItem(slot - 1, setName(new ItemStack(36, 1), Name, lore));
				}
    		}
    	}
    }
    
    public String handleModifiers(String string, Player player) {
    	string = string.replace("<name>", player.getName());
    	string = string.replace("<displayname>", player.getDisplayName());
    	string = string.replace("<x>", Integer.toString(player.getLocation().getBlockX()));
    	string = string.replace("<y>", Integer.toString(player.getLocation().getBlockY()));
    	string = string.replace("<z>", Integer.toString(player.getLocation().getBlockZ()));
    	string = string.replace("<yaw>", Integer.toString((int) player.getLocation().getYaw()));
    	string = string.replace("<pitch>", Integer.toString((int) player.getLocation().getPitch()));
    	return string;
    }
    
    public void checkMoney(Player player, int slot) {
		if (sectionExists(slot, ".Price")) {
			Double price = plugin.getConfig().getDouble(slot + ".Price");
			String name = player.getName();
			if (plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
				if (!player.hasPermission("compassnav.perks.free")) {
					if (!player.hasPermission("compassnav.perks.free." + slot)) {
						if (plugin.getVault().hasEnough(name, price)) {
							plugin.getVault().subtract(name, price);
							player.sendMessage(plugin.prefix + "§6Charged §a$" + Double.toString(price) + " §6from you!");
							checkBungee(player, slot);
						} else {
							player.sendMessage(plugin.prefix + "§6You do not have enough money!");
							player.closeInventory();
						}
					} else {
						checkBungee(player, slot);
					}
				} else {
					checkBungee(player, slot);
				}
			} else {
				checkBungee(player, slot);
			}
		} else {
			checkBungee(player, slot);
		}
    }
    
    public void checkBungee(Player player, int slot) {
    	if (sectionExists(slot, ".Command")) {
    		if (plugin.getConfig().getString(slot + ".Command").startsWith("c:")) {
    			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), handleModifiers(plugin.getConfig().getString(slot + ".Command").substring(2), player));
    		} else {
    			Bukkit.dispatchCommand(player, handleModifiers(plugin.getConfig().getString(slot + ".Command"), player));
    		}
    	}
		if (sectionExists(slot, ".Bungee")) {
		     Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
             
		     ByteArrayOutputStream b = new ByteArrayOutputStream();
		     DataOutputStream out = new DataOutputStream(b);
		                      
		     try {
		    	 out.writeUTF("Connect");
		         out.writeUTF(plugin.getConfig().getString(slot + ".Bungee"));
		     } catch (Exception e) {}
		     player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
		} else {
			checkWarp(player, slot);
		}
    }
    
    public void checkWarp(Player player, int slot) {
    	if (sectionExists(slot, ".Warp")) {
    		if (plugin.getServer().getPluginManager().isPluginEnabled("Essentials")) {
    			Bukkit.dispatchCommand(player, "warp " + plugin.getConfig().getString(slot + ".Warp"));
    			player.closeInventory();
    		} else {
    			checkCoords(player, slot);
    		}
    	} else {
    		checkCoords(player, slot);
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
    	}
    	player.closeInventory();
    }
    
    public void openInventory(Player player) {
    	if (plugin.getConfig().getBoolean("Sounds")) {
    		player.playSound(player.getLocation(), Sound.CHEST_OPEN, 1.0F, 1.0F);
    	}
		Inventory chest = Bukkit.createInventory(null, (plugin.getConfig().getInt("Rows") * 9), plugin.getConfig().getString("GUIName"));
		
		int[] row1 = {1,2,3,4,5,6,7,8,9};
		int[] row2 = {10,11,12,13,14,15,16,17,18};
		int[] row3 = {19,20,21,22,23,24,25,26,27};
		int[] row4 = {28,29,30,31,32,33,34,35,36};
		int[] row5 = {37,38,39,40,41,42,43,44,45};
		int[] row6 = {46,47,48,49,50,51,52,53,54};
				
		for (int slot : row1) {
			handleSlot(player, slot, chest);
		}
	
		if (plugin.getConfig().getInt("Rows") >= 2) {	
			for (int slot : row2) {
				handleSlot(player, slot, chest);
			}
		}
		
		if (plugin.getConfig().getInt("Rows") >= 3) {	
			for (int slot : row3) {
				handleSlot(player, slot, chest);
			}
		}
	
		if (plugin.getConfig().getInt("Rows") >= 4) {	
			for (int slot : row4) {
				handleSlot(player, slot, chest);
			}
		}
		
		if (plugin.getConfig().getInt("Rows") >= 5) {	
			for (int slot : row5) {
				handleSlot(player, slot, chest);
			}
		}
		
		if (plugin.getConfig().getInt("Rows") >= 6) {	
			for (int slot : row6) {
				handleSlot(player, slot, chest);
			}
		}
		
		player.openInventory(chest);
    }
    
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
		    Player player = event.getPlayer();
		    if (player.getItemInHand().getTypeId() == plugin.getConfig().getInt("Item")) {
		    	if (player.hasPermission("compassnav.use")) {
					if (plugin.getConfig().getList("DisabledWorlds").contains(player.getWorld().getName()) && (!player.hasPermission("compassnav.perks.use.world"))) {
		    			player.sendMessage(plugin.prefix + "§6You can't teleport from this world!");
		    		} else if (!plugin.canUseCompassHere(player.getLocation()) && (!player.hasPermission("compassnav.perks.use.region"))) {
		    			player.sendMessage(plugin.prefix + "§6You can't teleport in this region!");
		    		} else {
		    			openInventory(player);
		    			event.setCancelled(true);
		    		}
		    	}
		    }
		}
	}
	
	@EventHandler
	public void onInventoryInteract(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		if (e.getInventory().getTitle() == plugin.getConfig().getString("GUIName")) {
			if (e.getSlotType() == SlotType.CONTAINER) {
				if (e.isLeftClick()) {
					if (e.isShiftClick() == false) {
						int[] rows = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54};
						for (int slot : rows) {
							if (e.getRawSlot() == slot - 1) {
								if (sectionExists(slot, ".Enabled")) {
									if (plugin.getConfig().getString(slot + ".Enabled") == "true") {
										if ((player.hasPermission("compassnav.use")) && ((!player.hasPermission("compassnav.deny.slot." + slot) || player.isOp() || player.hasPermission("compassnav.admin")))) {
											if (plugin.getConfig().getBoolean("Sounds")) {
												player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0F, 1.0F);
											}
											checkMoney(player, slot);
										} else {
											if (plugin.getConfig().getBoolean("Sounds")) {
												player.playSound(player.getLocation(), Sound.ZOMBIE_IDLE, 1.0F, 1.0F);
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
	
	@EventHandler
	public void onSignInteract(PlayerInteractEvent event) {
		try {
			Block block = event.getClickedBlock();
			if (event.getPlayer().hasPermission("compassnav.sign.use")) {
				if (block.getTypeId() == 63 || block.getTypeId() == 68) {
					if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
						Sign sign = (Sign) block.getState();
				        if (sign.getLine(0).equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("SignName")))) {
				        	Player player = (Player) event.getPlayer();
							if (plugin.getConfig().getList("DisabledWorlds").contains(player.getWorld().getName()) && (!player.hasPermission("compassnav.perks.use.world"))) {
				    			player.sendMessage(plugin.prefix + "§6You can't teleport from this world!");
				    		} else if (!plugin.canUseCompassHere(player.getLocation()) && (!player.hasPermission("compassnav.perks.use.region"))) {
				    			player.sendMessage(plugin.prefix + "§6You can't teleport in this region!");
				    		} else {
				    			openInventory(player);
				    		}
							event.setCancelled(true);
				        }
					}
				}
			}
		} catch (Exception e) {}
	}
	  
	@EventHandler
	public void onSignCreate(SignChangeEvent event) {
		try {
			String line = event.getLine(0);
			if (line.equals(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("SignName")))) || (line.equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("SignName"))))) {
				if (event.getPlayer().hasPermission("compassnav.admin.sign.create")) {
					event.setLine(0, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("SignName")));
					event.getPlayer().sendMessage(plugin.prefix + "§6Succesfully created a Teleport sign!");
				} else {
					event.setLine(0, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("NoPermSignName")));
					event.getPlayer().sendMessage(plugin.prefix + "§6You do not have permission to create a Teleport sign.");
				}
			}
		} catch (Exception e) {}
	}
	
	public void setCompassItem() {
		lore.clear();
		String Name = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("CompassName"));
		String Item = plugin.getConfig().getString("Item");
		short Damage = 0;
		int ID = Integer.parseInt(Item.split(":")[0]);
		if (Item.split(":").length == 2) {
			Damage = Short.parseShort(Item.split(":")[1]);
		}
		for (String iLore : plugin.getConfig().getStringList("CompassDesc")) {
			lore.add(ChatColor.translateAlternateColorCodes('&', iLore));
		}
		compassItem = setName(new ItemStack(ID, 1, Damage), Name, lore);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (plugin.getConfig().getStringList("MustHaveCompassWorlds").contains(player.getWorld().getName())) {
			if (!plugin.getConfig().getList("DisabledWorlds").contains(player.getWorld().getName()) || (player.hasPermission("compassnav.perks.use.world"))) {
    			if (plugin.canUseCompassHere(player.getLocation()) || (player.hasPermission("compassnav.perks.use.region"))) {
					if (!player.getInventory().contains(compassItem)) {
						player.getInventory().addItem(compassItem);
					}
    			}
			}
		}
		if (plugin.getConfig().getList("DisabledWorlds").contains(player.getWorld().getName()) && (!player.hasPermission("compassnav.perks.use.world"))) {
			if (player.getInventory().contains(compassItem)) {
				player.getInventory().remove(compassItem);
				giveCompassBack.add(player);
			}
		} else if (!plugin.canUseCompassHere(player.getLocation()) && (!player.hasPermission("compassnav.perks.use.region"))) {
			if (player.getInventory().contains(compassItem)) {
				player.getInventory().remove(compassItem);
				giveCompassBack.add(player);
			}
		} else if (giveCompassBack.contains(player)) {
			if (!player.getInventory().contains(compassItem)) {
				player.getInventory().addItem(compassItem);
				giveCompassBack.remove(player);
			}
		}
	}
	
	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		if (plugin.getConfig().getStringList("MustHaveCompassWorlds").contains(player.getWorld().getName())) {
			if (event.getItemDrop().getItemStack().getTypeId() == plugin.getConfig().getInt("Item")) {
				player.sendMessage(plugin.prefix + "§6You can't drop your compass here!");
				event.setCancelled(true);
			}
		}
	}
	
	public ItemStack setName(ItemStack items, String name, List<String> lore) {
		ItemMeta itemMeta = items.getItemMeta();
		if (name != null) {
			itemMeta.setDisplayName(name);
		}
		if (lore != null) {
			itemMeta.setLore(lore);
		}
		items.setItemMeta(itemMeta);
		return items;
	}
	
}
