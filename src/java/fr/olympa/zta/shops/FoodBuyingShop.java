package fr.olympa.zta.shops;

import java.util.Arrays;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.zta.lootchests.creators.FoodCreator.Food;
import fr.olympa.zta.utils.npcs.AbstractShop.AbstractBuyingShop;

public class FoodBuyingShop extends AbstractBuyingShop<Food> {
	
	private static final List<Article<Food>> ARTICLES = Arrays.asList(
			new Article<>(Food.GOLDEN_CARROT, 15),
			new Article<>(Food.COOKIE, 5),
			new Article<>(Food.BAKED_POTATO, 7),
			new Article<>(Food.COOKED_BEEF, 12),
			new Article<>(Food.COOKED_PORKCHOP, 12),
			new Article<>(Food.BREAD, 10),
			new Article<>(Food.CARROT, 8));
	
	public FoodBuyingShop() {
		super("foodshop", "Vente de nourriture", DyeColor.CYAN, ARTICLES);
	}
	
	@Override
	protected boolean take(Food object, Player p) {
		if (SpigotUtils.containsItems(p.getInventory(), object.item, 1)) {
			SpigotUtils.removeItems(p.getInventory(), object.item, 1);
			return true;
		}
		return false;
	}
	
	@Override
	public ItemStack getItemStack(Food object) {
		return object.get(1);
	}
	
}
