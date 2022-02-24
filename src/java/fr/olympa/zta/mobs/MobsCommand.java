package fr.olympa.zta.mobs;

import java.util.Collection;
import java.util.stream.Collectors;

import org.bukkit.entity.Zombie;

import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.spigot.command.ComplexCommand;
import fr.olympa.api.spigot.lines.DynamicLine;
import fr.olympa.api.spigot.lines.TimerLine;
import fr.olympa.api.spigot.scoreboard.sign.Scoreboard;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import fr.olympa.zta.mobs.MobSpawning.SpawnType;
import fr.olympa.zta.mobs.custom.Mobs;
import fr.olympa.zta.mobs.custom.Mobs.Zombies;

public class MobsCommand extends ComplexCommand {

	private DynamicLine<Scoreboard<OlympaPlayerZTA>> mobTracker = new TimerLine<>(x -> {
		return "\n§7Zombies: §e" + OlympaZTA.getInstance().mobSpawning.world.getEntitiesByClass(Zombie.class).size();
	}, OlympaZTA.getInstance(), 20);
	
	public MobsCommand() {
		super(OlympaZTA.getInstance(), "mobs", "Gère les mobs du ZTA.", ZTAPermissions.MOBS_COMMAND);
		super.addArgumentParser("MOBTYPE", Zombies.class);
	}

	@Cmd
	public void info(CommandContext cmd) {
		MobSpawning spawning = OlympaZTA.getInstance().mobSpawning;
		TxtComponentBuilder builder = new TxtComponentBuilder(Prefix.DEFAULT_GOOD.formatMessage("Le spawn de mob est %s", spawning.isEnabled() ? "§a§lactif" : "§c§linactif"));
		builder.extraSpliter("\n§8➤ §7");
		if (spawning.isEnabled()) {
			builder.extra(new TxtComponentBuilder("§7- Queue de spawn.").onHoverText("§7Nombre de zombies moyen dans la queue de spawn: §a%s\n§7Nombre de zombies spawné lors de la dernière task: §a%d", Utils.formatDouble(spawning.getAverageQueueSize(), 2), spawning.lastSpawnedMobs));
			builder.extra(new TxtComponentBuilder("§7- Durées de calculs.").onHoverText("§7Calcul des spawn (sur 1min, en ms): §a%s\n§7Dernier calcul des chunks actifs: §a%d chunks§7, §a%d ms", spawning.getLastComputeTimes().stream().map(String::valueOf).collect(Collectors.joining(", ", "[", "]")), spawning.lastActiveChunks, spawning.timeActiveChunks));
		}
		builder.extra(new TxtComponentBuilder("§eConfiguration actuelle.").onHoverText("§eEntités:\n§7Quantité maximale sur le monde: §a%d\n§7Quantité maximale dans un chunk: §a%d\n\n§eDurées:\n§7Secondes entre les calculs: §a%d\n§7Ticks entre deux spawn de mobs: §a%d", spawning.maxEntities, spawning.criticalEntitiesPerChunk, spawning.calculationMillis / 1000, spawning.spawnTicks));
		builder.extra(new TxtComponentBuilder("§eNombre d'entités.").onHoverText("§e%s", spawning.getEntityCount()));
		if (player != null) builder.extra("§7Vous êtes dans une §ezone de spawn : §l%s", SpawnType.getSpawnType(player.getWorld(), player.getLocation().getBlockX(), player.getLocation().getBlockZ()));
		sendComponents(builder.build());
	}
	
	@Cmd (player = true)
	public void addScoreboardTracker(CommandContext cmd) {
		OlympaZTA.getInstance().scoreboards.getPlayerScoreboard(getOlympaPlayer()).addLines(mobTracker);
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

	@Cmd (min = 4, args = { "INTEGER", "INTEGER", "INTEGER", "INTEGER", "INTEGER" }, syntax = "<xMin> <zMin> <xMax> <zMax> [init %%]")
	public void mobsScan(CommandContext cmd) {
		new MapScan().start(sender, cmd.getArgument(0), cmd.getArgument(1), cmd.getArgument(2), cmd.getArgument(3), cmd.getArgument(4, 0));
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
