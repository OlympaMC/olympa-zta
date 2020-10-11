package fr.olympa.zta.lootchests.creators;

import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import fr.olympa.zta.lootchests.creators.LootCreator.Loot.InventoryLoot;
import fr.olympa.zta.registry.ItemStackable;
import fr.olympa.zta.registry.ItemStackableInstantiator;
import fr.olympa.zta.registry.ZTARegistry;

public class ItemStackableCreator implements LootCreator {

	private double chance;
	private ItemStackableInstantiator<? extends ItemStackable> instantiator;

	public ItemStackableCreator(double chance, Class<? extends ItemStackable> clazz) {
		this.chance = chance;
		for (ItemStackableInstantiator<?> inst : ZTARegistry.get().itemStackables) {
			if (inst.clazz == clazz) {
				this.instantiator = inst;
				break;
			}
		}
		if (instantiator == null) {
			System.out.println("No instantiator for class " + clazz.getName());
			new IllegalArgumentException("No instantiator for class " + clazz.getName());
		}
	}

	public ItemStackableCreator(double chance, ItemStackableInstantiator<? extends ItemStackable> inst) {
		this.chance = chance;
		this.instantiator = inst;
	}

	public double getChance() {
		return chance;
	}
	
	public ItemStackableInstantiator<? extends ItemStackable> getInstantiator() {
		return instantiator;
	}

	public Loot create(Player p, Random random) {
		return new ItemStackableLoot();
	}

	class ItemStackableLoot extends Loot implements InventoryLoot {

		public ItemStackableLoot() {
			super(instantiator.getDemoItem());
		}

		@Override
		public boolean onTake(Player p, Inventory inv, int slot) {
			try {
				inv.setItem(slot, ZTARegistry.get().createItem(instantiator.create()));
			}catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
			return false;
		}

	}

}
