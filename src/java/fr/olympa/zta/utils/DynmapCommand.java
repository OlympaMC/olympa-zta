package fr.olympa.zta.utils;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.zta.OlympaZTA;

public class DynmapCommand extends OlympaCommand {
	
	public DynmapCommand() {
		super(OlympaZTA.getInstance(), "map", "carte");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		sendSuccess("La carte virtuelle est accessible à l'adresse §l§nwww.olympa.fr:8124§a !");
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
}
