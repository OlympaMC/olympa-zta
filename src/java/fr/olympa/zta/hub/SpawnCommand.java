package fr.olympa.zta.hub;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;

public class SpawnCommand extends OlympaCommand {

	public SpawnCommand() {
		super(OlympaZTA.getInstance(), "spawn", ZTAPermissions.SPAWN_COMMAND);
		super.allowConsole = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		OlympaZTA.getInstance().hub.teleport(getPlayer());
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return Collections.EMPTY_LIST;
	}

}
