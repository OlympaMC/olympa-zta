package fr.olympa.zta.utils.npcs;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;

import fr.olympa.api.holograms.Hologram.HologramLine;
import fr.olympa.api.lines.AbstractLine;
import fr.olympa.api.lines.BlinkingLine;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.zta.OlympaZTA;
import net.citizensnpcs.api.event.NPCRightClickEvent;

public class AuctionsTrait extends HologramTrait {

	private AbstractLine<HologramLine>[] lines = new AbstractLine[] {
			new BlinkingLine<>((color, x) -> color + "§l" + OlympaZTA.getInstance().auctionsManager.getOngoingAuctions().size() + " ventes", OlympaZTA.getInstance(), 60, ChatColor.YELLOW, ChatColor.GOLD),
			FixedLine.EMPTY_LINE,
			new FixedLine<>("§e§nHôtel des Ventes") };
	
	public AuctionsTrait() {
		super("auctions");
	}

	@Override
	protected AbstractLine<HologramLine>[] getLines() {
		return lines;
	}
	
	@EventHandler
	public void onNPCRightClick(NPCRightClickEvent e) {
		if (e.getNPC() != npc) return;
		OlympaZTA.getInstance().auctionsManager.openAuctionsGUI(e.getClicker());
	}

}
