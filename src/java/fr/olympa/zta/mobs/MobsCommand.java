package fr.olympa.zta.mobs;

import org.bukkit.entity.Zombie;

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
		sendMessage(Prefix.INFO, "Taille du tableau des inventaires : " + MobsListener.inventories.size());
		sendMessage(Prefix.INFO, "Nombre d'entités vivantes sur le monde principal : " + OlympaZTA.getInstance().spawn.world.getLivingEntities().size());
	}

	@Cmd (player = true)
	public void spawnZombie(CommandContext cmd) {
		Mobs.spawnCommonZombie(cmd.player.getLocation());
	}

	@Cmd (args = "kill|remove", min = 0)
	public void removeZombies(CommandContext cmd) {
		boolean kill = cmd.args.length == 0 ? false : cmd.args[0].equals("kill");
		int amount = 0;
		for (Zombie zombie : OlympaZTA.getInstance().spawn.world.getEntitiesByClass(Zombie.class)) {
			if (kill) {
				zombie.damage(1000000);
			}else zombie.remove();
			amount++;
		}
		sendSuccess(amount + " zombies " + (kill ? "tués." : "supprimés."));
	}

}
