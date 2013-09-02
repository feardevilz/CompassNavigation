package com.gmail.adamwoollen.CompassNavigation;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

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
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

@SuppressWarnings("all")
public class EventListener implements Listener {
	
	public CompassNavigation plugin;
    public String title = "CompassNavigation";
    public VaultHandler vault;
    public HashMap<String, WarmupTimer> timers = new HashMap<String, WarmupTimer>();
    
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
	
    public Inventory handleSlot(Player player, int slot, Inventory chest) {
    	if (plugin.getConfig().getBoolean(slot + ".Enabled", false)) {
    		ArrayList<String> lore = new ArrayList<String>();
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
			
			if (plugin.getConfig().getBoolean(slot + ".Enchanted", false) && plugin.getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
				stack.addUnsafeEnchantment(Enchantment.WATER_WORKER, 4);
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
			
			if (!player.hasPermission("compassnav.use") || player.hasPermission(new Permission("compassnav.deny.slot." + slot, PermissionDefault.FALSE))) {
				lore.add("§4No permission");
			}
			
			chest.setItem(slot - 1, setName(stack, Name, lore));
    	}
    	return chest;
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
    			plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), handleModifiers(plugin.getConfig().getString(slot + ".Command").substring(2), player));
    		} else {
    			plugin.getServer().dispatchCommand(player, handleModifiers(plugin.getConfig().getString(slot + ".Command"), player));
    		}
    	}
		if (sectionExists(slot, ".Bungee")) {
			try {
				plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
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
    
    public void checkPlayers(Player player, int slot) {
    	if (timers.containsKey(player.getName())) {
    		timers.get(player.getName()).cancel();
    	}
    	if (plugin.getConfig().getBoolean("WarmupTime") && !player.hasPermission("compassnav.perks.cooldownprevent")) {
    		boolean delay = false;
    		
	    	for (Player p : plugin.getServer().getOnlinePlayers()) {
	    		if (p.getName() != player.getName()) {
		    		if (p.getLocation().distance(player.getLocation()) < plugin.getConfig().getInt("WarmupDistance")) {
		    			delay = true;
		    			break;
		    		}
	    		}
	    	}
	    	
	    	if (delay) {
	    		timers.put(player.getName(), new WarmupTimer(plugin, this, player, slot));
	    		timers.get(player.getName()).runTaskLater(plugin, 20L * plugin.getConfig().getInt("WarmupDelay"));
	    		plugin.send(player, "§6Teleporting you in " + plugin.getConfig().getInt("WarmupDelay") + " seconds, please do not move!");
	    	} else {
	    		checkMoney(player, slot);
	    	}
    	} else {
    		checkMoney(player, slot);
    	}
    }
    
    public void checkWarp(Player player, int slot) {
    	if (sectionExists(slot, ".Warp")) {
    		if (plugin.essentialsHandler != null) {
	    		Location loc = plugin.essentialsHandler.getWarp(player, plugin.getConfig().getString(slot + ".Warp"));
	    		if (loc != null) {
	    			player.teleport(loc);
	    		} else {
	    			checkCoords(player, slot);
	    		}
    		} else {
    			checkCoords(player, slot);
    		}
    	} else {
    		checkCoords(player, slot);
    	}
    }
    
    public void checkCoords(Player player, int slot) {
    	if (sectionExists(slot, ".X")) {
    		player.teleport(new Location(plugin.getServer().getWorld(plugin.getConfig().getString(slot + ".World")), plugin.getConfig().getDouble(slot + ".X"), plugin.getConfig().getDouble(slot + ".Y"), plugin.getConfig().getDouble(slot + ".Z"), plugin.getConfig().getInt(slot + ".Yaw"), plugin.getConfig().getInt(slot + ".Pitch")));
    	}
    	player.closeInventory();
    }
    
    public void openInventory(Player player) {
    	if (plugin.getConfig().getBoolean("Sounds")) {
    		player.playSound(player.getLocation(), Sound.CHEST_OPEN, 1.0F, 1.0F);
    	}
    	
    	title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("GUIName"));
		Inventory chest = plugin.getServer().createInventory(null, (plugin.getConfig().getInt("Rows") * 9), title);
		
		for (int slot = 0; slot < chest.getSize(); slot++) {
			chest = handleSlot(player, slot, chest);
		}
		
		player.openInventory(chest);
    }
    
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
		    Player player = event.getPlayer();
		    if (player.getItemInHand().getTypeId() == getID(plugin.getConfig().getString("Item")) && ((short) player.getItemInHand().getData().getData()) == getDamage(plugin.getConfig().getString("Item"))) {
		    	if (player.hasPermission("compassnav.use")) {
					if (plugin.getConfig().getList("DisabledWorlds").contains(player.getWorld().getName()) && !player.hasPermission("compassnav.perks.use.world")) {
		    			plugin.send(player, plugin.prefix + "§6You can't teleport from this world!");
		    		} else if (!plugin.canUseCompassHere(player.getLocation()) && !player.hasPermission("compassnav.perks.use.region")) {
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
	public void onInventoryClose(InventoryCloseEvent event) {
		if (event.getPlayer() instanceof Player) {
			final Player player = (Player) event.getPlayer();
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					player.updateInventory();
				}
			}, 5L);
		}
	}
	
	@EventHandler
	public void onInventoryInteract(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			Player player = (Player) event.getWhoClicked();
			 if (player.getItemInHand().getTypeId() == getID(plugin.getConfig().getString("Item")) && ((short) player.getItemInHand().getData().getData()) == getDamage(plugin.getConfig().getString("Item"))) {
				if (event.getInventory().getTitle().equals(title)) {
					event.setCancelled(true);
					int slot = event.getRawSlot() + 1;
					if (sectionExists(slot, ".Enabled")) {
						if (plugin.getConfig().getBoolean(slot + ".Enabled")) {
							if (player.hasPermission("compassnav.use") && !player.hasPermission(new Permission("compassnav.deny.slot." + slot, PermissionDefault.FALSE))) {
								if (plugin.getConfig().getBoolean("Sounds")) {
									player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0F, 1.0F);
								}
								checkPlayers(player, slot);
							} else if (plugin.getConfig().getBoolean("Sounds")) {
								player.playSound(player.getLocation(), Sound.ZOMBIE_IDLE, 1.0F, 1.0F);
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onSignInteract(PlayerInteractEvent event) {
		if (event.hasBlock()) {
			Block block = event.getClickedBlock();
			if (event.getPlayer().hasPermission("compassnav.sign.use")) {
				if (block.getTypeId() == 63 || block.getTypeId() == 68) {
					if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
						if (block.getState() instanceof Sign) {
							Sign sign = (Sign) block.getState();
							String line = sign.getLine(0);
							if (line != "" && line != null) {
						        if (line.equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("SignName")))) {
						        	Player player = (Player) event.getPlayer();
									if (plugin.getConfig().getList("DisabledWorlds").contains(player.getWorld().getName()) && !player.hasPermission("compassnav.perks.use.world")) {
						    			plugin.send(player, plugin.prefix + "§6You can't teleport from this world!");
						    		} else if (!plugin.canUseCompassHere(player.getLocation()) && !player.hasPermission("compassnav.perks.use.region")) {
						    			plugin.send(player, plugin.prefix + "§6You can't teleport in this region!");
						    		} else {
						    			openInventory(player);
						    		}
									event.setCancelled(true);
						        }
							}
						}
					}
				}
			}
		} 
	}
	  
	@EventHandler
	public void onSignCreate(SignChangeEvent event) {
		String line = event.getLine(0);
		if (line != "" && line != null) {
			if (line.equals(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("SignName")))) || line.equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("SignName")))) {
				if (event.getPlayer().hasPermission("compassnav.admin.sign.create")) {
					event.setLine(0, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("SignName")));
					plugin.send(event.getPlayer(), plugin.prefix + "§6Succesfully created a Teleport sign!");
				} else {
					event.setLine(0, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("NoPermSignName")));
					plugin.send(event.getPlayer(), plugin.prefix + "§6You do not have permission to create a Teleport sign.");
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		String cmd = event.getMessage().substring(1);
		if (cmd.equalsIgnoreCase(plugin.getConfig().getString("CommandName"))) {
			Player player = event.getPlayer();
			if (player.hasPermission("compassnav.use")) {
				if (plugin.getConfig().getStringList("DisabledWorlds").contains(player.getWorld().getName()) && !player.hasPermission("compassnav.perks.use.world")) {
					plugin.send(player, plugin.prefix + "§6You can't teleport from this world!");
				} else if (!plugin.canUseCompassHere(player.getLocation()) && !player.hasPermission("compassnav.perks.use.region")) {
					plugin.send(player, plugin.prefix + "§6You can't teleport in this region!");
				} else {
					openInventory(player);
				}
			} else {
				plugin.send(player, "§4You do not have access to that command.");
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (timers.containsKey(player.getName())) {
			if (event.getTo().getX() != event.getFrom().getX() || event.getTo().getY() != event.getFrom().getY() || event.getTo().getZ() != event.getFrom().getZ()) {
				timers.get(player.getName()).cancel();
				timers.remove(player.getName());
				plugin.send(player, "§6Teleportation cancelled.");
			}
		}
	}
	
	public ItemStack setName(ItemStack item, String name, ArrayList<String> lore) {
		ItemMeta itemMeta = item.getItemMeta();
		if (name != null) {
			itemMeta.setDisplayName(name);
		}
		if (lore != null) {
			itemMeta.setLore(lore);
		}
		item.setItemMeta(itemMeta);
		return item;
	}
	
}
