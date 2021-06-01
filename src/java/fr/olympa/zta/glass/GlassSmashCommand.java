package fr.olympa.zta.glass;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;

public class GlassSmashCommand extends OlympaCommand {
	
	private GlassSmashManager glass;
	
	public GlassSmashCommand(GlassSmashManager glass) {
		super(OlympaZTA.getInstance(), "glassSmash", "Active/désactive le cassage des vitres.", ZTAPermissions.GLASS_MANAGE_COMMAND);
		this.glass = glass;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		sendSuccess("§eLe système de cassage des vitres est désormais %s§e.", glass.toggle() ? "§aactif" : "§cinactif");
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
}
