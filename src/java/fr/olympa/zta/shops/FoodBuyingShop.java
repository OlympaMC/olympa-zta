package fr.olympa.zta.shops;

import java.util.Arrays;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import fr.olympa.api.spigot.item.ImmutableItemStack;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.zta.loot.creators.FoodCreator.Food;
import fr.olympa.zta.utils.npcs.AbstractShop.AbstractBuyingShop;

public class FoodBuyingShop extends AbstractBuyingShop<Food> {
	
	private static final List<Article<Food>> ARTICLES = Arrays.asList(
			new Article<>(Food.GOLDEN_CARROT, 10),
			new Article<>(Food.COOKIE, 5),
			new Article<>(Food.COOKED_BEEF, 8),
			new Article<>(Food.COOKED_PORKCHOP, 8),
			new Article<>(Food.COOKED_RABBIT, 8),
			new Article<>(Food.MUSHROOM_STEW, 7),
			new Article<>(Food.COOKED_COD, 8),
			new Article<>(Food.COOKED_SALMON, 8));
	
	public FoodBuyingShop() {
		super("foodshop", "Rachat de nourriture", "Nourriture", DyeColor.CYAN, ARTICLES);
	}
	
	@Override
	protected int take(Food object, Player p, boolean shift) {
		PlayerInventory inv = p.getInventory();
		ImmutableItemStack item = object.getOriginalItem();
		int amount = shift ? Math.min(SpigotUtils.getItemAmount(inv, item), 64) : 1;
		if (amount > 0 && SpigotUtils.containsItems(inv, item, amount)) {
			SpigotUtils.removeItems(inv, item, amount);
			return amount;
		}
		return 0;
	}
	
	@Override
	public ItemStack getItemStack(Food object) {
		return object.get(1);
	}
	
}
