package fr.olympa.zta.lootchests.creators;

import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import fr.olympa.zta.registry.ItemStackable;
import fr.olympa.zta.registry.ItemStackableInstantiator;
import fr.olympa.zta.registry.ZTARegistry;

public class ItemStackableCreator<T extends ItemStackable> implements LootCreator {

	private double chance;
	private ItemStackableInstantiator<T> instantiator;

	public ItemStackableCreator(double chance, Class<T> clazz) {
		this.chance = chance;
		for (ItemStackableInstantiator<?> inst : ZTARegistry.itemStackables) {
			if (inst.clazz == clazz) {
				this.instantiator = (ItemStackableInstantiator<T>) inst;
				break;
			}
		}
		if (instantiator == null) new IllegalArgumentException("No instantiator for class " + clazz.getName());
	}

	public ItemStackableCreator(double chance, ItemStackableInstantiator<T> inst) {
		this.chance = chance;
		this.instantiator = inst;
	}

	public double getChance() {
		return chance;
	}

	public Loot create(Player p, Random random) {
		return new ItemStackableLoot(instantiator);
	}

	class ItemStackableLoot extends Loot {

		private ItemStackableInstantiator<T> instantiator;

		public ItemStackableLoot(ItemStackableInstantiator<T> stackable) {
			super(stackable.getDemoItem());
			this.instantiator = stackable;
		}

		@Override
		public boolean onTake(Player p, Inventory inv, int slot) {
			try {
				inv.setItem(slot, ZTARegistry.createItem(instantiator.create()));
			}catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
			return false;
		}

	}

}
