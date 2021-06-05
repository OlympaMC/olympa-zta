package fr.olympa.zta.loot.creators;

import java.util.Random;

import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.utils.Utils;
import fr.olympa.zta.bank.PhysicalMoney;

public class MoneyCreator implements LootCreator {

	private ItemStack banknote;
	private int min, max;

	public MoneyCreator(ItemStack banknote, int min, int max) {
		this.banknote = banknote;
		this.min = min;
		this.max = max;
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
