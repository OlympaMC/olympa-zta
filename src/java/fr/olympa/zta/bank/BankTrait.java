package fr.olympa.zta.bank;

import org.bukkit.event.EventHandler;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.zta.OlympaPlayerZTA;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;

public class BankTrait extends Trait {

	protected BankTrait() {
		super("bank");
	}

	@EventHandler
	public void onNPCRightClick(NPCRightClickEvent e) {
		if (e.getNPC() != npc) return;
		new ChestGUI((OlympaPlayerZTA) AccountProvider.get(e.getClicker().getUniqueId())).create(e.getClicker());
	}

}
