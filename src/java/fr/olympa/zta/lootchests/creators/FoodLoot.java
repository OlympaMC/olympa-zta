package fr.olympa.zta.lootchests.creators;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.utils.AbstractRandomizedPicker;
import fr.olympa.zta.lootchests.creators.FoodLoot.Food;

public class FoodLoot extends AbstractRandomizedPicker<Food> implements LootCreator {

	public void give(Player p, Random random) {
		pick().forEach((food) -> food.give(p));
	}

	public double getChance() {
		return -1;
	}

	public int getMinItems() {
		return 1;
	}

	public int getMaxItems() {
		return 2;
	}

	private List<Food> foodList = Arrays.asList(Food.values());
	public List<Food> getObjectList() {
		return foodList;
	}

	public List<Food> getAlwaysObjectList() {
		return Collections.EMPTY_LIST;
	}

	public enum Food implements fr.olympa.api.utils.AbstractRandomizedPicker.Chanced {
		STEAK(2, ItemUtils.item(Material.COOKED_BEEF, "Steak")), APPLE(1, ItemUtils.item(Material.APPLE, "Pomme")), CARROT(0.5, ItemUtils.item(Material.CARROT, "Carrotte"));

		private double chance;
		private ItemStack item;

		private Food(double chance, ItemStack item) {
			this.chance = chance;
			this.item = item;
		}

		public double getChance() {
			return chance;
		}

		public void give(Player p) {
			p.getInventory().addItem(item.clone());
		}

	}

}
