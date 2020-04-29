package fr.olympa.zta.lootchests.creators;

import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.utils.AbstractRandomizedPicker.Chanced;

public interface LootCreator extends Chanced {

	public abstract Loot create(Player p, Random random);

	public class Loot {

		private final ItemStack item;

		public Loot(ItemStack item) {
			this.item = item;
		}

		public ItemStack getItem() {
			return item;
		}

		public boolean onTake(Player p, Inventory inv, int slot) {
			return false;
		}

		public void onRemove() {}

	}

}
