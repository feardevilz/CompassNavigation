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
	
	// NEWS:
	// I have cleaned up the source a bit.
	// I have changed over from ChatColor to § format.
	// I have made permissions more flexible. There is 2 collective permissions, that
	// allows players to do everything: compassnav.* and compassnav.admin.
	// Fixed a nasty bug that would mess up everything.
	// Fixed a nasty nasty bug that would let only Console use /cnsetup with 2 
	// parameters. Very very nasty bug.
	// Fixed a bug that ignores an item slot. Possible fix for the bug.
	// Added 3 new rows (Like a large chest) =D
	// Another possible fix for the inventory bug: On inventory interact I made an item
	// check: It only starts the checking if you are holding the compass item.
	// I have changed the "No permission" lore to a dark red one, instead of colorless.
	
	// TODO: Add a help section for the command /compassnav.
	// TODO: Add essentials warp support
	// TODO: Finish /cnsetup: Finish command, add console support.
	
	public void onEnable() {
        this.saveDefaultConfig();
		getServer().getPluginManager().registerEvents(new EventListener(this), this);
	}
	
	String prefix = "§a[§6CN§a]";
	String slot = "0";
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
    	if(cmd.getName().equalsIgnoreCase("compassnav") || (cmd.getName().equalsIgnoreCase("cn"))) {
    		if (args.length == 1) {
    		    if (sender instanceof Player) {
    			    Player p = (Player) sender;
    			    if (p.hasPermission("compassnav.admin.reload")) {
    				    if (args[0].equalsIgnoreCase("reload")) {
    					    this.reloadConfig();
    						p.sendMessage(prefix + "§6Compass Navigation reloaded!");
    					} else {
    						if (p.hasPermission("compassnav.admin.setup")) {
    							if (args[0].equalsIgnoreCase("setup")) {
    								p.sendMessage(prefix + " §6Please specify a slot number.");
    								p.sendMessage(prefix + " §6Usage:   /CN setup <1>");
    							}
    						}
    					}
    				} else {
    					if (args[0].equalsIgnoreCase("reload")) {
    						this.reloadConfig();
    						getLogger().info("Compass Navigation reloaded!");
    					}
    				}
    			}
    		} else if (args.length == 2) {
    			if (sender instanceof Player) {
    				Player p = (Player) sender;
    				if (p.hasPermission("compassnav.admin.setup")) {
    					if (args[0].equalsIgnoreCase("setup")) {
    						if (isInteger(args[1])) {
    							if (Integer.parseInt(args[1]) <= 27) {
    								slot = args[1]
    								p.sendMessage("§2------------------------ §6CN setup §2-------------------------");
    								p.sendMessage("§2/CN setup desc <description>     <--- Sets item description");
    								p.sendMessage(prefix + " §6You are now setting up slot " + args[1]);
    								p.sendMessage(prefix + " §6/CN setup loc                    <--- Sets location");
    								p.sendMessage(prefix + " §6/CN setup name <name>            <--- Sets item name");
    								p.sendMessage(prefix + " §6/CN setup desc <description>     <--- Sets item description");
    								p.sendMessage("§2---------------------------------------------------------------");
    								return true;
    							}
    						} else if(!slot.equals("0")){
    							if (args[1].equalsIgnoreCase("loc")) {
    								this.getConfig().set(slot + ".X", p.getLocation().getX());
    								this.getConfig().set(slot + ".Y", p.getLocation().getY());
    								this.getConfig().set(slot + ".Z", p.getLocation().getZ());
    								return true;
    							} else if (args[1].equalsIgnoreCase("name")) {
    								if(args[2] != null){
    									StringBuilder sb = new StringBuilder();
										for (int i = 2; i < args.length; i++) {
											sb.append(args[i]).append(" ");
										}
										String name = sb.toString().trim();
	    								this.getConfig().set(slot + ".Name", name);
	    								return true;
    								}
    							} else if (args[1].equalsIgnoreCase("desc")) {
    								if(args[2] != null){
    									StringBuilder sb = new StringBuilder();
										for (int i = 2; i < args.length; i++) {
											sb.append(args[i]).append(" ");
										}
										String desc = sb.toString().trim();
	    								this.getConfig().set(slot + ".Desc", desc);
	    								return true;
    								}
    							}
    						}
    						p.sendMessage(prefix + " §6This is not a valid number");
    					}
    				}
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
