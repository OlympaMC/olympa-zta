package fr.olympa.zta.mobs;

import org.bukkit.entity.Zombie;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.utils.Utils;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import fr.olympa.zta.mobs.MobSpawning.SpawnType;

public class MobsCommand extends ComplexCommand {

	public MobsCommand() {
		super(OlympaZTA.getInstance(), "mobs", "gestion des mobs", ZTAPermissions.MOBS_COMMAND);
	}

	@Cmd
	public void info(CommandContext cmd) {
		MobSpawning spawning = OlympaZTA.getInstance().mobSpawning;
		sendInfo("Le spawn de mob est §l" + (spawning.isEnabled() ? "§aactif" : "§cinactif"));
		if (spawning.isEnabled()) {
			sendInfo("Nombre de mobs moyen dans la queue de spawn : §l" + Utils.formatDouble(spawning.getAverageQueueSize(), 2));
		}
		sendInfo("Taille du tableau des inventaires : §l" + MobsListener.inventories.size());
		sendInfo("Nombre d'entités vivantes sur le monde principal : §l" + spawning.world.getLivingEntities().size());
		sendInfo("Quantité maximale d'entités sur le monde : §l" + spawning.maxEntities);
		sendInfo("Quantité maximale d'entités par chunk : §l" + spawning.criticalEntitiesPerChunk);
		if (player != null) sendInfo("Vous êtes actuellement dans une zone de spawn : §l" + SpawnType.getSpawnType(player.getLocation().getChunk()));
	}

	@Cmd (player = true)
	public void spawnZombie(CommandContext cmd) {
		Mobs.spawnCommonZombie(getPlayer().getLocation());
	}

	@Cmd (args = "kill|remove", min = 0, syntax = "<action>")
	public void removeZombies(CommandContext cmd) {
		boolean kill = cmd.args.length == 0 ? false : cmd.getArgument(0).equals("kill");
		int amount = 0;
		for (Zombie zombie : OlympaZTA.getInstance().mobSpawning.world.getEntitiesByClass(Zombie.class)) {
			if (kill) {
				zombie.damage(1000000);
			}else zombie.remove();
			amount++;
		}
		sendSuccess(amount + " zombies " + (kill ? "tués." : "supprimés."));
	}

	@Cmd
	public void toggleSpawning(CommandContext cmd) {
		MobSpawning spawning = OlympaZTA.getInstance().mobSpawning;
		String state;
		if (spawning.isEnabled()) {
			spawning.end();
			state = "désactivé";
		}else {
			spawning.start();
			state = "activé";
		}
		sendSuccess("Le spawn naturel des mobs a été §l" + state + "§r§a.");
	}

	@Cmd (min = 1, args = "INTEGER")
	public void setMaximumWorldEntities(CommandContext cmd) {
		OlympaZTA.getInstance().mobSpawning.maxEntities = cmd.getArgument(0);
		sendSuccess("Vous avez modifié la quantité maximale d'entités sur le monde.");
	}

	@Cmd (min = 1, args = "INTEGER")
	public void setMaximumChunkEntities(CommandContext cmd) {
		OlympaZTA.getInstance().mobSpawning.criticalEntitiesPerChunk = cmd.getArgument(0);
		sendSuccess("Vous avez modifié la quantité maximale d'entités par chunk.");
	}

}
