package fr.olympa.zta.bank;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaPlayerZTA;

public class BankGUI extends OlympaGUI {

	private OlympaPlayerZTA player;
	private int maxSlot;

	private boolean change = false;

	public BankGUI(OlympaPlayerZTA player) {
		super("Coffre de " + player.getName(), OlympaPlayerZTA.MAX_SLOTS / 9);
		this.player = player;
		maxSlot = player.getBankSlots();

		for (int i = 0; i < OlympaPlayerZTA.MAX_SLOTS; i++) {
			if (i < maxSlot) {
				if (player.getBankContent().length > i) inv.setItem(i, player.getBankContent()[i]);
			}else if (i == maxSlot) {
				inv.setItem(i, slotBuyItem());
			}else {
				inv.setItem(i, ItemUtils.item(Material.RED_STAINED_GLASS_PANE, "§cDébloquez les emplacement précédents"));
			}
		}
	}
	
	private ItemStack slotBuyItem() {
		return ItemUtils.item(Material.LIME_STAINED_GLASS_PANE, "§aAcheter le slot : §l" + price(maxSlot) + "$");
	}

	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (slot < maxSlot) {
			change = true;
			return false;
		}else if (slot == maxSlot) { // si c'est le slot à acheter
			if (player.getGameMoney().withdraw(price(maxSlot))) { // vérifier si l'achat se fait
				player.incrementBankSlots();
				maxSlot++;
				inv.setItem(slot, null); // enlever l'item "Acheter l'emplacement"
				if (maxSlot < OlympaPlayerZTA.MAX_SLOTS) inv.setItem(maxSlot, slotBuyItem());
			}else Prefix.DEFAULT_BAD.sendMessage(p, "Vous n'avez pas l'argent suffisant pour acheter cet emplacement.");
			return true;
		}
		return true;
	}
	
	public boolean onClickCursor(Player p, ItemStack current, ItemStack cursor, int slot) {
		return slot >= maxSlot;
	}

	public boolean onClose(Player p) {
		if (change) {
			ItemStack[] items = new ItemStack[maxSlot];
			for (int i = 0; i < maxSlot; i++) {
				items[i] = inv.getItem(i);
			}
			player.setBankContent(items);
		}
		return true;
	}
	
	public static int price(int slot) {
		return slot * 30; // TODO
	}

}
