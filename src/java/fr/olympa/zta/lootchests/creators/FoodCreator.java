package fr.olympa.zta.lootchests.creators;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.ImmutableItemStack;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.utils.Utils;

public class FoodCreator implements LootCreator {

	private Food type;
	private double chance;
	private int min;
	private int max;

	public FoodCreator(double chance, Food type) {
		this(chance, type, 1, 1);
	}

	public FoodCreator(double chance, Food type, int min, int max) {
		this.type = type;
		this.chance = chance;
		this.min = min;
		this.max = max;
	}

	public double getChance() {
		return chance;
	}

	public Loot create(Random random) {
		return new Loot(type.get(Utils.getRandomAmount(random, min, max)));
	}

	public enum Food {
		BREAD("Pain"),
		BAKED_POTATO("Pomme de terre cuite"),
		CARROT("Carotte"),
		COOKED_RABBIT("Viande de lapin cuite"),
		COOKED_COD("Morue cuite"),
		COOKED_BEEF("Steak cuit"),
		MUSHROOM_STEW("Soupe"),
		GOLDEN_CARROT("Carotte dorée"),
		COOKIE("Cookie"),
		COOKED_PORKCHOP("Viande de porc cuite"),
		COOKED_SALMON("Saumon cuit");

		private Material type;
		private String name;
		
		private final ImmutableItemStack item;

		private Food(String name) {
			this.name = name;
			this.type = Material.valueOf(name());
			
			item = new ImmutableItemStack(get(1));
		}
		
		public ImmutableItemStack getOriginalItem() {
			return item;
		}

		public ItemStack get(int amount) {
			ItemStack item = ItemUtils.item(type, "§a" + name);
			item.setAmount(amount);
			return item;
		}

	}

}
