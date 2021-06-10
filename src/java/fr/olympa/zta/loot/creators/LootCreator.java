package fr.olympa.zta.loot.creators;

import java.util.Random;

import org.bukkit.inventory.ItemStack;

import fr.olympa.api.utils.RandomizedPicker.Chanced;

public interface LootCreator extends Chanced {

	public abstract Loot create(Random random);
	
	public abstract String getTitle();

	public class Loot {

		private final ItemStack item;

		public Loot(ItemStack item) {
			this.item = item;
		}

		public ItemStack getItem() {
			return item;
		}
		
		public ItemStack getRealItem() {
			return null;
		}

		public void onRemove() {}

	}

}
