package com.gmail.adamwoollen.CompassNavigation;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EventListener implements Listener {
	
	public CompassNavigation plugin;
    public Inventory chest;
    public List<String> lore = new CopyOnWriteArrayList<String>();
    public String title = "CompassNavigation";
    public VaultHandler vault;
    
    public EventListener(CompassNavigation plugin) {
    	this.plugin = plugin;
    	this.vault = new VaultHandler(plugin);
    }
    
    public boolean sectionExists(int slot, String path) {
    	if (plugin.getConfig().contains(slot + path)) {
    		return true;
    	}
    	return false;
    }
    
    public int getID(String id) {
    	String[] split = id.split(":");
    	return Integer.parseInt(split[0]);
    }
    
    public short getDamage(String id) {
    	String[] split = id.split(":");
    	if (split.length >= 2) {
    		return Short.parseShort(split[1]);
    	}
    	return (short) 0;
    }
	
    public void handleSlot(Player player, int slot, Inventory chest) {
    	if (plugin.getConfig().getBoolean(slot + ".Enabled", false)) {
    		lore.clear();
    		String Name = null;
    		
    		if (sectionExists(slot, ".Name")) {
    			Name = "§r" + ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(slot + ".Name"));
    		}
    		
    		String Item = plugin.getConfig().getString(slot + ".Item", "2");
    		int Amount = plugin.getConfig().getInt(slot + ".Amount", 1);
    		int ID = getID(Item);
    		short Damage = getDamage(Item);
    		
			for (String loreLine : plugin.getConfig().getStringList(slot + ".Lore")) {
				lore.add(ChatColor.translateAlternateColorCodes('&', loreLine));
			}
		
			ItemStack stack = new ItemStack(ID, Amount, Damage);
			
			if ((plugin.getConfig().getBoolean(slot + ".Enchanted", false)) && (plugin.getServer().getPluginManager().isPluginEnabled("ProtocolLib"))) {
				stack.addUnsafeEnchantment(Enchantment.WATER_WORKER, 1337);
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
			
			if (!(player.hasPermission("compassnav.use")) && ((!player.hasPermission("compassnav.deny.slot." + slot) || player.hasPermission("compassnav.*")))) {
				lore.add("§4No permission");
			}
			
			chest.setItem(slot - 1, setName(stack, Name, lore));
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
						if (vault.hasEnough(name, price)) {
							vault.subtract(name, price);
							plugin.send(player, plugin.prefix + "§6Charged §a$" + Double.toString(price) + " §6from you!");
							checkBungee(player, slot);
						} else {
							plugin.send(player, plugin.prefix + "§6You do not have enough money!");
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
			try {
				ByteArrayOutputStream b = new ByteArrayOutputStream();
				DataOutputStream out = new DataOutputStream(b);
				out.writeUTF("Connect");
				out.writeUTF(plugin.getConfig().getString(slot + ".Bungee"));

				player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
			} catch (Exception e) {
				checkLilypad(player, slot);
			}
		} else {
			checkLilypad(player, slot);
		}
    }
    
    public void checkLilypad(Player player, int slot) {
    	if (sectionExists(slot, ".Lilypad")) {
    		try {
	    		Class<?> connectClass = Class.forName("lilypad.client.connect.api.Connect");
	    		Class<?> requestClass = Class.forName("lilypad.client.connect.api.request.impl.RedirectRequest");
	    		Constructor<?> constructor = requestClass.getConstructor(new Class[] { String.class, String.class });
	    		Object request = constructor.newInstance(new Object[] { plugin.getConfig().getString(slot + ".Lilypad"), player.getName() });
	    		Method connection = connectClass.getDeclaredMethod("request", requestClass);
	    		
	    		Object connect = (Object) plugin.getServer().getServicesManager().getRegistration(connectClass).getProvider();
	    		connection.invoke(connect, request);
    		} catch (Exception e) {
    			checkWarp(player, slot);
    		}
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
    	
    	title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("GUIName"));
		Inventory chest = Bukkit.createInventory(null, (plugin.getConfig().getInt("Rows") * 9), title);
		
		for (int slot = 0; slot < 9; slot++) {
			handleSlot(player, slot, chest);
		}
	
		if (plugin.getConfig().getInt("Rows") >= 2) {	
			for (int slot = 9; slot < 18; slot++) {
				handleSlot(player, slot, chest);
			}
		}
		
		if (plugin.getConfig().getInt("Rows") >= 3) {	
			for (int slot = 18; slot < 27; slot++) {
				handleSlot(player, slot, chest);
			}
		}
	
		if (plugin.getConfig().getInt("Rows") >= 4) {	
			for (int slot = 27; slot < 36; slot++) {
				handleSlot(player, slot, chest);
			}
		}
		
		if (plugin.getConfig().getInt("Rows") >= 5) {	
			for (int slot = 36; slot < 45; slot++) {
				handleSlot(player, slot, chest);
			}
		}
		
		if (plugin.getConfig().getInt("Rows") >= 6) {	
			for (int slot = 45; slot < 54; slot++) {
				handleSlot(player, slot, chest);
			}
		}
		
		player.openInventory(chest);
    }
    
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
		    Player player = event.getPlayer();
		    if ((player.getItemInHand().getTypeId() == getID(plugin.getConfig().getString("Item")) && ((short) player.getItemInHand().getData().getData() == getDamage(plugin.getConfig().getString("Item"))))) {
		    	if (player.hasPermission("compassnav.use")) {
					if (plugin.getConfig().getList("DisabledWorlds").contains(player.getWorld().getName()) && (!player.hasPermission("compassnav.perks.use.world"))) {
		    			plugin.send(player, plugin.prefix + "§6You can't teleport from this world!");
		    		} else if (!plugin.canUseCompassHere(player.getLocation()) && (!player.hasPermission("compassnav.perks.use.region"))) {
		    			plugin.send(player, plugin.prefix + "§6You can't teleport in this region!");
		    		} else {
		    			openInventory(player);
		    			event.setCancelled(true);
		    		}
		    	}
		    }
		}
	}
	
	@EventHandler
	public void onInventoryInteract(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		if (event.getInventory().getTitle().equals(title)) {
			for (int slot = 0; slot < 54; slot++) {
				if (event.getRawSlot() == slot - 1) {
					if (sectionExists(slot, ".Enabled")) {
						if (plugin.getConfig().getBoolean(slot + ".Enabled")) {
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
							event.setCancelled(true);
						}
					}
				}
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
				    			plugin.send(player, plugin.prefix + "§6You can't teleport from this world!");
				    		} else if (!plugin.canUseCompassHere(player.getLocation()) && (!player.hasPermission("compassnav.perks.use.region"))) {
				    			plugin.send(player, plugin.prefix + "§6You can't teleport in this region!");
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
					plugin.send(event.getPlayer(), plugin.prefix + "§6Succesfully created a Teleport sign!");
				} else {
					event.setLine(0, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("NoPermSignName")));
					plugin.send(event.getPlayer(), plugin.prefix + "§6You do not have permission to create a Teleport sign.");
				}
			}
		} catch (Exception e) {}
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
