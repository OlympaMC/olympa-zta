package fr.olympa.zta;

import java.text.DecimalFormat;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;

public class StatsCommand extends OlympaCommand {
	
	private final DecimalFormat ratioFormat = new DecimalFormat("0.00");
	
	public StatsCommand(Plugin plugin) {
		super(plugin, "stats");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		OlympaPlayerZTA olympaPlayer = getOlympaPlayer();
		sendSuccess("Nombre de morts: §e%d", olympaPlayer.deaths.get());
		sendSuccess("Joueurs tués: §e%d", olympaPlayer.killedPlayers.get());
		sendSuccess("Zombies tués: §e%d", olympaPlayer.killedZombies.get());
		double head = olympaPlayer.headshots.get();
		double other = olympaPlayer.otherShots.get();
		sendSuccess("Ratio de headshots: §e%s", ratioFormat.format(olympaPlayer.otherShots.get() == 0 ? head : head / (other + head)));
		sendSuccess("Coffres ouverts: §e%d", olympaPlayer.openedChests.get());
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
}
