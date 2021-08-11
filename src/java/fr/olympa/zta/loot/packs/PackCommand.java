package fr.olympa.zta.loot.packs;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.ZTAPermissions;

public class PackCommand extends OlympaCommand {
	
	public PackCommand(Plugin plugin) {
		super(plugin, "packs", "Donne des packs au joueur.", ZTAPermissions.PACKS_COMMAND);
		
		minArg = 2;
		usageString = "/packs <player> <pack name>";
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player bplayer = Bukkit.getPlayerExact(args[0]);
		if (bplayer == null) {
			sendUnknownPlayer(args[0]);
			return false;
		}
		OlympaPlayerZTA player = AccountProviderAPI.getter().get(bplayer.getUniqueId());
		if (player == null) {
			sendImpossibleWithOlympaPlayer();
			return false;
		}
		try {
			PackType type = PackType.valueOf(args[1]);
			player.packs.givePack(type);
		}catch (IllegalArgumentException ex) {
			sendError("Pack %s invalide", args[1]);
		}
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
}
