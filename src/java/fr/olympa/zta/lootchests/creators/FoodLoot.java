package fr.olympa.zta.lootchests.creators;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.ItemUtils;

public class FoodLoot implements LootCreator {

	private Food type;
	private double chance;
	private int min;
	private int max;

	public FoodLoot(double chance, Food type) {
		this(chance, type, 1, 1);
	}

	public FoodLoot(double chance, Food type, int min, int max) {
		this.type = type;
		this.chance = chance;
		this.min = min;
		this.max = max;
	}

	public double getChance() {
		return chance;
	}

	public void give(Player p, Random random) {
		type.give(p, random.nextInt(max - min) + min);
	}

	public enum Food {
		BREAD("Pain"),
		BAKED_POTATO("Pomme de terre cuite"),
		CARROT("Carotte"),
		COOKED_RABBIT("Viande de lapin cuite"),
		COOKED_COD("Morue cuite"),
		COOKED_BEEF("Steak cuit"),
		SOUP("Soupe"),
		GOLDEN_CARROT("Carotte dorée"),
		COOKIE("Cookie"),
		COOKED_PORKCHOP("Viande de porc cuite"),
		COOKED_SALMON("Saumon cuit");

		private Material type;
		private String name;

		private Food(String name) {
			this.name = name;
			this.type = Material.valueOf(name());
		}

		public void give(Player p, int amount) {
			ItemStack item = ItemUtils.item(type, "§a" + name);
			item.setAmount(amount);
			p.getInventory().addItem(item);
		}

	}

}
