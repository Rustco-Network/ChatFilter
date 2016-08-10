package de.thexxturboxx.chatfilter;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ChatFilterCmd extends Command {
	
	ChatFilter plugin;
	CommandExecutor exe = null;
	
	public ChatFilterCmd(ChatFilter plugin) {
		super("chatfilter", "ChatFilter-Hauptcommand", "/chatfilter", Arrays.asList(new String[]{}));
		this.plugin = plugin;
	}
	
	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if(exe != null) {
			return exe.onCommand(sender, this, commandLabel, args);
        }
        return false;
	}
	
	public void setExecutor(CommandExecutor exe){
        this.exe = exe;
    }
	
}