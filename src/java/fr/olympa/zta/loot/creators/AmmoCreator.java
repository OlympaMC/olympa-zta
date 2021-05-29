package fr.olympa.zta.loot.creators;

import java.util.Random;

import fr.olympa.api.utils.Utils;
import fr.olympa.zta.weapons.guns.AmmoType;

public class AmmoCreator implements LootCreator {

	private AmmoType type;
	private int min;
	private int max;
	private boolean filled;

	public AmmoCreator(int min, int max) {
		this(null, min, max, false);
	}

	public AmmoCreator(AmmoType type, int min, int max, boolean filled) {
		this.type = type;
		this.min = min;
		this.max = max;
		this.filled = filled;
	}

	public Loot create(Random random) {
		int amount = Utils.getRandomAmount(random, min, max);
		return new Loot(type == null ? AmmoType.getPowder(amount) : type.getAmmo(amount, filled));
	}
	
	@Override
	public String getTitle() {
		return type == null ? "Poudre à canon" : (type.getName() + (filled ? "" : " vides"));
	}

}
