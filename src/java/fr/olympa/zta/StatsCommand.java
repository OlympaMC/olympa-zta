package fr.olympa.zta;

import java.text.DecimalFormat;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;

public class StatsCommand extends OlympaCommand {
	
	private final DecimalFormat ratioFormat = new DecimalFormat("00.#");
	
	public StatsCommand(Plugin plugin) {
		super(plugin, "stats");
		super.description = "Affiche les statistiques de son personnage ZTA.";
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		OlympaPlayerZTA olympaPlayer = getOlympaPlayer();
		sendSuccess("Nombre de morts: §e%d", olympaPlayer.deaths.get());
		sendSuccess("Joueurs tués: §e%d", olympaPlayer.killedPlayers.get());
		sendSuccess("Zombies tués: §e%d", olympaPlayer.killedZombies.get());
		double head = olympaPlayer.headshots.get();
		double other = olympaPlayer.otherShots.get();
		sendSuccess("Headshots: %s%%", head == 0 && other == 0 ? "§cx" : "§e" + ratioFormat.format(head / (other + head) * 100));
		sendSuccess("Coffres ouverts: §e%d", olympaPlayer.openedChests.get());
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
}
