package fr.olympa.zta.lootchests.creators;

import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.lootchests.creators.LootCreator.Loot.InventoryLoot;
import fr.olympa.zta.weapons.guns.Gun;
import fr.olympa.zta.weapons.guns.GunRegistry.GunInstantiator;

public class GunCreator implements LootCreator {

	private double chance;
	private GunInstantiator<?> instantiator;

	public GunCreator(double chance, Class<? extends Gun> clazz) {
		this(chance, OlympaZTA.getInstance().gunRegistry.getInstantiator(clazz));
	}
	
	public GunCreator(double chance, GunInstantiator<?> instantiator) {
		this.chance = chance;
		this.instantiator = instantiator;
	}

	public double getChance() {
		return chance;
	}

	public Loot create(Player p, Random random) {
		return new GunLoot();
	}

	class GunLoot extends Loot implements InventoryLoot {

		public GunLoot() {
			super(instantiator.getDemoItem());
		}

		@Override
		public boolean onTake(Player p, Inventory inv, int slot) {
			inv.setItem(slot, instantiator.createItem());
			return false;
		}

	}

}
