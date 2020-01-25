package fr.olympa.zta.bank;

import java.io.IOException;
import java.sql.SQLException;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.utils.Prefix;

public class ChestGUI extends OlympaGUI {

	public final static int MAX_ROWS = 3;
	private OlympaPlayer player;
	private int maxSlot;

	private boolean change = false;

	public ChestGUI(OlympaPlayer player, int slots, ItemStack[] contents) {
		super("Coffre de " + player.getName(), MAX_ROWS);
		this.player = player;
		maxSlot = slots;

		for (int i = 0; i < MAX_ROWS * 9; i++) {
			if (i < maxSlot) {
				if (contents.length > i) inv.setItem(i, contents[i]);
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
			if (player.withdrawMoney(price(maxSlot))) { // vérifier si l'achat se fait
				try {
					ChestManagement.updateSize(player, maxSlot + 1);
					maxSlot++;
					inv.setItem(slot, null); // enlever l'item "Acheter l'emplacement"
					if (maxSlot < MAX_ROWS * 9) inv.setItem(maxSlot, slotBuyItem());
				}catch (SQLException e) {
					e.printStackTrace();
					Prefix.ERROR.sendMessage(p, "Une erreur est survenue lors de l'augmentation de la capacité de votre banque.");
				}
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
			try {
				ChestManagement.saveItems(player, items);
			}catch (IOException | SQLException e) {
				e.printStackTrace();
				Prefix.ERROR.sendMessage(p, "Une erreur est survenue lors de l'enregistrement de votre coffre.");
			}
		}
		return true;
	}
	
	public static int price(int slot) {
		return slot * 30; // TODO
	}

}
