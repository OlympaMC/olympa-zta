package fr.olympa.zta.lootchests.creators;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.zta.OlympaPlayerZTA;

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
	public Loot create(Player p, Random random) {
		return new MoneyLoot(random.nextInt(max - min) + min);
	}

	static class MoneyLoot extends Loot {

		private int amount;

		public MoneyLoot(int amount) {
			super(ItemUtils.item(Material.GOLD_INGOT, "§6§l" + amount + "§e Omegas"));
			this.amount = amount;
		}

		@Override
		public boolean onTake(Player p, Inventory inv, int slot) {
			OlympaPlayerZTA.get(p).getGameMoney().give(amount);
			inv.setItem(slot, null);
			return true;
		}

	}

}
