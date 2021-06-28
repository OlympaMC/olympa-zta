package fr.olympa.zta.mobs;

import java.util.Collection;
import java.util.stream.Collectors;

import org.bukkit.entity.Zombie;

import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.spigot.command.ComplexCommand;
import fr.olympa.api.utils.Utils;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import fr.olympa.zta.mobs.MobSpawning.SpawnType;
import fr.olympa.zta.mobs.custom.Mobs;
import fr.olympa.zta.mobs.custom.Mobs.Zombies;

public class MobsCommand extends ComplexCommand {

	public MobsCommand() {
		super(OlympaZTA.getInstance(), "mobs", "Gère les mobs du ZTA.", ZTAPermissions.MOBS_COMMAND);
		super.addArgumentParser("MOBTYPE", Zombies.class);
	}

	@Cmd
	public void info(CommandContext cmd) {
		MobSpawning spawning = OlympaZTA.getInstance().mobSpawning;
		sendInfo("Le spawn de mob est §l%s", spawning.isEnabled() ? "§aactif" : "§cinactif");
		if (spawning.isEnabled()) {
			sendInfo("§7- Nombre de mobs moyen dans la §equeue de spawn : §l%s", Utils.formatDouble(spawning.getAverageQueueSize(), 2));
			sendInfo("§7- Durée de §ecalcul des spawn de la dernière minute (en ms) : §l%s", spawning.getLastComputeTimes().stream().map(String::valueOf).collect(Collectors.joining(", ", "[", "]")));
			sendInfo("§7- Dernière durée de §ecalcul des chunks actifs (%d) : §l%d ms", spawning.lastActiveChunks, spawning.timeActiveChunks);
			sendInfo("§7- Nombre de §ezombies spawnés à la dernière task : §l%d", spawning.lastSpawnedMobs);
		}
		sendInfo("§7Nombre d'§eentités vivantes sur le monde principal : §l%s", spawning.getEntityCount());
		sendInfo("§7Quantité maximale d'§eentités sur le monde/le chunk : §l%d/%d", spawning.maxEntities, spawning.criticalEntitiesPerChunk);
		sendInfo("§7Ticks entre deux spawn de mobs/secondes entre deux calculs : §l%d/%d", spawning.spawnTicks, spawning.calculationMillis / 1000);
		if (player != null) sendInfo("§7Vous êtes actuellement dans une §ezone de spawn : §l%s", SpawnType.getSpawnType(player.getWorld(), player.getLocation().getBlockX(), player.getLocation().getBlockZ()));
	}

	@Cmd (player = true, args = { "MOBTYPE", "INTEGER" }, min = 0, syntax = "[type] [quantité]")
	public void spawnZombie(CommandContext cmd) {
		Zombies zombieType = cmd.getArgument(0, Zombies.COMMON);
		int amount = cmd.getArgument(1, 1);
		for (int i = 0; i < amount; i++) {
			Mobs.spawnCommonZombie(zombieType, getPlayer().getLocation());
		}
		sendSuccess("Vous avez fait spawn %d zombie de type %s.", amount, zombieType.name());
	}

	@Cmd (args = { "kill|remove", "DOUBLE" }, min = 0, syntax = "[action] [rayon]")
	public void killZombies(CommandContext cmd) {
		boolean remove = cmd.getArgumentsLength() != 0 && cmd.getArgument(0).equals("remove");
		double radius = cmd.getArgument(1, 0D);
		if (radius < 0) {
			sendIncorrectSyntax();
			return;
		}
		
		Collection<Zombie> zombies;
		if (radius > 0) {
			if (player == null) {
				sendImpossibleWithConsole();
				return;
			}
			zombies = player.getNearbyEntities(radius, radius, radius).stream().filter(x -> x instanceof Zombie).map(Zombie.class::cast).collect(Collectors.toList());
		}else zombies = OlympaZTA.getInstance().mobSpawning.world.getEntitiesByClass(Zombie.class);
		int amount = 0;
		for (Zombie zombie : zombies) {
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
			state = "§c§ldésactivé";
		}else {
			if (!spawning.start()) {
				sendError("Le spawn naturel des mobs n'a pas pu démarrer.");
				return;
			}
			state = "§a§lactivé";
		}
		sendSuccess("§eLe spawn naturel des mobs a été " + state + "§e.");
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
	
	@Cmd (min = 1, args = "INTEGER")
	public void setSpawnTicks(CommandContext cmd) {
		OlympaZTA.getInstance().mobSpawning.setSpawnTicks(cmd.getArgument(0));
		sendSuccess("Vous avez modifié l'intervalle de spawn.");
	}

}
