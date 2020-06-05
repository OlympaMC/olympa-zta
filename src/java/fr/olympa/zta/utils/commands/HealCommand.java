package fr.olympa.zta.utils.commands;

import java.util.List;

import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.zta.ZTAPermissions;

public class HealCommand extends OlympaCommand {

	public HealCommand(Plugin plugin) {
		super(plugin, "heal", ZTAPermissions.MOD_COMMANDS);
		setAllowConsole(false);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		getPlayer().setHealth(getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
		sendSuccess("Tu as été heal.");
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
