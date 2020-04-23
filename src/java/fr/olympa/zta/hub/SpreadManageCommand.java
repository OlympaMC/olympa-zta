package fr.olympa.zta.hub;

import java.util.stream.Collectors;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import fr.olympa.zta.mobs.MobSpawning.SpawnType;

public class SpreadManageCommand extends ComplexCommand {

	public SpreadManageCommand() {
		super(OlympaZTA.getInstance(), "spreadManage", "Permet de gérer la dispersion des joueurs.", ZTAPermissions.PLAYER_SPREAD_COMMAND);
	}

	@Cmd (min = 1, args = "INTEGER", syntax = "<distance minimale>")
	public void minDistance(CommandContext cmd) {
		OlympaZTA.getInstance().hub.minDistance = (int) cmd.args[0];
		sendSuccess("Vous avez modifié la distance de spawn par défaut entre deux joueurs.");
	}

	@Cmd (min = 1, args = "SAFE|EASY|MEDIUM|HARD", syntax = "<région>")
	public void addRegion(CommandContext cmd) {
		OlympaZTA.getInstance().hub.addSpawnRegion(SpawnType.valueOf((String) cmd.args[0]));
		sendSuccess("Vous avez ajouté une région de spawn.");
	}

	@Cmd (min = 1, args = "SAFE|EASY|MEDIUM|HARD", syntax = "<région>")
	public void removeRegion(CommandContext cmd) {
		OlympaZTA.getInstance().hub.removeSpawnRegion(SpawnType.valueOf((String) cmd.args[0]));
		sendSuccess("Vous avez retiré une région de spawn.");
	}

	@Cmd
	public void info(CommandContext cmd) {
		sendInfo("Distance entre les joueurs : §l" + OlympaZTA.getInstance().hub.minDistance);
		sendInfo("Régions de spawn : §l" + OlympaZTA.getInstance().hub.getSpawnRegions().stream().map(x -> x.name()).collect(Collectors.joining(", ", "[", "]")));
	}

}
