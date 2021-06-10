package fr.olympa.zta.clans;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.clans.ClanPlayerInterface;
import fr.olympa.api.clans.ClansManager;
import fr.olympa.api.clans.gui.ClanManagementGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.clans.plots.ClanPlayerDataZTA;
import fr.olympa.zta.clans.plots.ClanPlot;

public class ClanZTAManagementGUI extends ClanManagementGUI<ClanZTA, ClanPlayerDataZTA> {

	private ClanPlot plot;

	public ClanZTAManagementGUI(ClanPlayerInterface<ClanZTA, ClanPlayerDataZTA> p, ClanZTA clan, ClansManager<ClanZTA, ClanPlayerDataZTA> manager) {
		super(p, clan, manager, clan.getMaxSize() == 5 ? 2 : 3);
	}

	@Override
	protected void refreshInventory() {
		super.refreshInventory();

		plot = clan.getCachedPlot();

		String[] plotLore;
		if (plot == null) {
			plotLore = new String[] { "§8> §oVotre clan n'a pas de parcelle" };
		}else {
			plotLore = new String[] { "§8> §oLoyer : §l" + plot.getPriceFormatted(), "§8> §oDate d'expiration : §l" + plot.getExpirationDate(), "", "§e§lClique pour t'y téléporter" };
		}
		inv.setItem(1, ItemUtils.item(Material.STONE, "§6Parcelle du clan", plotLore));
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (slot == 1) {
			if (plot != null) OlympaZTA.getInstance().teleportationManager.teleport(p, plot.getSpawn(), Prefix.DEFAULT_GOOD.formatMessage("Tu as été téléporté dans la parcelle de ton clan !"));
			return true;
		}
		return super.onClick(p, current, slot, click);
	}
	
	@Override
	protected int getPlayerSlot(int id) {
		return (id >= 5 ? 13 : 9) + id;
	}
	
	@Override
	protected int getPlayerID(int slot) {
		return slot - (slot > 18 ? 13 : 9);
	}

}
