package fr.olympa.zta.clans;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.clans.ClanPlayerInterface;
import fr.olympa.api.spigot.clans.ClansManager;
import fr.olympa.api.spigot.clans.gui.ClanManagementGUI;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.clans.plots.ClanPlayerDataZTA;
import fr.olympa.zta.clans.plots.ClanPlot;

public class ClanZTAManagementGUI extends ClanManagementGUI<ClanZTA, ClanPlayerDataZTA> {

	private static ItemStack noSpace = ItemUtils.skullCustom("§cx", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTgzODI0MWUyOTg1MjZjNTg5YzAwM2VhN2FiZWViOGFmZGRhZTc1NWVlNzhlYzgyNzZlZjA2MTU2MmUwZjNkZiJ9fX0=", "§7§oAchetez un grade sur la boutique", "§7§opour obtenir des emplacements", "§7§osupplémentaires ! §l/shop"); // https://minecraft-heads.com/custom-heads/alphabet/32486-oak-wood-omega

	private ClanPlot plot;

	public ClanZTAManagementGUI(ClanPlayerInterface<ClanZTA, ClanPlayerDataZTA> p, ClanZTA clan, ClansManager<ClanZTA, ClanPlayerDataZTA> manager) {
		super(p, clan, manager, 3);
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
		
		int maxSize = ((ClansManagerZTA) manager).getMaxSize();
		if (clan.getMaxSize() < maxSize) {
			for (int i = clan.getMaxSize(); i < maxSize; i++) {
				int slot = getPlayerSlot(i);
				inv.setItem(slot, noSpace);
			}
		}
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
		return (id >= 7 ? 11 : 9) + id;
	}
	
	@Override
	protected int getPlayerID(int slot) {
		return slot - (slot > 18 ? 11 : 9);
	}
	
	@Override
	protected int slotLeave() {
		return 17;
	}
	
	@Override
	protected int slotDisband() {
		return 26;
	}

}
