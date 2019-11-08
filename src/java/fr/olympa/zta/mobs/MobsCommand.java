package fr.olympa.zta.mobs;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;

public class MobsCommand extends ComplexCommand {

	public MobsCommand() {
		super(null, OlympaZTA.getInstance(), "mobs", "gestion des mobs", ZTAPermissions.MOBS_COMMAND);
	}

	@Cmd
	public void info(CommandContext cmd) {
		sendMessage(Prefix.INFO, "Nombre de mobs moyen dans la queue de spawn : " + Utils.formatDouble(OlympaZTA.getInstance().spawn.getAverageQueueSize(), 2));
	}

	@Cmd (player = true)
	public void spawnZombie(CommandContext cmd) {
		Mobs.spawnZombie(cmd.player.getLocation());
	}

}
