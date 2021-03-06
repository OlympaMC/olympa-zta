package fr.olympa.zta.hub;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;

public class HubCommand extends OlympaCommand {

	public HubCommand() {
		super(OlympaZTA.getInstance(), "spawn", "Téléporte au spawn du ZTA.", ZTAPermissions.HUB_COMMAND/*, "hub"*/);
		super.allowConsole = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		OlympaZTA.getInstance().hub.teleport(player);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
