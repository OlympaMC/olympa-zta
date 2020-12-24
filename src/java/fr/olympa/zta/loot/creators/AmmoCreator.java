package fr.olympa.zta.loot.creators;

import java.util.Random;

import fr.olympa.api.utils.Utils;
import fr.olympa.zta.weapons.guns.AmmoType;

public class AmmoCreator implements LootCreator {

	private double chance;
	private AmmoType type;
	private int min;
	private int max;
	private boolean filled;

	public AmmoCreator(double chance, int min, int max) {
		this(chance, null, min, max, false);
	}

	public AmmoCreator(double chance, AmmoType type, int min, int max, boolean filled) {
		this.type = type;
		this.chance = chance;
		this.min = min;
		this.max = max;
		this.filled = filled;
	}

	public double getChance() {
		return chance;
	}

	public Loot create(Random random) {
		int amount = Utils.getRandomAmount(random, min, max);
		return new Loot(type == null ? AmmoType.getPowder(amount) : type.getAmmo(amount, filled));
	}

}
