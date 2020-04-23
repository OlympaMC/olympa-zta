package fr.olympa.zta.hub;

import org.bukkit.command.CommandSender;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;

public class HubCommand extends ComplexCommand {

	public HubCommand() {
		super(null, OlympaZTA.getInstance(), "spawn", "Vous téléporte au spawn.", ZTAPermissions.HUB_COMMAND, "hub");
		super.noArgs = this::noArgsMethod;
	}

	public boolean noArgsMethod(CommandSender sender) {
		if (player == null) return false;
		OlympaZTA.getInstance().hub.teleport(player);
		return true;
	}

	@Cmd (min = 1, args = "INTEGER", syntax = "<distance minimale>", permissionName = "HUB_EDIT_DISTANCE_COMMAND")
	public void minDistance(CommandContext cmd) {
		OlympaZTA.getInstance().hub.minDistance = (int) cmd.args[0];
		sendSuccess("Vous avez modifié la distance de spawn par défaut entre deux joueurs.");
	}

}
