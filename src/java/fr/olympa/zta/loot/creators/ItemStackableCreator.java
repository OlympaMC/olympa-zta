package fr.olympa.zta.loot.creators;

import java.util.Random;

import org.bukkit.inventory.ItemStack;

import fr.olympa.zta.itemstackable.ItemStackable;
import fr.olympa.zta.weapons.guns.GunType;

public class ItemStackableCreator implements LootCreator {

	private ItemStackable stackable;
	
	public ItemStackableCreator(ItemStackable stackable) {
		this.stackable = stackable;
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
		
		@Override
		public boolean isStackable() {
			return !(stackable instanceof GunType);
		}

	}

}
