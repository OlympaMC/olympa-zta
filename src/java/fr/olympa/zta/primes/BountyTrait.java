package fr.olympa.zta.primes;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;

import fr.olympa.api.spigot.holograms.Hologram.HologramLine;
import fr.olympa.api.spigot.lines.AbstractLine;
import fr.olympa.api.spigot.lines.BlinkingLine;
import fr.olympa.api.spigot.lines.FixedLine;
import fr.olympa.api.utils.Utils;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.utils.npcs.HologramTrait;
import net.citizensnpcs.api.event.NPCRightClickEvent;

public class BountyTrait extends HologramTrait {
	
	private AbstractLine<HologramLine>[] lines =
			new AbstractLine[] {
					new BlinkingLine<>((color, x) -> color + "§l" + Utils.withOrWithoutS(OlympaZTA.getInstance().primes.getPrimes().size(), "prime"), OlympaZTA.getInstance(), 60, ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE),
					FixedLine.EMPTY_LINE,
					new FixedLine<>("§d§nChasseur de Primes") };
	
	public BountyTrait() {
		super("bountyMan");
		OlympaZTA.getInstance().primes.getPrimes().observe("hologram_trait", ((BlinkingLine<HologramLine>) lines[0])::updateGlobal);
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
