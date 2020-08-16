package fr.olympa.zta.utils.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.zta.ZTAPermissions;

public class FeedCommand extends OlympaCommand {

	public FeedCommand(Plugin plugin) {
		super(plugin, "feed", "Restaure entièrement la barre de faim de l'utilisateur.", ZTAPermissions.MOD_COMMANDS);
		setAllowConsole(false);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		getPlayer().setFoodLevel(20);
		getPlayer().setSaturation(20);
		sendSuccess("Ta barre de faim a été restautée et sa saturation est au maximum.");
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
