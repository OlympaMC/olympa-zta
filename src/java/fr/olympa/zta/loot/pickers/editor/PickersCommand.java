package fr.olympa.zta.loot.pickers.editor;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;

public class PickersCommand extends OlympaCommand {
	
	public PickersCommand() {
		super(OlympaZTA.getInstance(), "pickers", "Ouvre l'éditeur de pickers.", ZTAPermissions.PICKERS_COMMAND);
		setAllowConsole(false);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		new PickersListGUI().toGUI().create(player);
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
}
