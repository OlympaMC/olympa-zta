package fr.olympa.zta.utils.map;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaZTA;

public class DynmapCommand extends OlympaCommand {
	
	public DynmapCommand() {
		super(OlympaZTA.getInstance(), "map", "Donne le lien de la carte virtuelle.", (OlympaSpigotPermission) null, "carte", "dynmap");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		sendHoverAndURL(Prefix.DEFAULT_GOOD, "La carte virtuelle est accessible à l'adresse §l§nwww.olympa.fr/zta", "§eClique pour l'ouvrir...", "https://olympa.fr/zta");
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
}
