package fr.olympa.zta.primes;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;

import fr.olympa.api.holograms.Hologram.HologramLine;
import fr.olympa.api.lines.AbstractLine;
import fr.olympa.api.lines.BlinkingLine;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.utils.Utils;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.utils.npcs.HologramTrait;
import net.citizensnpcs.api.event.NPCRightClickEvent;

public class BountyTrait extends HologramTrait {
	
	private AbstractLine<HologramLine>[] lines =
			new AbstractLine[] {
					new BlinkingLine<>((color, x) -> color + "§l" + Utils.withOrWithoutS(OlympaZTA.getInstance().primes.getPrimes().size(), "vente"), OlympaZTA.getInstance(), 60, ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE),
					FixedLine.EMPTY_LINE,
					new FixedLine<>("§d§nChasseur de Primes") };
	
	public BountyTrait() {
		super("bountyMan");
	}
	
	@Override
	protected AbstractLine<HologramLine>[] getLines() {
		return lines;
	}
	
	@EventHandler
	public void onNPCRightClick(NPCRightClickEvent e) {
		if (e.getNPC() != npc) return;
		OlympaZTA.getInstance().primes.openPrimesGUI(e.getClicker());
	}
	
}
