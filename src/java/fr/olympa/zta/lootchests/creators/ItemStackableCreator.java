package fr.olympa.zta.lootchests.creators;

import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import fr.olympa.zta.lootchests.creators.LootCreator.Loot.InventoryLoot;
import fr.olympa.zta.weapons.ItemStackable;

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

	class StackableLoot extends Loot implements InventoryLoot {

		public StackableLoot() {
			super(stackable.getDemoItem());
		}

		@Override
		public void onTake(Player p, Inventory inv, int slot) {
			inv.setItem(slot, stackable.createItem());
		}

	}

}
