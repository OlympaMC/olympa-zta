package fr.olympa.zta;

import org.bukkit.event.EventHandler;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;

public class AuctionsTrait extends Trait {

	public AuctionsTrait() {
		super("auctions");
	}

	@EventHandler
	public void onNPCRightClick(NPCRightClickEvent e) {
		if (e.getNPC() != npc) return;
		OlympaZTA.getInstance().auctionsManager.openAuctionsGUI(e.getClicker());
	}

}
