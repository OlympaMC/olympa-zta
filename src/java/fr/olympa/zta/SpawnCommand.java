package fr.olympa.zta;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import fr.olympa.api.command.OlympaCommand;

public class SpawnCommand extends OlympaCommand {

	public SpawnCommand() {
		super(OlympaZTA.getInstance(), "spawn", ZTAPermissions.SPAWN_COMMAND);
		super.allowConsole = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		player.teleport(OlympaZTA.getInstance().spawn);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return Collections.EMPTY_LIST;
	}

}
