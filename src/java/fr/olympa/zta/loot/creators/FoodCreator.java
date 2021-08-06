package fr.olympa.zta.loot.creators;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.item.ImmutableItemStack;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.utils.Utils;
import fr.olympa.zta.loot.RandomizedInventory.LootContext;

public class FoodCreator implements LootCreator {

	private Food type;
	private int min;
	private int max;

	public FoodCreator(Food type) {
		this(type, 1, 1);
	}

	public FoodCreator(Food type, int min, int max) {
		this.type = type;
		this.min = min;
		this.max = max;
	}

	@Override
	public Loot create(Random random, LootContext context) {
		return new Loot(type.get(Utils.getRandomAmount(random, min, max)));
	}
	
	@Override
	public String getTitle() {
		return type.name;
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
		GOLDEN_APPLE("Pomme d'or"),
		APPLE("Pomme"),
		COOKIE("Cookie"),
		COOKED_PORKCHOP("Viande de porc cuite"),
		COOKED_SALMON("Saumon cuit"),
		DRIED_KELP("Biscuit militaire"),
		;
		
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
