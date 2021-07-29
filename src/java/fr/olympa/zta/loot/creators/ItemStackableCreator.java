package fr.olympa.zta.loot.creators;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.inventory.ItemStack;

import fr.olympa.api.common.randomized.RandomizedPickerBase.Conditioned;
import fr.olympa.zta.itemstackable.ItemStackable;
import fr.olympa.zta.loot.RandomizedInventory.LootContext;
import fr.olympa.zta.weapons.guns.GunType;

public class ItemStackableCreator implements LootCreator {

	private ItemStackable stackable;
	
	public ItemStackableCreator(ItemStackable stackable) {
		this.stackable = stackable;
	}

	@Override
	public Loot create(Random random, LootContext context) {
		return new StackableLoot();
	}
	
	@Override
	public String getTitle() {
		return stackable.getName();
	}
	
	public ItemStackable getStackable() {
		return stackable;
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
	
	public static class GunConditionned implements Conditioned<LootCreator, LootContext> {
		
		private ItemStackableCreator creator;
		private double scalar;
		private double min;
		
		public GunConditionned(GunType gun, double scalar, double min) {
			this.creator = new ItemStackableCreator(gun);
			this.scalar = scalar;
			this.min = min;
		}
		
		@Override
		public LootCreator getObject() {
			return creator;
		}
		
		@Override
		public boolean isValid(LootContext context) {
			double odds = Math.max(min, 1D - scalar * context.getCarriedGuns().size());
			return ThreadLocalRandom.current().nextDouble() <= odds;
		}
		
		@Override
		public boolean isValidWithNoContext() {
			return true;
		}
		
	}

}
