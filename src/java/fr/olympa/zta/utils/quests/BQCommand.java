package fr.olympa.zta.utils.quests;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.zta.OlympaZTA;
import fr.skytasul.quests.gui.quests.PlayerListGUI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;

public class BQCommand extends OlympaCommand {
	
	public BQCommand() {
		super(OlympaZTA.getInstance(), "missions", "Ouvre l'inventaire des missions.", (OlympaSpigotPermission) null, "mission");
		setAllowConsole(false);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		PlayerAccount account = PlayersManager.getPlayerAccount(getPlayer());
		if (account == null) {
			sendError("Il n'y a pas de données de missions pour toi. Attends un peu avant de refaire la commande.");
		}else new PlayerListGUI(account).create(getPlayer());
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
}
