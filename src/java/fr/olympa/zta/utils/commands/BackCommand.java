package fr.olympa.zta.utils.commands;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.zta.ZTAPermissions;

public class BackCommand extends OlympaCommand {

	public BackCommand(Plugin plugin) {
		super(plugin, "back", ZTAPermissions.MOD_COMMANDS);
		setAllowConsole(false);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		List<MetadataValue> metadata = getPlayer().getMetadata("lastDeath");
		if (metadata.isEmpty()) {
			sendError("Tu n'es pas mort dernièrement.");
		}else {
			getPlayer().teleport((Location) metadata.get(0).value());
			sendSuccess("Tu as été téléporté sur le lieu de ta mort.");
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
