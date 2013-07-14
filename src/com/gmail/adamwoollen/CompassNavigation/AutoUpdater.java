package com.gmail.adamwoollen.CompassNavigation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings("all")
public class AutoUpdater implements Runnable, Listener, CommandExecutor, CommandSender
{
  public long delay = 216000L;
  public String bukkitdevSlug = "compass-navigation";
  public final ChatColor COLOR_INFO = ChatColor.BLUE;
  public final ChatColor COLOR_OK = ChatColor.GREEN;
  public final ChatColor COLOR_ERROR = ChatColor.RED;
  public boolean debug = true;
  
  public final String version = "1.4";
  public String consolePrefix = "[CN]";
  
  public final Plugin plugin;
  public int pid = -1;
  public final String av;
  public Configuration config;
  
  boolean enabled = false;
  public final AtomicBoolean lock = new AtomicBoolean(false);
  public boolean needUpdate = false;
  public boolean updatePending = false;
  public String updateURL;
  public String updateVersion;
  public String pluginURL;
  public String type;
  
  public ArrayList<CommandExecutor> otherUpdaters;
  
  public AutoUpdater(CompassNavigation plugin) throws Exception
  {
	  this(plugin, plugin.getConfig());
  }

  public AutoUpdater(CompassNavigation plugin, Configuration config) throws Exception
  {
	if(plugin == null)
	  throw new Exception("Plugin can not be null");
	if(!plugin.isEnabled())
	  throw new Exception("Plugin not enabled");
	this.plugin = plugin;
	av = plugin.getDescription().getVersion();
	if(bukkitdevSlug == null || bukkitdevSlug.equals(""))
	  bukkitdevSlug = plugin.getName();
	bukkitdevSlug = bukkitdevSlug.toLowerCase();
	consolePrefix = plugin.consolePrefix;
	if(delay < 72000L)
	{
	  plugin.getLogger().info("[CompassNav] delay < 72000 ticks not supported. Setting delay to 72000.");
	  delay = 72000L;
	}
	setConfig(config);
	registerCommand();
	plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  public boolean restartMainTask()
  {
	try
	{
	  ResetTask rt = new ResetTask(enabled);
	  rt.setPid(plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, rt, 0L, 1L));
	  return enabled;
	}
	catch(Throwable t)
	{
	  printStackTraceSync(t, false);
	  return false;
	}
  }
  
  public boolean checkState(boolean newState, boolean restart)
  {
	if(enabled != newState)
	{
	  enabled = newState;
	  plugin.getLogger().info(consolePrefix + "v"+version+(enabled ? " enabled" : " disabled")+"!");
	  if(restart)
		return restartMainTask();
	}
	return enabled;
  }
  
  public class ResetTask implements Runnable
  {
	public int pid;
	public final boolean restart;
	
	public ResetTask(boolean restart)
	{
	  this.restart = restart;
	}
	
	public void setPid(int pid)
	{
	  this.pid = pid;
	}
	
	public void run()
	{
	  try
	  {
		if(!lock.compareAndSet(false, true))
		  return;
		BukkitScheduler bs = plugin.getServer().getScheduler();
		if(bs.isQueued(AutoUpdater.this.pid) || bs.isCurrentlyRunning(AutoUpdater.this.pid))
		  bs.cancelTask(AutoUpdater.this.pid);
		if(restart)
		  AutoUpdater.this.pid = bs.scheduleAsyncRepeatingTask(plugin, AutoUpdater.this, 5L, delay);
		else
		  AutoUpdater.this.pid = -1;
		lock.set(false);
		bs.cancelTask(pid);
	  }
	  catch(Throwable t)
	  {
		printStackTraceSync(t, false);
	  }
	}
  }
  
  public void resetConfig() throws FileNotFoundException
  {
	setConfig(plugin.getConfig());
  }
  
  public void setConfig(Configuration config) throws FileNotFoundException
  {
	if(config == null)
	  throw new FileNotFoundException("Config can not be null");
	try
	{
	  if(!lock.compareAndSet(false, true))
	  {
		ConfigSetter cf = new ConfigSetter(config);
		cf.setPid(plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, cf, 0L, 1L));
	  }
	  else
	  {
		setConfig2(config);
		lock.set(false);
	  }
	}
	catch(Throwable t)
	{
	  printStackTraceSync(t, false);
	}
  }
  
  public class ConfigSetter implements Runnable
  {
	public final Configuration config;
	public int pid;
	
	public ConfigSetter(Configuration config)
	{
	  this.config = config;
	}
	
	public void setPid(int pid)
	{
	  this.pid = pid;
	}
	
	public void run()
	{
	  if(!lock.compareAndSet(false, true))
		return;
	  setConfig2(config);
	  lock.set(false);
	  plugin.getServer().getScheduler().cancelTask(pid);
	}
  }
  
  public void setConfig2(Configuration config)
  {
	if(!config.isSet("compassnav"))
	  config.set("compassnav", true);
	checkState(config.getBoolean("compassnav"), true);
  }
  
  public void run()
  {
	if(!plugin.isEnabled())
	{
	  plugin.getServer().getScheduler().cancelTask(pid);
	  return;
	}
	try
	{
	  while(!lock.compareAndSet(false, true))
	  {
		try
		{
		  Thread.sleep(1L);
		}
		catch(InterruptedException e)
		{
		}
		continue;
	  }
	  try
	  {
		InputStreamReader ir;
		URL url = new URL("http://api.bukget.org/api2/bukkit/plugin/"+bukkitdevSlug+"/latest");
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		con.connect();
		int res = con.getResponseCode();
		if(res != 200)
		{
		  if(debug)
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SyncMessageDelayer(null, new String[] {"[compassnav] WARNING: Bukget returned "+res}));
		  lock.set(false);
		  return;
		}
		ir = new InputStreamReader(con.getInputStream());
		
		String nv;
		try
		{
		  JSONParser jp = new JSONParser();
		  Object o = jp.parse(ir);
		  
		  if(!(o instanceof JSONObject))
		  {
			ir.close();
			lock.set(false);
			return;
		  }
		  
		  JSONObject jo = (JSONObject)o;
		  jo = (JSONObject)jo.get("versions");
		  nv = (String)jo.get("version");
		  if(av.equals(nv) || (updateVersion != null && updateVersion.equals(nv)))
		  {
			ir.close();
			pluginURL = null;
			lock.set(false);
			return;
		  }
		  updateURL = (String)jo.get("download");
		  pluginURL = (String)jo.get("link");
		  updateVersion = nv;
		  type = (String)jo.get("type");
		  needUpdate = true;
		  ir.close();
		}
		catch(ParseException e)
		{
		  lock.set(false);
		  printStackTraceSync(e, true);
		  ir.close();
		  return;
		}
		final String[] out = new String[] {
				"["+plugin.getName()+"] New "+type+" available!",
				"If you want to update from "+av+" to "+updateVersion+" use /update "+plugin.getName(),
				"See "+pluginURL+" for more information."
		};
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SyncMessageDelayer(null, out));
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
		  public void run()
		  {
			String[] rout = new String[3];
			for(int i = 0; i < 3; i++)
			  rout[i] = COLOR_INFO+out[i];
			for(Player p: plugin.getServer().getOnlinePlayers())
			  if(hasPermission(p, "compassnav.announce"))
				p.sendMessage(rout);
		  }
		});
	  }
	  catch(Exception e)
	  {
		printStackTraceSync(e, true);
	  }
	  lock.set(false);
	}
	catch(Throwable t)
	{
	  printStackTraceSync(t, false);
	}
  }
  
  @EventHandler(priority = EventPriority.MONITOR)
  public void adminJoin(PlayerJoinEvent event)
  {
	try
	{
	  if(!enabled || !lock.compareAndSet(false, true))
		return;
	  Player p = event.getPlayer();
	  String[] out;
	  if(needUpdate)
	  {
		if(hasPermission(p, "compassnav.announce"))
		{
		  out = new String[] {
				  COLOR_INFO+"["+plugin.getName()+"] New "+type+" available!",
				  COLOR_INFO+"If you want to update from "+av+" to "+updateVersion+" use /update "+plugin.getName(),
				  COLOR_INFO+"See "+pluginURL+" for more information."
		  };
		}
		else
		  out = null;
	  }
	  else if(updatePending)
	  {
		if(hasPermission(p, "compassnav.announce"))
		{
		  out = new String[] {
				  COLOR_INFO+"Please restart the server to finish the update of "+plugin.getName(),
				  COLOR_INFO+"See "+pluginURL+" for more information."
		  };
		}
		else
		  out = null;
	  }
	  else
		out = null;
	  lock.set(false);
	  if(out != null)
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SyncMessageDelayer(p.getName(), out));
	}
	catch(Throwable t)
	{
	  printStackTraceSync(t, false);
	}
  }
  
  public class SyncMessageDelayer implements Runnable
  {
	public final String p;
	public final String[] msgs;
	
	public SyncMessageDelayer(String p, String[] msgs)
	{
	  this.p = p;
	  this.msgs = msgs;
	}
	
	public void run()
	{
	  try
	  {
		if(p != null)
		{
		  Player p = plugin.getServer().getPlayerExact(this.p);
		  if(p != null)
			for(String msg: msgs)
			  if(msg != null)
				p.sendMessage(msg);
		}
		else
		{
		  Logger log = plugin.getLogger();
		  for(String msg: msgs)
			if(msg != null)
			  log.info(msg);
		}
	  }
	  catch(Throwable t)
	  {
		printStackTraceSync(t, false);
	  }
	}
  }
  
  public void registerCommand()
  {
	try
	{
	  SimplePluginManager pm = (SimplePluginManager)plugin.getServer().getPluginManager();
	  Field f = SimplePluginManager.class.getDeclaredField("commandMap");
	  f.setAccessible(true);
	  SimpleCommandMap cm = (SimpleCommandMap)f.get(pm);
	  f.setAccessible(false);
	  if(cm.getCommand("update") == null) // First!
	  {
		Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
		c.setAccessible(true);
		PluginCommand cmd = c.newInstance("update", plugin);
		c.setAccessible(false);
		cmd.setExecutor(this);
		cm.register("update", cmd);
		otherUpdaters = new ArrayList<CommandExecutor>();
	  }
	  else
		plugin.getServer().dispatchCommand(this, "update [REGISTER]");
	}
	catch(Throwable t)
	{
	  printStackTraceSync(t, false);
	}
  }
  
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
  {
	try
	{
	  if(args.length > 0)
	  {
		if(args[0].equals("[REGISTER]"))
		{
		  otherUpdaters.add((CommandExecutor)sender);
		  return true;
		}
		if(!plugin.getName().equalsIgnoreCase(args[0]))
		{
		  informOtherUpdaters(sender, args);
		  return true;
		}
	  }
	  else
		informOtherUpdaters(sender, args);
	  update(sender);
	}
	catch(Throwable t)
	{
	  printStackTraceSync(t, false);
	}
	return true;
  }
  
  public void informOtherUpdaters(CommandSender sender, String[] args)
  {
	try
	{
	  if(otherUpdaters != null)
		for(CommandExecutor ou: otherUpdaters)
		  ou.onCommand(sender, null, null, args);
	}
	catch(Throwable t)
	{
	  printStackTraceSync(t, false);
	}
  }
  
  public void update(CommandSender sender)
  {
	if(!hasPermission(sender, "compassnav.update")) {
	{
	  sender.sendMessage(COLOR_ERROR+plugin.getName()+": You are not allowed to update CompassNavigation!");
	  return;
	}
	} else {
		final BukkitScheduler bs = plugin.getServer().getScheduler();
		final String pn = sender instanceof Player ? ((Player)sender).getName() : null;
		bs.scheduleAsyncDelayedTask(plugin, new Runnable()
		{
			public void run()
			{
				try
				{
					while(!lock.compareAndSet(false, true))
					{
						try
						{
							Thread.sleep(1L);
						}
						catch(InterruptedException e)
						{
						}
					}
					String out;
					try
					{
						File to = new File(plugin.getServer().getUpdateFolderFile(), updateURL.substring(updateURL.lastIndexOf('/')+1, updateURL.length()));
						File tmp = new File(to.getPath()+".au");
						if(!tmp.exists())
						{
							plugin.getServer().getUpdateFolderFile().mkdirs();
							tmp.createNewFile();
						}
						URL url = new URL(updateURL);
						InputStream is = url.openStream();
						OutputStream os = new FileOutputStream(tmp);
						byte[] buffer = new byte[4096];
						int fetched;
						while((fetched = is.read(buffer)) != -1)
							os.write(buffer, 0, fetched);
						is.close();
						os.flush();
						os.close();
						if(to.exists())
							to.delete();
						tmp.renameTo(to);
						out = COLOR_OK+plugin.getName()+" ready! Restart server to finish the update.";
						needUpdate = false;
						updatePending = true;
						updateURL = type = null;
					}
					catch(Exception e)
					{
			out = COLOR_ERROR+plugin.getName()+" failed to update!";
			printStackTraceSync(e, true);
					}
					bs.scheduleSyncDelayedTask(plugin, new SyncMessageDelayer(pn, new String[] {out}));
					lock.set(false);
				}
				catch(Throwable t)
				{
					printStackTraceSync(t, false);
				}
			}
		});
		}
	}
  
  public void printStackTraceSync(Throwable t, boolean expected)
  {
	BukkitScheduler bs = plugin.getServer().getScheduler();
	try
	{
	  String prefix = consolePrefix;
	  StringWriter sw = new StringWriter();
	  PrintWriter pw = new PrintWriter(sw);
	  t.printStackTrace(pw);
	  String[] sts = sw.toString().replace("\r", "").split("\n");
	  String[] out;
	  if(expected)
		out = new String[sts.length+25];
	  else
		out = new String[sts.length+27];
	  out[0] = prefix;
	  out[1] = prefix+"Internal error!";
	  out[2] = prefix+"If this bug hasn't been reported please open a ticket at http://dev.bukkit.org/bukkit-plugins/compass-navigation/";
	  out[3] = prefix+"Include the following into your bug report:";
	  out[4] = prefix+"          ======= SNIP HERE =======";
	  int i = 5;
	  for(; i-5 < sts.length; i++)
		out[i] = prefix+sts[i-5];
	  out[++i] = prefix+"          ======= DUMP =======";
	  out[++i] = prefix+"version        : "+version;
	  out[++i] = prefix+"delay          : "+delay;
	  out[++i] = prefix+"bukkitdevSlug  : "+bukkitdevSlug;
	  out[++i] = prefix+"COLOR_INFO     : "+COLOR_INFO.name();
	  out[++i] = prefix+"COLO_OK        : "+COLOR_OK.name();
	  out[++i] = prefix+"COLOR_ERROR    : "+COLOR_ERROR.name();
	  out[++i] = prefix+"pid            : "+pid;
	  out[++i] = prefix+"av             : "+av;
	  out[++i] = prefix+"config         : "+config;
	  out[++i] = prefix+"lock           : "+lock.get();
	  out[++i] = prefix+"needUpdate     : "+needUpdate;
	  out[++i] = prefix+"updatePending  : "+updatePending;
	  out[++i] = prefix+"UpdateUrl      : "+updateURL;
	  out[++i] = prefix+"updateVersion  : "+updateVersion;
	  out[++i] = prefix+"pluginURL      : "+pluginURL;
	  out[++i] = prefix+"type           : "+type;
	  out[++i] = prefix+"          ======= SNIP HERE =======";
	  out[++i] = prefix;
	  if(!expected)
	  {
		out[++i] = prefix+"DISABLING UPDATER!";
		out[++i] = prefix;
	  }
	  bs.scheduleSyncDelayedTask(plugin, new SyncMessageDelayer(null, out));
	}
	catch(Throwable e) //This prevents endless loops.
	{
	  e.printStackTrace();
	}
	if(!expected)
	{
	  bs.cancelTask(pid);
	  bs.scheduleAsyncDelayedTask(plugin, new Runnable()
	  {
		public void run()
		{
		  while(!lock.compareAndSet(false, true))
		  {
			try
			{
			  Thread.sleep(1L);
			}
			  catch(InterruptedException e)
			{
			}
		  }
		  pid = -1;
		  config = null;
		  needUpdate = updatePending = enabled = false;
		  updateURL = updateVersion = pluginURL = type = null;
		  lock.set(false);
		}
	  });
	}
  }
  
  public boolean hasPermission(Permissible player, String node)
  {
	if(player.isPermissionSet(node))
	  return player.hasPermission(node);
	while(node.contains("."))
	{
	  node = node.substring(0, node.lastIndexOf("."));
	  if(player.isPermissionSet(node))
	    return player.hasPermission(node);
	  if(player.isPermissionSet(node+".*"))
	    return player.hasPermission(node+".*");
	}
	if(player.isPermissionSet("*"))
	  return player.hasPermission("*");
	return player.isOp();
  }

  public void setDebug(boolean mode)
  {
	debug = mode;
  }


  public boolean getDebug()
  {
	return debug;
  }

  public PermissionAttachment addAttachment(Plugin arg0) {
	return null;
  }

  public PermissionAttachment addAttachment(Plugin arg0, int arg1) {
	return null;
  }

  public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2) {
	return null;
  }

  public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2, int arg3) {
	return null;
  }

  public Set<PermissionAttachmentInfo> getEffectivePermissions() {
	return null;
  }

  public boolean hasPermission(String arg0) {
	return false;
  }

  public boolean hasPermission(Permission arg0) {
	return false;
  }

  public boolean isPermissionSet(String arg0) {
	return false;
  }

  public boolean isPermissionSet(Permission arg0) {
	return false;
  }

  public void recalculatePermissions() {}

  public void removeAttachment(PermissionAttachment arg0) {}

  public boolean isOp() {
	return false;
  }

  public void setOp(boolean arg0) {}

  public String getName() {
	return null;
  }

  public Server getServer() {
	return null;
  }

  public void sendMessage(String arg0) {}

  public void sendMessage(String[] arg0) {}
}