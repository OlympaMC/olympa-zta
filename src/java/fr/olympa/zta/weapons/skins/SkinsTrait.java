package fr.olympa.zta.weapons.skins;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;

import fr.olympa.api.spigot.holograms.Hologram.HologramLine;
import fr.olympa.api.spigot.lines.AbstractLine;
import fr.olympa.api.spigot.lines.BlinkingLine;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.utils.npcs.HologramTrait;
import net.citizensnpcs.api.event.NPCRightClickEvent;

public class SkinsTrait extends HologramTrait {
	
	private AbstractLine<HologramLine>[] lines =
			new AbstractLine[] {
					new BlinkingLine<>((color, x) -> color + "§lSkins d'armes", OlympaZTA.getInstance(), 60, ChatColor.YELLOW, ChatColor.GOLD),
			};
	
	public SkinsTrait() {
		super("skins");
	}
	
	@Override
	protected AbstractLine<HologramLine>[] getLines() {
		return lines;
	}
	
	@EventHandler
	public void onNPCRightClick(NPCRightClickEvent e) {
		if (e.getNPC() != npc) return;
		new SkinsItemsGUI().create(e.getClicker());
	}
	
}
