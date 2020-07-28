package fr.olympa.zta;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;

public class StatsCommand extends OlympaCommand {
	
	public StatsCommand(Plugin plugin) {
		super(plugin, "stats");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		OlympaPlayerZTA olympaPlayer = getOlympaPlayer();
		sendSuccess("Nombre de morts: §e%d", olympaPlayer.deaths.get());
		sendSuccess("Joueurs tués: §e%d", olympaPlayer.killedPlayers.get());
		sendSuccess("Zombies tués: §e%d", olympaPlayer.killedZombies.get());
		sendSuccess("Headshots effectués: §e%d", olympaPlayer.headshots.get());
		sendSuccess("Coffres ouverts: §e%d", olympaPlayer.openedChests.get());
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
}
