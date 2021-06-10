package fr.olympa.zta.loot.creators;

import java.util.Random;

import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.utils.Utils;
import fr.olympa.zta.utils.PhysicalMoney;

public class MoneyCreator implements LootCreator {

	private double chance;
	private ItemStack banknote;
	private int min, max;

	public MoneyCreator(double chance, ItemStack banknote, int min, int max) {
		this.chance = chance;
		this.banknote = banknote;
		this.min = min;
		this.max = max;
	}

	@Override
	public double getChance() {
		return chance;
	}

	@Override
	public Loot create(Random random) {
		return new Loot(PhysicalMoney.getBanknote(banknote, Utils.getRandomAmount(random, min, max)));
	}
	
	@Override
	public String getTitle() {
		return ItemUtils.getName(banknote);
	}

}
