package fr.olympa.zta.clans;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
import fr.olympa.zta.plots.clans.ClanPlot;

public class ClanZTAManagementGUI extends ClanManagementGUI<ClanZTA> {

	private static final DateFormat paymentDateFormat = new SimpleDateFormat("dd/MM");

	private ClanPlot plot;

	public ClanZTAManagementGUI(ClanPlayerInterface<ClanZTA> p, ClansManager<ClanZTA> manager) {
		super(p, manager, 2);
	}

	@Override
	protected void refreshInventory() {
		super.refreshInventory();

		plot = clan.cachedPlot;

		String[] plotLore;
		if (plot == null) {
			plotLore = new String[] { "§8> §oVotre clan n'a pas de parcelle" };
		}else {
			plotLore = new String[] { "§8> §oLoyer : §l" + plot.getPrice(), "§8> §oProchain payement : §l" + paymentDateFormat.format(new Date(plot.getNextPayment())), "", "§e§lClique pour t'y téléporter" };
		}
		inv.setItem(7, ItemUtils.item(Material.STONE, "§6Parcelle du clan", plotLore));
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (slot == 7) {
			if (plot != null) OlympaZTA.getInstance().teleportationManager.teleport(p, plot.getSpawn(), Prefix.DEFAULT_GOOD.formatMessage("Tu as été téléporté dans la parcelle de ton clan !"));
			return true;
		}
		return super.onClick(p, current, slot, click);
	}

}
