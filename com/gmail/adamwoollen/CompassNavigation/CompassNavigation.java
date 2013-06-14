package com.gmail.adamwoollen.CompassNavigation;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class CompassNavigation extends JavaPlugin {

  // CHAT COLORS			FORMATTING CODES
	// §0: Black			§k: Obfuscated
	// §1: Dark Blue		§l: Bold
	// §2: Dark Green		§m: Strikethrough
	// §3: Dark Aqua		§n: Underline
	// §4: Dark Red			§o: Italic
	// §5: Purple			§r: Reset
	// §6: Gold (Orange)
	// §7: Gray
	// §8: Dark Gray
	// §9: Blue
	// §a: Green
	// §b: Aqua
	// §c: Red
	// §d: Light Purple (Pink)
	// §e: Yellow
	// §f: White
	
	public void onEnable() {
        this.saveDefaultConfig();
		getServer().getPluginManager().registerEvents(new EventListener(this), this);
	}
	
	String prefix = "§2§l[§a§lCN§2§l] ";
	String slot = "0";
	
	public void sendHelpMessage(CommandSender p) {
		if (p.hasPermission("compassnav.admin.help")) {
			p.sendMessage("§6§lHELP§f | §7/compassnav help");
			p.sendMessage("§6Oo-----------------------oOo-----------------------oO");
			p.sendMessage("§2/compassnav help§a - Get command help");
			p.sendMessage("§2/compassnav reload§a - Reload CompassNavigation");
			p.sendMessage("§2/compassnav setup§a - Set up compass inventory slots");
			p.sendMessage("§6Oo-----------------------oOo-----------------------oO");
		} else {
			p.sendMessage("§4You do not have access to that command.");
		}
	}
		
	public void sendSetupMessage(CommandSender p, String slot) {
		if (p.hasPermission("compassnav.admin.setup")) {
			p.sendMessage("§6§lSETUP§f | §7/compassnav setup");
			p.sendMessage("§6Oo-----------------------oOo-----------------------oO");
			if (!slot.equals("0")) {
				p.sendMessage("§aYou are now setting up slot " + slot + ".");
			}
			p.sendMessage("§2/compassnav setup loc§a - Sets location");
			p.sendMessage("§2/compassnav setup bungee <server>§a - Sets BungeeCord server");
			p.sendMessage("§2/compassnav setup warp <warp>§a - Sets Essentials warp");
			p.sendMessage("§2/compassnav setup item§a - Sets item from hand");
			p.sendMessage("§2/compassnav setup name <name>§a - Sets item name");
			p.sendMessage("§2/compassnav setup desc <description>§a - Sets item description");
			p.sendMessage("§2/compassnav setup enable§a - Enables slot");
			p.sendMessage("§6Oo-----------------------oOo-----------------------oO");
		} else {
			p.sendMessage("§4You do not have access to that command.");
		}
	}
	
	public String handleString(String[] args) {
		StringBuilder sb = new StringBuilder();
		for (int i = 2; i < args.length; i++) {
			sb.append(args[i]).append(" ");
		}
		String nameS = sb.toString().trim();
		return nameS;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
    	if(cmd.getName().equalsIgnoreCase("compassnav") || (cmd.getName().equalsIgnoreCase("cn"))) {
    		if (args.length == 0) {
    			if (sender instanceof Player) {
    				this.sendHelpMessage(sender);
    			}
    		} else if (args.length == 1) {
    			if (sender instanceof Player) {
    			    Player p = (Player) sender;
				    if (args[0].equalsIgnoreCase("reload")) {
				    	if (p.hasPermission("compassnav.admin.reload")) {
					    	this.reloadConfig();
					    	p.sendMessage(prefix + "§6Compass Navigation reloaded!");;
				    	} else {
				    		p.sendMessage("§4You do not have access to that command.");
				    	}
				    } else if (args[0].equalsIgnoreCase("setup")) {
						if (p.hasPermission("compassnav.admin.setup")) {
							p.sendMessage("§6§lSETUP§f | §7/compassnav setup");
							p.sendMessage("§6Oo-----------------------oOo-----------------------oO");
							p.sendMessage("§aPlease specify a slot number.");
							p.sendMessage("§2Usage:§a /compassnav setup <1>");
							p.sendMessage("§6Oo-----------------------oOo-----------------------oO");
						} else {
							p.sendMessage("§4You do not have access to that command.");
						}
					} else {
						this.sendHelpMessage(sender);
					}
			    } else {
					if (args[0].equalsIgnoreCase("reload")) {
						this.reloadConfig();
						getLogger().info("[CN] Compass Navigation reloaded!");
					} else {
						getLogger().info("HELP | /compassnav help");
						getLogger().info("Oo-----------------------oOo-----------------------o");
						getLogger().info("/compassnav help - Get command help");
						getLogger().info("/compassnev reload - Reload CompassNavigation");
						getLogger().info("Oo-----------------------oOo-----------------------o");
					}
				}
			} else if (args.length == 2) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					if (args[0].equalsIgnoreCase("setup")) {
							if (isInteger(args[1])) {
								if (Integer.parseInt(args[1]) <= 54) {
									slot = args[1];
									this.sendSetupMessage(p, slot);
								}
							}
							if (p.hasPermission("compassnav.admin.setup")) {
								if (!slot.equals("0")) {
									if (args[1].equalsIgnoreCase("loc")) {
										this.getConfig().set(slot + ".World", p.getWorld().getName());
										this.getConfig().set(slot + ".X", p.getLocation().getX());
										this.getConfig().set(slot + ".Y", p.getLocation().getY());
										this.getConfig().set(slot + ".Z", p.getLocation().getZ());
										this.getConfig().set(slot + ".Yaw", p.getLocation().getYaw());
										this.getConfig().set(slot + ".Pitch", p.getLocation().getPitch());
										p.sendMessage(prefix + "§6Location set for slot " + slot + "!");
										this.saveConfig();
									} else if (args[1].equalsIgnoreCase("item")) {
										String ID = Integer.toString(p.getItemInHand().getTypeId());
										String Damage = Short.toString(p.getItemInHand().getDurability());
										if (ID != "0" && (Damage != "-1")) {
											if (Damage != "0") {
												this.getConfig().set(slot + ".Item", ID + ":" + Damage);
											} else {
												this.getConfig().set(slot + ".Item", ID);
											}
											p.sendMessage(prefix + "§6Item set for slot " + slot + "!");
											this.saveConfig();
										} else {
											p.sendMessage(prefix + "§6An error happened while trying to set the item. Are you sure it isn't air?");
										}
									} else if (args[1].equalsIgnoreCase("enable")){
										this.getConfig().set(slot + ".Enabled", true);
										this.saveConfig();
										p.sendMessage(prefix + "§6Enabled slot " + slot + ".");
										slot = "0";
										}
									} else if (args[1].equalsIgnoreCase("bungee")) {
										this.getConfig().set(slot + ".Bungee", null);
										p.sendMessage(prefix + "§6Bungee unset for slot " + slot + "!");
										this.saveConfig();
									} else if (args[1].equalsIgnoreCase("desc")) {
										this.getConfig().set(slot + ".Desc", null);
										p.sendMessage(prefix + "§6Description unset for slot " + slot + "!");
										this.saveConfig();
									} else if (args[1].equalsIgnoreCase("warp")) {
										this.getConfig().set(slot + ".Warp", null);
										p.sendMessage(prefix + "§6Warp unset for slot " + slot + "!");
										this.saveConfig();
									} else {
										this.sendSetupMessage(p, slot);
									}
								} else {
									p.sendMessage(prefix + "§6You haven't specified a slot to modify.");
								}
							} else {
								p.sendMessage("§4You do not have access to that command.");
							}
				} else {
					getLogger().info("[CN] This command can only be ran by ingame players.");
				}
			} else if (args.length >= 3) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					if (p.hasPermission("compassnav.admin.setup")) {
						if (!slot.equals("0")) {
							if (args[0].equalsIgnoreCase("setup")) {
								if (args[1].equalsIgnoreCase("bungee")) {
									this.getConfig().set(slot + ".Bungee", args[2]);
									p.sendMessage(prefix + "§6Bungee set for slot " + slot + "!");
									this.saveConfig();
								} else if (args[1].equalsIgnoreCase("name")) {
									this.getConfig().set(slot + ".Name", handleString(args));
									p.sendMessage(prefix + "§6Name set for slot " + slot + "!");
									this.saveConfig();
								} else if (args[1].equalsIgnoreCase("desc")) {
									this.getConfig().set(slot + ".Desc", handleString(args));
									p.sendMessage(prefix + "§6Description set for slot " + slot + "!");
									this.saveConfig();
								} else if (args[1].equalsIgnoreCase("warp")) {
									this.getConfig().set(slot + ".Warp", handleString(args));
									p.sendMessage(prefix + "§6Warp set for slot " + slot + "!");
									this.saveConfig();
								} else {
									this.sendSetupMessage(p, slot);
								}
							} else {
								this.sendSetupMessage(p, slot);
							}
						} else {
							p.sendMessage(prefix + "§6You haven't specified a slot to modify.");
						}
					} else {
						p.sendMessage("§4You do not have access to that command.");
					}
				} else {
					getLogger().info("[CN] This command can only be ran by ingame players.");
				}
			}
    	}
    	return true;
	}

	public boolean isInteger(String s) {
	    try {
	        Integer.parseInt(s);
	    } catch(NumberFormatException e) {
	        return false;
	    }
		return true;
	}
}
