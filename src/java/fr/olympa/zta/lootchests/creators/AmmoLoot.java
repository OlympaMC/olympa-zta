package fr.olympa.zta.lootchests.creators;

import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.zta.weapons.guns.AmmoType;

public class AmmoLoot implements LootCreator {

	private double chance;
	private AmmoType type;
	private int min;
	private int max;
	private boolean filled;

	public AmmoLoot(double chance, int min, int max) {
		this(chance, null, min, max, false);
	}

	public AmmoLoot(double chance, AmmoType type, int min, int max, boolean filled) {
		this.type = type;
		this.chance = chance;
		this.min = min;
		this.max = max;
		this.filled = filled;
	}

	public double getChance() {
		return chance;
	}

	public ItemStack create(Player p, Random random) {
		int amount = random.nextInt(max - min + 1) + min;
		return type == null ? AmmoType.getPowder(amount) : type.getAmmo(amount, filled);
	}

}
