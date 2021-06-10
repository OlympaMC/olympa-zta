package fr.olympa.zta.loot;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.RandomizedPicker;
import fr.olympa.zta.loot.creators.LootCreator;
import fr.olympa.zta.loot.creators.LootCreator.Loot;

public abstract class RandomizedInventory extends OlympaGUI implements RandomizedPicker<LootCreator> {
	
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
		for (LootCreator creator : pick(random)) {
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
	
	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (click == ClickType.DROP || click == ClickType.CONTROL_DROP) return true;
		Loot loot = currentLoots.get(slot);
		if (loot == null) throw new RuntimeException("No loot in slot " + slot);
		if (click.isShiftClick() && p.getInventory().firstEmpty() == -1) {
			Prefix.DEFAULT_BAD.sendMessage(p, "Il n'y a pas d'espace pour cet item dans ton inventaire...");
			return false;
		}
		ItemStack realItem = loot.getRealItem();
		if (realItem != null) inv.setItem(slot, realItem);
		currentLoots.remove(slot);
		return false;
	}
	
}
