package fr.olympa.zta.loot.packs;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.ZTAPermissions;

public class PackCommand extends OlympaCommand {
	
	public PackCommand(Plugin plugin) {
		super(plugin, "packs", "Donne des packs au joueur", ZTAPermissions.PACKS_COMMAND);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
	public void givePack(OlympaPlayerZTA player) {
		
	}
	
}
