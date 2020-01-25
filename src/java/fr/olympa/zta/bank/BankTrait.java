package fr.olympa.zta.bank;

import java.io.IOException;
import java.sql.SQLException;

import org.bukkit.event.EventHandler;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;

public class BankTrait extends Trait {

	protected BankTrait() {
		super("bank");
	}

	@EventHandler
	public void onNPCRightClick(NPCRightClickEvent e) {
		if (e.getNPC() != npc) return;
		OlympaPlayer p = AccountProvider.get(e.getClicker().getUniqueId());
		try {
			ChestManagement.getBankGUI(p).create(e.getClicker());
		}catch (SQLException | IOException e1) {
			e1.printStackTrace();
		}
	}

}
