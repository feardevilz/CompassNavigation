package com.gmail.adamwoollen.CompassNavigation;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;

import org.bukkit.configuration.file.YamlConfiguration;

public class AutoUpdater {
	
	public CompassNavigation plugin;
    public UpdateType type;
    public String versionTitle;
    public String versionLink;
    public long totalSize;
    public int sizeLine;
    public int multiplier;
    public boolean announce;
    public URL url;
    public File file;
    public Thread thread;
    public Thread downloadThread;
    public String updateFolder = YamlConfiguration.loadConfiguration(new File("bukkit.yml")).getString("settings.update-folder");
    public AutoUpdater.UpdateResult result = AutoUpdater.UpdateResult.SUCCESS;

    public enum UpdateResult {
        SUCCESS,
        NO_UPDATE,
        FAIL_DOWNLOAD,
        FAIL_DBO,
        FAIL_NOVERSION,
        UPDATE_AVAILABLE
    }

    public enum UpdateType {
        DEFAULT,
        NO_VERSION_CHECK,
        NO_DOWNLOAD
    }

    public AutoUpdater(CompassNavigation plugin, File file, UpdateType type, boolean announce) {
        this.plugin = plugin;
        this.type = type;
        this.announce = announce;
        this.file = file;
        
        try {
            url = new URL("http://dev.bukkit.org/bukkit-plugins/compass-navigation/files.rss");
        } catch (Exception e) {
	        plugin.getLogger().warning("[AutoUpdater] Couldn't contact RSS feed. Network down?");
            result = AutoUpdater.UpdateResult.FAIL_DBO;
        }
        
        thread = new Thread(new UpdateRunnable());
        thread.start();
    }

    public AutoUpdater.UpdateResult getResult() {
        waitForThread();
        return result;
    }

    public long getFileSize() {
        waitForThread();
        return totalSize;
    }

    public String getLatestVersionString() {
        waitForThread();
        return versionTitle;
    }
    
    public void waitForThread() {
        if (thread.isAlive()) {
            try {
                thread.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void waitForDownloadThread() {
    	if (downloadThread.isAlive()) {
    		try {
    			downloadThread.join();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    }

    public void saveFile(File folder, String file, String u) {
        if (!folder.exists()) {
            folder.mkdir();
        }
        
        try {
            URL url = new URL(u);
            int fileLength = url.openConnection().getContentLength();
            BufferedInputStream in = new BufferedInputStream(url.openStream());
            FileOutputStream fout = new FileOutputStream(folder.getAbsolutePath() + "/" + file);

            byte[] data = new byte[1024];
            int count;
            long downloaded = 0;
            
            if (announce) {
            	plugin.getLogger().info("[AutoUpdater] About to download a new update: " + versionTitle);
            }
            
            while ((count = in.read(data, 0, 1024)) != -1)  {
                downloaded += count;
                fout.write(data, 0, count);
                int percent = (int) (downloaded * 100 / fileLength);
                if (announce & (percent % 10 == 0)) {
                    plugin.getLogger().info("[AutoUpdater] Downloading update: " + percent + "% of " + fileLength + " bytes.");
                }
            }
            
            if (announce) {
            	plugin.getLogger().info("[AutoUpdater] Finished updating.");
            }
            
            in.close();
            fout.close();
            
            result = AutoUpdater.UpdateResult.SUCCESS;
        } catch (Exception ex) {
            plugin.getLogger().warning("[AutoUpdater] The auto-updater tried to download a new update, but was unsuccessful.");
            result = AutoUpdater.UpdateResult.FAIL_DOWNLOAD;
        }
    }

    public String getFile(String link) {
        String download = null;
        try {
            URL url = new URL(link);
            URLConnection urlConn = url.openConnection();
            InputStreamReader inStream = new InputStreamReader(urlConn.getInputStream());
            BufferedReader buff = new BufferedReader(inStream);

            int counter = 0;
            String line;
            while ((line = buff.readLine()) != null) {
                counter++;
                if (line.contains("<li class=\"user-action user-action-download\">")) {
                    download = line.split("<a href=\"")[1].split("\">Download</a>")[0];
                } else if (line.contains("<dt>Size</dt>")) {
                    sizeLine = counter + 1;
                } else if (counter == sizeLine) {
                    String size = line.replaceAll("<dd>", "").replaceAll("</dd>", "");
                    multiplier = size.contains("MiB") ? 1048576 : 1024;
                    size = size.replace(" KiB", "").replace(" MiB", "");
                    totalSize = (long)(Double.parseDouble(size) * multiplier);
                }
            }
            urlConn = null;
            inStream = null;
            buff.close();
            buff = null;
        } catch (Exception e) {
            e.printStackTrace();
            plugin.getLogger().warning("[AutoUpdater] Couldn't contact DBO database. Network down?");
            result = AutoUpdater.UpdateResult.FAIL_DBO;
            return null;
        }
        return download;
    }

    public boolean versionCheck(String title) {
        if (type != UpdateType.NO_VERSION_CHECK) {
            String version = plugin.getDescription().getVersion();
            if (title.split(" v").length == 2) {
                String remoteVersion = title.split(" v")[1].split(" ")[0];
                int remVer = -1;
                int curVer = 0;
                
                try {
                    remVer = calVer(remoteVersion);
                    curVer = calVer(version);
                } catch (Exception e) {
                    remVer = -1;
                }
                
                if (version.contains("SNAPSHOT") || version.equalsIgnoreCase(remoteVersion) || curVer >= remVer) {
                    result = AutoUpdater.UpdateResult.NO_UPDATE;
                    return false;
                }
            } else {
                plugin.getLogger().warning("[AutoUpdater] Couldn't check for latest version.");
                plugin.getLogger().warning("[AutoUpdater] Please notify danielyourbest about this issue.");
                result = AutoUpdater.UpdateResult.FAIL_NOVERSION;
                return false;
            }
        }
        return true;
    }
    
    public Integer calVer(String s) throws NumberFormatException {
        if (s.contains(".")) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i <s.length(); i++) {
                Character c = s.charAt(i);
                if (Character.isLetterOrDigit(c)) {
                    sb.append(c);
                }
            }
            return Integer.parseInt(sb.toString());
        }
        return Integer.parseInt(s);
    }

    public boolean readFeed() {
        try {
            String title = "";
            String link = "";
            boolean success = false;
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            InputStream in = read();
            
            if (in != null) {
                XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
                
                while (eventReader.hasNext()) {
                    XMLEvent event = eventReader.nextEvent();
                    if (event.isStartElement()) {
                        if (event.asStartElement().getName().getLocalPart().equals("title")) {
                            event = eventReader.nextEvent();
                            title = event.asCharacters().getData();
                            continue;
                        }
                        
                        if (event.asStartElement().getName().getLocalPart().equals("link")) {
                            event = eventReader.nextEvent();
                            link = event.asCharacters().getData();
                            continue;
                        }
                    } else if (event.isEndElement()) {
                        if (event.asEndElement().getName().getLocalPart().equals("item")) {
                            versionTitle = title;
                            versionLink = link;
                            break;
                        }
                    }
                }
                
                success = true;
            }
            
            if (in != null) {
            	in.close();
            }
            
            return success;
        } catch (Exception e) {
            plugin.getLogger().warning("[AutoUpdater] Couldn't contact DBO database. Network down?");
            return false;
        }
    }

    public InputStream read() {
        try {
            return url.openStream();
        } catch (Exception e) {
            plugin.getLogger().warning("[AutoUpdater] Couldn't contact DBO database. Network down?");
            return null;
        }
    }
    
    public void downloadLatestVersion() {
    	type = AutoUpdater.UpdateType.NO_VERSION_CHECK;
    	downloadThread = new Thread(new UpdateRunnable());
    	downloadThread.start();
    	waitForDownloadThread();
    	type = AutoUpdater.UpdateType.DEFAULT;
    }

    public class UpdateRunnable implements Runnable {
    	public void run() {
            if (url != null) {
                if (readFeed()) {
                    if (versionCheck(versionTitle)) {
                        String fileLink = getFile(versionLink);
                        if (fileLink != null && type != UpdateType.NO_DOWNLOAD) {
                            String name = file.getName();
                            saveFile(new File("plugins/" + updateFolder), name, fileLink);
                        } else {
                            result = UpdateResult.UPDATE_AVAILABLE;
                        }
                    }
                }
            }
        }
    }
}