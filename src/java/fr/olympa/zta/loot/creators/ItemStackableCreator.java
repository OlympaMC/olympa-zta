package fr.olympa.zta.loot.creators;

import java.util.Random;

import org.bukkit.inventory.ItemStack;

import fr.olympa.zta.itemstackable.ItemStackable;

public class ItemStackableCreator implements LootCreator {

	private double chance;
	private ItemStackable stackable;
	
	public ItemStackableCreator(double chance, ItemStackable stackable) {
		this.chance = chance;
		this.stackable = stackable;
	}

	public double getChance() {
		return chance;
	}

	public Loot create(Random random) {
		return new StackableLoot();
	}
	
	@Override
	public String getTitle() {
		return stackable.getName();
	}

	class StackableLoot extends Loot {

		public StackableLoot() {
			super(stackable.getDemoItem());
		}
		
		@Override
		public ItemStack getRealItem() {
			return stackable.createItem();
		}

	}

}
