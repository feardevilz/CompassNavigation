package com.gmail.adamwoollen.CompassNavigation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class CompassNavigation extends JavaPlugin {
	
	public String prefix = "";
	public WorldGuardHandler worldGuardHandler;
	public ProtocolLibHandler protocolLibHandler; 
	public EventListener eventListener;
	public VaultHandler vaultHandler;
	public AutoUpdater autoUpdater;
	public EssentialsHandler essentialsHandler;
	public LilypadHandler lilypadHandler;
	public String slot = "0";
	
	public void onEnable() {
		getConfig().options().copyDefaults(true);
        
        migrateToList();
        migrateFromDesc();
        
		saveConfig();
        
        if (getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
        	protocolLibHandler = new ProtocolLibHandler(this);
        }
        
        if (getServer().getPluginManager().isPluginEnabled("Essentials")) {
        	essentialsHandler = new EssentialsHandler(this);
        }
        
        if (getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
        	worldGuardHandler = new WorldGuardHandler((WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard"));
        }
        
        if (getServer().getPluginManager().isPluginEnabled("Vault")) {
        	vaultHandler = new VaultHandler(this);
        }
        
        if (getServer().getPluginManager().isPluginEnabled("LilyPad-Connect")) {
        	lilypadHandler = new LilypadHandler(this);
        }
        
        if (getConfig().getBoolean("AutoUpdater", true) && !getDescription().getVersion().contains("SNAPSHOT")) {
        	autoUpdater = new AutoUpdater(this, getFile(), AutoUpdater.UpdateType.DEFAULT, true);
        }
        
        eventListener = new EventListener(this);

		getServer().getPluginManager().registerEvents(eventListener, this);
		
		if (getConfig().getString("Prefix") != null && getConfig().getString("Prefix") != "") { 
			prefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Prefix") + " ");
		}
	}
	
	public void migrateToList() {
		for (int number = 0; number < 54; number++) {
			if (getConfig().contains(number + ".Desc")) {
				if (getConfig().isString(number + ".Desc")) {
					getConfig().set(number + ".Lore", new ArrayList<String>(Arrays.asList(getConfig().getString(number + ".Desc"))));
					getConfig().set(number + ".Desc", null);
				}
			}
		}
	}
	
	public void migrateFromDesc() {
		for (int number = 0; number < 54; number++) {
			if (getConfig().contains(number + ".Desc")) {
				getConfig().set(number + ".Lore", getConfig().getStringList(number + ".Desc"));
				getConfig().set(number + ".Desc", null);
			}
		}
	}
	
	public void send(CommandSender sender, String message) {
		if (sender instanceof Player) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
		} else {
			getLogger().info(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', message)));
		}
	}
	
	public void sendHelpMessage(CommandSender sender) {
		send(sender, "§6§lCOMPASSNAV§f | §7/compassnav help");
		send(sender, "§6Oo-----------------------oOo-----------------------oO");
		send(sender, "§2/compassnav help§a - Get command help");
		send(sender, "§2/compassnav reload§a - Reload the plugin");
		send(sender, "§2/compassnav update§a - Download the latest update");
		send(sender, "§2/compassnav setup§a - Set up the compass inventory");
		send(sender, "§6Oo-----------------------oOo-----------------------oO");
	}
		
	public void sendSetupMessage(CommandSender sender, String slot) {
		if (sender.hasPermission("compassnav.admin.setup")) {
			send(sender, "§6§lSETUP§f | §7/compassnav setup");
			send(sender, "§6Oo-----------------------oOo-----------------------oO");
			if (!slot.equals("0")) {
				send(sender, "§aYou are now setting up slot " + slot + ".");
			}
			send(sender, "§2/compassnav setup loc§a - Sets location");
			send(sender, "§2/compassnav setup bungee <server>§a - Sets BungeeCord server");
			send(sender, "§2/compassnav setup lilypad <server>§a - Sets Lilypad server");
			send(sender, "§2/compassnav setup warp <warp>§a - Sets Essentials warp");
			send(sender, "§2/compassnav setup item§a - Sets item from hand");
			send(sender, "§2/compassnav setup name <name>§a - Sets item name");
			send(sender, "§2/compassnav setup lore [number] <lore>§a - Sets item lore");
			send(sender, "§2/compassnav setup price <price>§a - Sets the price of using the item");
			send(sender, "§2/compassnav setup amount <amount>§a - Sets item amount");
			send(sender, "§2/compassnav setup command <command>§a - Sets the executable command");
			send(sender, "§2/compassnav setup enchant§a - Toggles enchanted status");
			send(sender, "§2/compassnav setup enable§a - Enables slot");
			send(sender, "§6Oo-----------------------oOo-----------------------oO");
		} else {
			send(sender, "§4You do not have access to that command.");
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
    	if (cmd.getName().equalsIgnoreCase("compassnav") || cmd.getName().equalsIgnoreCase("cn") || cmd.getName().equalsIgnoreCase("compassnavigation")) {
    		if (args.length == 0) {
    			sendHelpMessage(sender);
    		} else if (args.length == 1) {
    			if (args[0].equalsIgnoreCase("reload")) {
    				if (sender.hasPermission("compassnav.admin.reload")) {
    					reloadConfig();
    					send(sender, prefix + "§6CompassNavigation reloaded!");;
				   	} else {
				   		send(sender, "§4You do not have access to that command.");
				   	}
    			} else if (args[0].equalsIgnoreCase("update")) {
    				if (sender.hasPermission("compassnav.admin.update")) {
	    				if (autoUpdater != null) {
		    				if (autoUpdater.thread != null && autoUpdater.thread.isAlive()) {
		    					send(sender, prefix + "§6There is already a download in progress!");
		    				} else {
		    					send(sender, prefix + "§6Downloading latest version...");
		    					autoUpdater.downloadLatestVersion();
		    					if (autoUpdater.result == AutoUpdater.UpdateResult.FAIL_DBO) {
		    						send(sender, prefix + "§6Couldn't contact DevBukkit. Is your server connected to the internet?");
		    					} else if (autoUpdater.result == AutoUpdater.UpdateResult.FAIL_DOWNLOAD) {
		    						send(sender, prefix + "§6Couldn't download the file. Please try again.");
		    					} else if (autoUpdater.result == AutoUpdater.UpdateResult.SUCCESS) {
		    						send(sender, prefix + "§6Successfully downloaded the latest version! Please restart your server for the changes to take effect.");
		    					} else {
		    						send(sender, prefix + "§6I'm not sure what happened. Please check the console.");
		    					}
	    					}
	    				} else {
	    					send(sender, prefix + "§6Couldn't download the latest update because the auto-updater is disabled.");
	    				}
    				} else {
    					send(sender, "§4You do not have access to that command.");
    				}
    			} else if (args[0].equalsIgnoreCase("setup")) {
    				if (sender instanceof Player) {
    					Player player = (Player) sender;
    					if (player.hasPermission("compassnav.admin.setup")) {
    						send(player, "§6§lSETUP§f | §7/compassnav setup");
    						send(player, "§6Oo-----------------------oOo-----------------------oO");
							send(player, "§aPlease specify a slot number.");
							send(player, "§2Usage:§a /compassnav setup <1>");
							send(player, "§6Oo-----------------------oOo-----------------------oO");
    					} else {
    						send(player, "§4You do not have access to that command.");
    					}
    				} else {
    					send(getServer().getConsoleSender(), prefix + "Sorry, but consoles can't execute this command.");
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
									send(player, prefix + "§6Location set for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("item")) {
									getConfig().set(slot + ".Item", player.getItemInHand().getTypeId() + ":" + player.getItemInHand().getDurability());
									send(player, prefix + "§6Item set for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("enable")) {
									if (getConfig().contains(slot + ".Enabled")) {
										if (getConfig().getBoolean(slot + ".Enabled")) {
											send(player, prefix + "§6Disabled slot " + slot + ".");
											getConfig().set(slot + ".Enabled", false);
										} else {
											send(player, prefix + "§6Enabled slot " + slot + ".");
											getConfig().set(slot + ".Enabled", true);
										}
									} else {
										send(player, prefix + "§6Enabled slot " + slot + ".");
										getConfig().set(slot + ".Enabled", true);
									}
								} else if (args[1].equalsIgnoreCase("bungee")) {
									getConfig().set(slot + ".Bungee", null);
									send(player, prefix + "§6Bungee unset for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("lilypad")) {
									getConfig().set(slot + ".Lilypad", null);
									send(player, prefix + "§6Lilypad unset for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("lore")) {
									getConfig().set(slot + ".Lore", null);
									send(player, prefix + "§6Lore unset for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("warp")) {
									getConfig().set(slot + ".Warp", null);
									send(player, prefix + "§6Warp unset for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("price")) {
									getConfig().set(slot + ".Price", null);
									send(player, prefix + "§6Price unset for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("amount")) {
									getConfig().set(slot + ".Amount", null);
									send(player, prefix + "§6Amount unset for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("enchant")) {
									if (getConfig().contains(slot + ".Enchant")) {
										if (getConfig().getBoolean(slot + ".Enchant")) {
											send(player, prefix + "§6Removed enchant from slot " + slot + ".");
											getConfig().set(slot + ".Enchant", null);
										} else {
											send(player, prefix + "§6Added enchant to slot " + slot + ".");
											getConfig().set(slot + ".Enchant", true);
										}
									} else {
										send(player, prefix + "§6Added enchant to slot " + slot + ".");
										getConfig().set(slot + ".Enchant", true);
									}
								} else if (args[1].equalsIgnoreCase("command")) {
									getConfig().set(slot + ".Command", null);
									send(player, prefix + "§6Command unset for slot " + slot + ".");
								} else {
									sendSetupMessage(player, slot);
								}
								saveConfig();
							} else {
								send(player, prefix + "§6You haven't specified a slot to modify.");
							}
						} else {
							send(player, "§4You do not have access to that command.");
						}
					} else {
						sendHelpMessage(sender);
					}
				} else {
					send(getServer().getConsoleSender(), prefix + "Sorry, but consoles can't execute this command.");
				}
			} else if (args.length >= 3) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					if (player.hasPermission("compassnav.admin.setup")) {
						if (!slot.equals("0")) {
							if (args[0].equalsIgnoreCase("setup")) {
								if (args[1].equalsIgnoreCase("bungee")) {
									getConfig().set(slot + ".Bungee", handleString(args));
									send(player, prefix + "§6Bungee set for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("lilypad")) {
									getConfig().set(slot + ".Lilypad", handleString(args));
									send(player, prefix + "§6Lilypad set for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("name")) {
									getConfig().set(slot + ".Name", handleString(args));
									send(player, prefix + "§6Name set for slot " + slot + "!");
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
									send(player, prefix + "§6Lore set for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("warp")) {
									getConfig().set(slot + ".Warp", handleString(args));
									send(player, prefix + "§6Warp set for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("price")) {
									getConfig().set(slot + ".Price", Double.parseDouble(handleString(args)));
									send(player, prefix + "§6Price set for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("amount")) {
									getConfig().set(slot + ".Amount", Integer.parseInt(handleString(args)));
									send(player, prefix + "§6Amount set for slot " + slot + "!");
								} else if (args[1].equalsIgnoreCase("command")) {
									getConfig().set(slot + ".Command", handleString(args));
									send(player, prefix + "§6Command set for slot " + slot + "!");
								} else {
									sendSetupMessage(player, slot);
								}
								saveConfig();
							} else {
								sendHelpMessage(player);
							}
						} else {
							send(player, prefix + "§6You haven't specified a slot to modify.");
						}
					} else {
						send(player, "§4You do not have access to that command.");
					}
				} else {
					send(getServer().getConsoleSender(), prefix + "Sorry, but consoles can't execute this command.");
				}
			}
    	} else {
    		sendHelpMessage(sender);
    	}
		return true;
	}
}