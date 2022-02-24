package fr.olympa.zta.weapons;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;

public class WeaponsSeeCommand extends OlympaCommand {
	
	public WeaponsSeeCommand() {
		super(OlympaZTA.getInstance(), "armes", "Commande pour voir les armes.", ZTAPermissions.WEAPONS_COMMAND);
		setAllowConsole(false);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		new WeaponsGiveView(false).toGUI().create(player);
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
}
