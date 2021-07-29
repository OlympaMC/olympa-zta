package fr.olympa.zta.loot.creators;

import java.util.Random;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import fr.olympa.zta.loot.RandomizedInventory.LootContext;

public interface LootCreator {

	public abstract Loot create(Random random, @Nullable LootContext context);
	
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

		public boolean isStackable() {
			return true;
		}
		
		public void onRemove() {}

	}

}
