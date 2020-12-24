package fr.olympa.zta.loot.creators;

import java.util.Random;

import fr.olympa.api.utils.Utils;
import fr.olympa.zta.utils.PhysicalMoney;

public class MoneyCreator implements LootCreator {

	private double chance;
	private int min, max;

	public MoneyCreator(double chance, int min, int max) {
		this.chance = chance;
		this.min = min;
		this.max = max;
	}

	@Override
	public double getChance() {
		return chance;
	}

	@Override
	public Loot create(Random random) {
		return new Loot(PhysicalMoney.getBanknote(PhysicalMoney.BANKNOTE_1, Utils.getRandomAmount(random, min, max)));
	}

}
