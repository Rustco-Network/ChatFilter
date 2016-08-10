package de.thexxturboxx.chatfilter;

import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatFilterCmdExec implements CommandExecutor {
	
	ChatFilter plugin;
	
	public ChatFilterCmdExec(ChatFilter plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			if(p.hasPermission("chatfilter.cmd.cf") || p.isOp()) {
				if(args.length >= 1) {
					if(args.length == 1 && args[0].equalsIgnoreCase("count")) {
						p.sendMessage(ChatFilter.getPrefix() + ChatColor.GREEN + "Derzeit sind " + ChatColor.GOLD + ChatFilter.boeseWoerter.size() + ChatColor.GREEN + " Schimpfwörter in der Datenbank!");
						return true;
					}
					String wort = addWord(args);
					if(wort != null)
						p.sendMessage(ChatFilter.getPrefix() + ChatColor.GREEN + "Das Wort " + wort + " wurde erfolgreich zur Datenbank hinzugefügt!");
					else {
						p.sendMessage(ChatFilter.getPrefix() + ChatColor.DARK_RED + "Es gab ein Problem beim Hinzufügen des Wortes zur Datenbank.");
						p.sendMessage(ChatFilter.getPrefix() + ChatColor.DARK_RED + "Schau am besten in der Konsole nach Fehlern nach!");
					}
				} else {
					p.sendMessage(ChatColor.AQUA + "===============(::)===============");
					p.sendMessage(ChatColor.GOLD + "            ChatFilter            ");
					p.sendMessage(ChatColor.GOLD + "              v1.0.0              ");
					p.sendMessage(ChatColor.GOLD + "     Plugin von: ThexXTURBOXx     ");
					p.sendMessage(ChatColor.GOLD + "           und Sensetox           ");
					p.sendMessage(ChatColor.AQUA + "===============(::)===============");
				}
			} else {
				p.sendMessage(ChatFilter.getPrefix() + ChatColor.DARK_RED + "Dazu hast du keine Erlaubnis!");
			}
		} else {
			if(args.length >= 1) {
				if(args.length == 1 && args[0].equalsIgnoreCase("count")) {
					plugin.getServer().getLogger().info("[ChatFilter] Derzeit sind " + ChatFilter.boeseWoerter.size() + " Schimpfwoerter in der Datenbank!");
					return true;
				}
				String wort = addWord(args);
				if(wort != null)
					plugin.getServer().getLogger().info("[ChatFilter] Das Wort " + wort + " wurde erfolgreich zur Datenbank hinzugefuegt!");
				else {
					plugin.getServer().getLogger().info("[ChatFilter] Es gab ein Problem beim Hinzufuegen des Wortes zur Datenbank.");
				}
			} else {
				plugin.getServer().getLogger().info("===============(::)===============");
				plugin.getServer().getLogger().info("            ChatFilter            ");
				plugin.getServer().getLogger().info("              v1.0.0              ");
				plugin.getServer().getLogger().info("     Plugin von: ThexXTURBOXx     ");
				plugin.getServer().getLogger().info("           und Sensetox           ");
				plugin.getServer().getLogger().info("===============(::)===============");
			}
		}
		return true;
	}
	
	private String addWord(String[] args) {
		String wort = "";
		for(String s : args) {
			if(!wort.equals("")) {
				wort = wort + " ";
			}
			wort = wort + s;
		}
		wort = wort.toLowerCase();
		try {
			ChatFilter.c.createStatement().executeUpdate("INSERT INTO " + ChatFilter.TABLE_WOERTER + " (wort) VALUES ('" + wort + "');");
			ChatFilter.boeseWoerter.add(wort);
			return wort;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}