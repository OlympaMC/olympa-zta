package fr.olympa.zta.enderchest;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;

public class EnderChestCommand extends OlympaCommand {

	public EnderChestCommand() {
		super(OlympaZTA.getInstance(), "enderchest", "Ouvre l'enderchest", ZTAPermissions.ENDERCHEST_COMMAND, "ec", "enderc", "echest");
		setAllowConsole(false);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		new EnderChestGUI(AccountProvider.<OlympaPlayerZTA>get(getPlayer())).create(getPlayer());
		return true;
	}

	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return Collections.EMPTY_LIST;
	}

}
