package com.gmail.adamwoollen.CompassNavigation;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CompassNavigation extends JavaPlugin {
	
	public String prefix = "";
	public String consolePrefix = "";
	public WorldGuardHandler worldGuardHandler;
	public ProtocolLibHandler protocolLibHandler;
	public EventListener eventListener;
	public String slot = "0";
	
	public void onEnable() {
		getConfig().options().copyDefaults(true);
        
        migrateToList();
        migrateFromDesc();
        
		saveConfig();
        
        if (getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
        	protocolLibHandler = new ProtocolLibHandler(this);
        	protocolLibHandler.initializeListener();
        }
        
        if (getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
        	worldGuardHandler = new WorldGuardHandler(this);
        }
        
        try {
        	if ((getConfig().getBoolean("AutoUpdater")) && (!getDescription().getVersion().contains("SNAPSHOT"))) {
        		new AutoUpdater(this);
        	}
		} catch (Exception e) {}
        
        eventListener = new EventListener(this);

		getServer().getPluginManager().registerEvents(eventListener, this);
		
		if ((getConfig().getString("Prefix") != "") && (getConfig().getString("Prefix") != null)) { 
			prefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Prefix") + " ");
			consolePrefix = ChatColor.stripColor(prefix);
		}
	}
	
	public void migrateToList() {
		for (int number = 0; number < 54; number++) {
			if (getConfig().contains(number + ".Desc")) {
				if (getConfig().isString(number + ".Desc")) {
					List<String> newLore = new CopyOnWriteArrayList<String>();
					newLore.add(getConfig().getString(number + ".Desc"));
					getConfig().set(number + ".Lore", newLore);
					getConfig().set(number + ".Desc", null);
				}
			}
		}
	}
	
	public void migrateFromDesc() {
		for (int number = 0; number < 54; number++) {
			if (getConfig().contains(number + ".Desc")) {
				List<String> newLore = getConfig().getStringList(number + ".Desc");
				getConfig().set(number + ".Lore", newLore);
				getConfig().set(number + ".Desc", null);
			}
		}
	}
	
	public void sendHelpMessage(CommandSender sender) {
		if (sender instanceof Player) {
			sender.sendMessage("§6§lCOMPASSNAV§f | §7/compassnav help");
			sender.sendMessage("§6Oo-----------------------oOo-----------------------oO");
			sender.sendMessage("§2/compassnav help§a - Get command help");
			sender.sendMessage("§2/compassnav reload§a - Reload the plugin");
			sender.sendMessage("§2/compassnav setup§a - Set up compass inventory slots");
			sender.sendMessage("§6Oo-----------------------oOo-----------------------oO");
		} else {
			getLogger().info("COMPASSNAV | /compassnav help");
			getLogger().info("Oo-----------------------oOo-----------------------oO");
			getLogger().info("/compassnav help - Get command help");
			getLogger().info("/compassnav reload - Reload the plugin");
			getLogger().info("/compassnav setup - Set up compass inventory slots");
			getLogger().info("Oo-----------------------oOo-----------------------oO");
		}
	}
		
	public void sendSetupMessage(CommandSender sender, String slot) {
		if (sender.hasPermission("compassnav.admin.setup")) {
			sender.sendMessage("§6§lSETUP§f | §7/compassnav setup");
			sender.sendMessage("§6Oo-----------------------oOo-----------------------oO");
			if (!slot.equals("0")) {
				sender.sendMessage("§aYou are now setting up slot " + slot + ".");
			}
			sender.sendMessage("§2/compassnav setup loc§a - Sets location");
			sender.sendMessage("§2/compassnav setup bungee <server>§a - Sets BungeeCord server");
			sender.sendMessage("§2/compassnav setup lilypad <server>§a - Sets Lilypad server");
			sender.sendMessage("§2/compassnav setup warp <warp>§a - Sets Essentials warp");
			sender.sendMessage("§2/compassnav setup item§a - Sets item from hand");
			sender.sendMessage("§2/compassnav setup name <name>§a - Sets item name");
			sender.sendMessage("§2/compassnav setup lore [number] <lore>§a - Sets item lore");
			sender.sendMessage("§2/compassnav setup price <price>§a - Sets the price of using the item");
			sender.sendMessage("§2/compassnav setup amount <amount>§a - Sets item amount");
			sender.sendMessage("§2/compassnav setup command <command>§a - Sets the executable command");
			sender.sendMessage("§2/compassnav setup enchant§a - Toggles enchanted status");
			sender.sendMessage("§2/compassnav setup enable§a - Enables slot");
			sender.sendMessage("§6Oo-----------------------oOo-----------------------oO");
		} else {
			sender.sendMessage("§4You do not have access to that command.");
		}
	}
	
	public String handleString(String[] args) {
		StringBuilder sb = new StringBuilder();
		for (int i = 2; i < args.length; i++) {
			sb.append(args[i]).append(" ");
		}
		return sb.toString().trim();
	}
	
	public boolean canUseCompassHere(Location location) {
		return (worldGuardHandler == null) ? true : worldGuardHandler.canUseCompassHere(location);
	}
	
    public boolean sectionExists(String slot, String path) {
    	if (getConfig().contains(slot + path)) {
    		return true;
    	}
    	return false;
    }
    
	public boolean isInteger(String str) {
	    try {
	        Integer.parseInt(str);
	    } catch (Exception e) {
	        return false;
	    }
		return true;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	if ((cmd.getName().equalsIgnoreCase("compassnav")) || (cmd.getName().equalsIgnoreCase("cn")) || (cmd.getName().equalsIgnoreCase("compassnavigation"))) {
    		if (args.length == 0) {
    			sendHelpMessage(sender);
    		} else if (args.length == 1) {
    			if (args[0].equalsIgnoreCase("reload")) {
    				if (sender instanceof Player) {
    					Player player = (Player) sender;
    					if (player.hasPermission("compassnav.admin.reload")) {
    						reloadConfig();
    						player.sendMessage(prefix + "§6CompassNavigation reloaded!");;
				    	} else {
				    		player.sendMessage("§4You do not have access to that command.");
				    	}
    				} else {
    					reloadConfig();
    					getLogger().info(consolePrefix + "CompassNavigation reloaded!");
    				}
    			} else if (args[0].equalsIgnoreCase("setup")) {
    				if (sender instanceof Player) {
    					Player player = (Player) sender;
    					if (player.hasPermission("compassnav.admin.setup")) {
    						player.sendMessage("§6§lSETUP§f | §7/compassnav setup");
    						player.sendMessage("§6Oo-----------------------oOo-----------------------oO");
							player.sendMessage("§aPlease specify a slot number.");
							player.sendMessage("§2Usage:§a /compassnav setup <1>");
							player.sendMessage("§6Oo-----------------------oOo-----------------------oO");
    					} else {
    						player.sendMessage("§4You do not have access to that command.");
    					}
    				} else {
    					getLogger().info(consolePrefix + "Sorry, but consoles can't execute this command.");
    				}
    			} else {
    				sendHelpMessage(sender);
    			}
			} else if (args.length == 2) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					if (args[0].equalsIgnoreCase("setup")) {
						if (player.hasPermission("compassnav.admin.setup")) {
							if (isInteger(args[1])) {
								if (Integer.parseInt(args[1]) <= 54) {
									slot = args[1];
									sendSetupMessage(player, slot);
								}
							} else if (!slot.equals("0")) {
								if (args[1].equalsIgnoreCase("loc")) {
									getConfig().set(slot + ".World", player.getWorld().getName());
									getConfig().set(slot + ".X", player.getLocation().getX());
									getConfig().set(slot + ".Y", player.getLocation().getY());
									getConfig().set(slot + ".Z", player.getLocation().getZ());
									getConfig().set(slot + ".Yaw", player.getLocation().getYaw());
									getConfig().set(slot + ".Pitch", player.getLocation().getPitch());
									player.sendMessage(prefix + "§6Location set for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("item")) {
									getConfig().set(slot + ".Item", player.getItemInHand().getTypeId() + ":" + player.getItemInHand().getDurability());
									player.sendMessage(prefix + "§6Item set for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("enable")) {
									if (getConfig().contains(slot + ".Enabled")) {
										if (getConfig().getBoolean(slot + ".Enabled") == true) {
											player.sendMessage(prefix + "§6Disabled slot " + slot + ".");
											getConfig().set(slot + ".Enabled", false);
										} else {
											player.sendMessage(prefix + "§6Enabled slot " + slot + ".");
											getConfig().set(slot + ".Enabled", true);
										}
									} else {
										player.sendMessage(prefix + "§6Enabled slot " + slot + ".");
										getConfig().set(slot + ".Enabled", true);
									}
								} else if (args[1].equalsIgnoreCase("bungee")) {
									getConfig().set(slot + ".Bungee", null);
									player.sendMessage(prefix + "§6Bungee unset for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("lilypad")) {
									getConfig().set(slot + ".Lilypad", null);
									player.sendMessage(prefix + "§6Lilypad unset for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("lore")) {
									getConfig().set(slot + ".Lore", null);
									player.sendMessage(prefix + "§6Lore unset for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("warp")) {
									getConfig().set(slot + ".Warp", null);
									player.sendMessage(prefix + "§6Warp unset for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("price")) {
									getConfig().set(slot + ".Price", null);
									player.sendMessage(prefix + "§6Price unset for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("amount")) {
									getConfig().set(slot + ".Amount", null);
									player.sendMessage(prefix + "§6Amount unset for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("enchant")) {
									if (getConfig().contains(slot + ".Enchant")) {
										if (getConfig().getBoolean(slot + ".Enchant") == true) {
											player.sendMessage(prefix + "§6Removed enchant from slot " + slot + ".");
											getConfig().set(slot + ".Enchant", null);
										} else {
											player.sendMessage(prefix + "§6Added enchant to slot " + slot + ".");
											getConfig().set(slot + ".Enchant", true);
										}
									} else {
										player.sendMessage(prefix + "§6Added enchant to slot " + slot + ".");
										getConfig().set(slot + ".Enchant", true);
									}
								} else if (args[1].equalsIgnoreCase("command")) {
									getConfig().set(slot + ".Command", null);
									player.sendMessage(prefix + "§6Command unset for slot " + slot + ".");
								} else {
									sendSetupMessage(player, slot);
								}
								saveConfig();
							} else {
								player.sendMessage(prefix + "§6You haven't specified a slot to modify.");
							}
						} else {
							player.sendMessage("§4You do not have access to that command.");
						}
					} else {
						sendHelpMessage((Player) sender);
					}
				} else {
					getLogger().info(consolePrefix + "Sorry, but consoles can't execute this command.");
				}
			} else if (args.length >= 3) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					if (player.hasPermission("compassnav.admin.setup")) {
						if (!slot.equals("0")) {
							if (args[0].equalsIgnoreCase("setup")) {
								if (args[1].equalsIgnoreCase("bungee")) {
									getConfig().set(slot + ".Bungee", handleString(args));
									player.sendMessage(prefix + "§6Bungee set for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("lilypad")) {
									getConfig().set(slot + ".Lilypad", handleString(args));
									player.sendMessage(prefix + "§6Lilypad set for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("name")) {
									getConfig().set(slot + ".Name", handleString(args));
									player.sendMessage(prefix + "§6Name set for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("lore")) {
									List<String> loreList = new CopyOnWriteArrayList<String>();
									if (args.length >= 4) {
										String lore = handleString(args);
										try {
											int number = Integer.parseInt(args[2]);
											lore = lore.split(" ", 2)[1];
											for (String secondLore : getConfig().getStringList(slot + ".Lore")) {
												loreList.add(secondLore);
											}
											loreList.set(number - 1, lore);
											getConfig().set(slot + ".Lore", loreList);
										} catch (Exception e) {
											String loreString = handleString(args);
											for (String secondLore : getConfig().getStringList(slot + ".Lore")) {
												loreList.add(secondLore);
											}
											loreList.add(loreString);
											getConfig().set(slot + ".Lore", loreList);
										}
									} else {
										String loreString = handleString(args);
										for (String secondLore : getConfig().getStringList(slot + ".Lore")) {
											loreList.add(secondLore);
										}
										loreList.add(loreString);
										getConfig().set(slot + ".Lore", loreList);
									}
									player.sendMessage(prefix + "§6Lore set for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("warp")) {
									getConfig().set(slot + ".Warp", handleString(args));
									player.sendMessage(prefix + "§6Warp set for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("price")) {
									getConfig().set(slot + ".Price", Double.parseDouble(handleString(args)));
									player.sendMessage(prefix + "§6Price set for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("amount")) {
									getConfig().set(slot + ".Amount", Integer.parseInt(handleString(args)));
									player.sendMessage(prefix + "§6Amount set for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("command")) {
									getConfig().set(slot + ".Command", handleString(args));
									player.sendMessage(prefix + "§6Command set for slot " + slot + "!");
								} else {
									sendSetupMessage(player, slot);
								}
								saveConfig();
							} else {
								sendHelpMessage(player);
							}
						} else {
							player.sendMessage(prefix + "§6You haven't specified a slot to modify.");
						}
					} else {
						player.sendMessage("§4You do not have access to that command.");
					}
				} else {
					getLogger().info(consolePrefix + "Sorry, but consoles can't execute this command.");
				}
			}
    	} else if (cmd.getName().equalsIgnoreCase(getConfig().getString("CommandName"))) {
    		if (sender instanceof Player) {
				Player player = (Player) sender;
    			if (player.hasPermission("compassnav.use")) {
    				if (getConfig().getList("DisabledWorlds").contains(player.getWorld().getName()) && (!player.hasPermission("compassnav.perks.use.world"))) {
    					player.sendMessage(prefix + "§6You can't teleport from this world!");
    				} else if (!canUseCompassHere(player.getLocation()) && (!player.hasPermission("compassnav.perks.use.region"))) {
    					player.sendMessage(prefix + "§6You can't teleport in this region!");
    				} else {
    					eventListener.openInventory(player);
    				}
    			} else {
    				player.sendMessage("§4You do not have access to that command.");
    			}
    		} else {
    			getLogger().info(consolePrefix + "Sorry, but consoles can't execute this command.");
    		}
    	} else {
    		sendHelpMessage(sender);
    	}
		return true;
	}
}