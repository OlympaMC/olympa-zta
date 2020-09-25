package fr.olympa.zta.mobs;

import java.util.stream.Collectors;

import org.bukkit.entity.Zombie;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.utils.Utils;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import fr.olympa.zta.mobs.MobSpawning.SpawnType;
import fr.olympa.zta.mobs.custom.Mobs;
import fr.olympa.zta.mobs.custom.Mobs.Zombies;

public class MobsCommand extends ComplexCommand {

	public MobsCommand() {
		super(OlympaZTA.getInstance(), "mobs", "Gestion des mobs", ZTAPermissions.MOBS_COMMAND);
		super.addArgumentParser("MOBTYPE", Zombies.class);
	}

	@Cmd
	public void info(CommandContext cmd) {
		MobSpawning spawning = OlympaZTA.getInstance().mobSpawning;
		sendInfo("Le spawn de mob est §l%s", spawning.isEnabled() ? "§aactif" : "§cinactif");
		if (spawning.isEnabled()) {
			sendInfo("Nombre de mobs moyen dans la queue de spawn : §l%s", Utils.formatDouble(spawning.getAverageQueueSize(), 2));
			sendInfo("Durée de calcul des spawn de la dernière minute (en ms) : §l%s", spawning.getLastComputeTimes().stream().sorted().map(String::valueOf).collect(Collectors.joining(", ", "[", "]")));
		}
		sendInfo("Taille du tableau des inventaires : §l%d", OlympaZTA.getInstance().mobsListener.inventories.size());
		sendInfo("Nombre d'entités vivantes sur le monde principal : §l%s", spawning.getEntityCount());
		sendInfo("Quantité maximale d'entités sur le monde : §l%d", spawning.maxEntities);
		sendInfo("Quantité maximale d'entités par chunk : §l%d", spawning.criticalEntitiesPerChunk);
		if (player != null) sendInfo("Vous êtes actuellement dans une zone de spawn : §l%s", SpawnType.getSpawnType(player.getWorld(), player.getLocation().getBlockX(), player.getLocation().getBlockZ()));
	}

	@Cmd (player = true, args = "MOBTYPE", min = 0, syntax = "[type]")
	public void spawnZombie(CommandContext cmd) {
		Mobs.spawnCommonZombie(cmd.getArgument(0, Zombies.COMMON), getPlayer().getLocation());
	}

	@Cmd (args = "kill|remove", min = 0, syntax = "<action>")
	public void killZombies(CommandContext cmd) {
		boolean remove = cmd.getArgumentsLength() == 0 ? false : cmd.getArgument(0).equals("remove");
		int amount = 0;
		for (Zombie zombie : OlympaZTA.getInstance().mobSpawning.world.getEntitiesByClass(Zombie.class)) {
			if (remove) {
				zombie.remove();
			}else zombie.damage(1000000);
			amount++;
		}
		sendSuccess(amount + " zombies " + (remove ? "supprimés." : "tués."));
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
