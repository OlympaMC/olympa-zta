package fr.olympa.zta.loot;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.gui.OlympaGUI;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.RandomizedPickerBase.RandomizedMultiPicker;
import fr.olympa.zta.loot.creators.LootCreator;
import fr.olympa.zta.loot.creators.LootCreator.Loot;

public abstract class RandomizedInventory extends OlympaGUI {
	
	private Map<Integer, Loot> currentLoots = new HashMap<>();
	private Random random = new Random();
	
	public RandomizedInventory(String name, InventoryType type) {
		super(name, type);
	}
	
	public RandomizedInventory(String name, int rows) {
		super(name, rows);
	}
	
	protected void fillInventory() {
		clearInventory();
		for (LootCreator creator : getLootPicker().pickMulti(random)) {
			int slot;
			do {
				slot = random.nextInt(inv.getSize());
			}while (inv.getItem(slot) != null);

			Loot loot = creator.create(random);
			currentLoots.put(slot, loot);
			inv.setItem(slot, loot.getItem());
		}
	}
	
	protected void clearInventory() {
		currentLoots.values().forEach(Loot::onRemove);
		currentLoots.clear();
		inv.clear();
	}
	
	protected abstract RandomizedMultiPicker<LootCreator> getLootPicker();
	
	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (click == ClickType.DROP || click == ClickType.CONTROL_DROP) return true;
		Loot loot = currentLoots.get(slot);
		if (loot == null) throw new RuntimeException("No loot in slot " + slot);
		if (click.isShiftClick()) {
			if (p.getInventory().firstEmpty() == -1) {
				boolean valid = false;
				if (loot.isStackable() && current.getMaxStackSize() > 1) {
					int amount = current.getAmount();
					for (ItemStack item : p.getInventory().getStorageContents()) {
						if (item.isSimilar(current)) {
							amount -= item.getMaxStackSize() - item.getAmount(); // remove available amount in this slot
							if (amount <= 0) {
								valid = true;
								break;
							}
						}
					}
				}
				if (!valid) {
					Prefix.DEFAULT_BAD.sendMessage(p, "Il n'y a pas d'espace pour cet item dans ton inventaire...");
					return true;
				}
			}
		}
		ItemStack realItem = loot.getRealItem();
		if (realItem != null) inv.setItem(slot, realItem);
		if (click != ClickType.RIGHT || current.getAmount() > 1) currentLoots.remove(slot);
		return false;
	}
	
}
