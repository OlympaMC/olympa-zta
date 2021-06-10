package fr.olympa.zta;

import java.text.DecimalFormat;
import java.util.List;
import java.util.StringJoiner;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.utils.Prefix;

public class StatsCommand extends OlympaCommand {
	
	private final DecimalFormat percentageFormat = new DecimalFormat("00.##");
	private final DecimalFormat ratioFormat = new DecimalFormat("0.##");
	
	public StatsCommand(Plugin plugin) {
		super(plugin, "stats");
		super.description = "Affiche les statistiques de son personnage ZTA.";
		addArgs(false, "JOUEUR");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		OlympaPlayerZTA olympaPlayer;
		if (args.length == 0) {
			if (player == null) {
				sendImpossibleWithConsole();
				return false;
			}
			olympaPlayer = getOlympaPlayer();
		}else {
			Player targetPlayer = Bukkit.getPlayer(args[0]);
			if (targetPlayer == null) {
				sendUnknownPlayer(args[0]);
				return false;
			}
			olympaPlayer = OlympaPlayerZTA.get(targetPlayer);
		}
		StringJoiner joiner = new StringJoiner("\n§7➤ §a");
		joiner.add(Prefix.DEFAULT_GOOD.formatMessage("Statistiques de §l%s§a - ZTA", olympaPlayer.getName()));
		joiner.add("Nombre de morts: §e" + olympaPlayer.deaths.get());
		joiner.add("Joueurs tués: §e" + olympaPlayer.killedPlayers.get());
		joiner.add("Zombies tués: §e" + olympaPlayer.killedZombies.get());
		joiner.add("Ratio: " + (olympaPlayer.deaths.get() == 0 ? "§cx" : "§e" + ratioFormat.format(olympaPlayer.killedPlayers.getAsDouble() / olympaPlayer.deaths.getAsDouble())));
		double head = olympaPlayer.headshots.get();
		double other = olympaPlayer.otherShots.get();
		joiner.add("Headshots: " + (head == 0 && other == 0 ? "§cx" : "§e" + percentageFormat.format(head / (other + head) * 100) + "%"));
		joiner.add("Coffres ouverts: §e" + olympaPlayer.openedChests.get());
		sender.sendMessage(joiner.toString());
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
}
