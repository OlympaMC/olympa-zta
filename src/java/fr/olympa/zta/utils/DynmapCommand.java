package fr.olympa.zta.utils;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.permission.OlympaSpigotPermission;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaZTA;

public class DynmapCommand extends OlympaCommand {
	
	public DynmapCommand() {
		super(OlympaZTA.getInstance(), "map", "Donne le lien de la carte virtuelle.", (OlympaSpigotPermission) null, "carte");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		sendHoverAndURL(Prefix.DEFAULT_GOOD, "La carte virtuelle est accessible à l'adresse §l§nwww.olympa.fr:8124§a !", "§eClique pour l'ouvrir...", "www.olympa.fr:8124");
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
}
