package fr.olympa.zta.enderchest;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;

public class EnderChestCommand extends OlympaCommand {

	public EnderChestCommand() {
		super(OlympaZTA.getInstance(), "enderchest", "Ouvre l'enderchest", ZTAPermissions.ENDERCHEST_COMMAND, "ec", "enderc", "echest");
		setAllowConsole(false);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player target;
		if (args.length == 1 && hasPermission(ZTAPermissions.ENDERCHEST_COMMAND_OTHER)) {
			target = Bukkit.getPlayer(args[0]);
		}else target = player;
		player.openInventory(OlympaPlayerZTA.get(target).getEnderChest());
		return true;
	}

	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1 && hasPermission(ZTAPermissions.ENDERCHEST_COMMAND_OTHER)) {
			String arg = args[0].toLowerCase();
			return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase().startsWith(arg)).collect(Collectors.toList());
		}
		return Collections.EMPTY_LIST;
	}

}
