package fr.olympa.zta.settings;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.zta.OlympaZTA;

public class PlayerSettingsCommand extends OlympaCommand {
	
	public PlayerSettingsCommand() {
		super(OlympaZTA.getInstance(), "settings", "Accéder au menu des paramètres.", (OlympaSpigotPermission) null, "parametres");
		setAllowConsole(false);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		new PlayerSettingsGUI(getOlympaPlayer()).create(getPlayer());
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
}
