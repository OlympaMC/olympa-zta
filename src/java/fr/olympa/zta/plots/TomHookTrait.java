package fr.olympa.zta.plots;

import org.bukkit.event.EventHandler;

import fr.olympa.zta.OlympaPlayerZTA;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;

public class TomHookTrait extends Trait {

	public TomHookTrait() {
		super("plots");
	}

	@EventHandler
	public void onClick(NPCRightClickEvent e) {
		if (e.getNPC() == npc) {
			new PlayerPlotGUI(OlympaPlayerZTA.get(e.getClicker())).create(e.getClicker());
		}
	}

}
