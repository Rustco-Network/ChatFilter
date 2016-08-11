package de.thexxturboxx.chatfilter;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.huskehhh.mysql.mysql.MySQL;

public class ChatFilter extends JavaPlugin implements Listener {
	
	public static ChatFilter instance;
	public static File path = new File("plugins/ChatFilter"), dataPath;
	private static CommandMap cmap;
	public static List<String> boeseWoerter;
	public static MySQL MySQL = null;
    public static Connection c = null;
    public static final String TABLE_WOERTER = "ChatFilter",
    		TABLE_USER = "ChatFilter_user",
    		DATABASE = "AutoNick";
	
	public static ChatFilter getInstance() {
		return instance;
	}
	
	@Override
	public void onEnable() {
		try {
			loadConfiguration();
			if(!getConfig().contains("MySQL.hostname") || getConfig().getString("MySQL.hostname").equals("null")) {
				set("MySQL.hostname", "null");
				set("MySQL.port", "null");
				set("MySQL.username", "null");
				set("MySQL.password", "null");
				getServer().getLogger().info("Bitte gib Deine MySQL-Daten in der Config ein!");
				getServer().shutdown();
				return;
			} else {
				MySQL = new MySQL(getConfig().getString("MySQL.hostname"),
								  getConfig().getString("MySQL.port"),
								  DATABASE,
								  getConfig().getString("MySQL.username"),
								  getConfig().getString("MySQL.password"));
				c = MySQL.openConnection();
				Statement s = c.createStatement();
				s.executeUpdate("CREATE TABLE IF NOT EXISTS " + TABLE_WOERTER + " (ID INT AUTO_INCREMENT PRIMARY KEY, wort VARCHAR(64));");
				s.executeUpdate("CREATE TABLE IF NOT EXISTS " + TABLE_USER + " (UUID VARCHAR(40) PRIMARY KEY, anzahl INT);");
			}
			try {
				if(Bukkit.getServer() instanceof CraftServer) {
					final Field f = CraftServer.class.getDeclaredField("commandMap");
					f.setAccessible(true);
					cmap = (CommandMap) f.get(Bukkit.getServer());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			ChatFilterCmd cmd_cf = new ChatFilterCmd(this);
			cmap.register("", cmd_cf);
			cmd_cf.setExecutor(new ChatFilterCmdExec(this));
			String[] boeseWoerterArray = new String[] {
					"scheiße", "fick dich", "shit", "fuck"
			};
			Statement s = c.createStatement();
			for(String wort : boeseWoerterArray) {
				if(!isMySQLsetWort(s, wort)) {
					s.executeUpdate("INSERT INTO " + TABLE_WOERTER + " (wort) VALUES ('" + wort + "');");
				}
			}
			boeseWoerter = new ArrayList<String>();
			for(int i = 1; i <= Integer.MAX_VALUE; i++) {
				if(isMySQLsetID(s, i)) {
					ResultSet rs = s.executeQuery("SELECT * FROM " + TABLE_WOERTER + " WHERE ID = '" + i + "';");
					rs.next();
					boeseWoerter.add(rs.getString("wort"));
				} else {
					break;
				}
			}
			getServer().getLogger().info("Erfolgreich " + boeseWoerter.size() + " Schimpfwoerter aus der Datenbank geladen!");
			getServer().getPluginManager().registerEvents(this, this);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDisable() {
		
	}
	
	public void set(String key, Object value) {
		getConfig().set(key, value);
		saveConfig();
	}
	
	public void loadConfiguration() {
		getConfig().options().copyDefaults(true);
		saveConfig();
	}
	
	public void simplePardon(String name) {
		getServer().getBanList(Type.NAME).pardon(name);
	}
	
	public void simpleBan(String name, String reason) {
		getServer().getBanList(Type.NAME).addBan(name, reason, null, null);
	}
	
	public static File getPluginPath() {
		return path;
	}
	
	public static File getDataPath() {
		return dataPath;
	}
	
	public static boolean isMySQLsetWort(Statement s, String string) throws SQLException {
		ResultSet rs = s.executeQuery("SELECT * FROM " + TABLE_WOERTER + " WHERE wort = '" + string + "';");
		return rs.next();
	}
	
	public static boolean isMySQLsetID(Statement s, int integer) throws SQLException {
		ResultSet rs = s.executeQuery("SELECT * FROM " + TABLE_WOERTER + " WHERE ID = '" + integer + "';");
		return rs.next();
	}
	
	public static boolean isMySQLsetPlayer(Statement s, String UUID) throws SQLException {
		ResultSet rs = s.executeQuery("SELECT * FROM " + TABLE_USER + " WHERE UUID = '" + UUID + "';");
		return rs.next();
	}
	
	public static String getPrefix() {
		return ChatColor.GRAY + "[" + ChatColor.GOLD + "ChatFilter" + ChatColor.GRAY + "] ";
	}
	
	public static double round(double value, int decimal) {
	    return (double) Math.round(value * Math.pow(10d, decimal)) / Math.pow(10d, decimal);
	}
	
	@EventHandler
	public void playerInitialize(PlayerJoinEvent e) {
		try {
			Statement s = c.createStatement();
			if(!isMySQLsetPlayer(s, e.getPlayer().getUniqueId().toString())) {
				s.executeUpdate("INSERT INTO " + TABLE_USER + " (UUID, anzahl) VALUES ('" + e.getPlayer().getUniqueId().toString() + "', '0');");
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	
	@EventHandler
	public void boesesWortGesagt(AsyncPlayerChatEvent e) {
		if(e.isAsynchronous()) {
			String newmessage = e.getMessage();
			for(String boesesWort : boeseWoerter) {
				if(StringUtils.containsIgnoreCase(e.getMessage().replace(" ", ""), boesesWort)) {
					newmessage = replaceIgnoreCaseAndSpaces(newmessage, boesesWort, "****");
				}
			}
			if(!newmessage.equals(e.getMessage())) {
				try {
					Statement s = c.createStatement();
					s.executeUpdate("UPDATE " + TABLE_USER + " SET anzahl = anzahl + 1 WHERE UUID = '" + e.getPlayer().getUniqueId().toString() + "' ORDER BY anzahl DESC;");
					ResultSet rs = s.executeQuery("SELECT * FROM " + TABLE_USER + " WHERE UUID ='" + e.getPlayer().getUniqueId().toString() + "';");
					if(rs.next()) {
						int anzahl = rs.getInt("anzahl");
						if(anzahl >= 3) getServer().dispatchCommand(getServer().getConsoleSender(), "tempmute " + e.getPlayer().getName() + " " + (anzahl * 10) + " m Chatverhalten");
					}
					e.getPlayer().sendMessage(ChatColor.DARK_RED + "Keine Schimpfwörter hier!!!");
				} catch(SQLException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	public static String replaceIgnoreCaseAndSpaces(String string, String target, String replacement) {
		String pattern = "";
		for(int i = 0; i < target.length(); i++) {
			pattern = pattern + target.charAt(i) + " *?";
		}
		pattern = pattern.substring(0, pattern.length() - 3);
		return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(string).replaceAll(replacement);
	}
	
}