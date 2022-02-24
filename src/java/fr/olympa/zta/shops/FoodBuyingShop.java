package fr.olympa.zta.shops;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import fr.olympa.api.spigot.economy.fluctuating.FixedFluctuatingEconomy;
import fr.olympa.api.spigot.economy.fluctuating.FluctuatingEconomy;
import fr.olympa.api.spigot.item.ImmutableItemStack;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.loot.creators.FoodCreator.Food;
import fr.olympa.zta.utils.npcs.AbstractShop.AbstractBuyingShop;

public class FoodBuyingShop extends AbstractBuyingShop<Food> {
	
	private static boolean registered = false;
	private static final FluctuatingEconomy BREAD_ECO = new FixedFluctuatingEconomy("bread_sell", 1.1, 0.08, 40, TimeUnit.MINUTES, 0.03, 0.1);
	private static final FluctuatingEconomy CARROT_ECO = new FixedFluctuatingEconomy("carrot_sell", 1.1, 0.08, 40, TimeUnit.MINUTES, 0.03, 0.1);
	private static final FluctuatingEconomy POTATO_ECO = new FixedFluctuatingEconomy("potato_sell", 1.1, 0.08, 40, TimeUnit.MINUTES, 0.03, 0.1);
	private static final FluctuatingEconomy COD_ECO = new FixedFluctuatingEconomy("cod_sell", 4, 1, 30, TimeUnit.MINUTES, 0.06, 0.2);
	private static final FluctuatingEconomy SALMON_ECO = new FixedFluctuatingEconomy("salmon_sell", 4.5, 1, 30, TimeUnit.MINUTES, 0.06, 0.2);
	private static final List<AbstractArticle<Food>> ARTICLES =
			Arrays.asList(
					new FluctuatingArticle<>(Food.BREAD, BREAD_ECO),
					new FluctuatingArticle<>(Food.CARROT, CARROT_ECO),
					new FluctuatingArticle<>(Food.BAKED_POTATO, POTATO_ECO),
					new FluctuatingArticle<>(Food.COOKED_COD, COD_ECO),
					new FluctuatingArticle<>(Food.COOKED_SALMON, SALMON_ECO),
					new Article<>(Food.GOLDEN_CARROT, 10),
					new Article<>(Food.COOKIE, 5),
					new Article<>(Food.COOKED_BEEF, 8),
					new Article<>(Food.COOKED_PORKCHOP, 8),
					new Article<>(Food.COOKED_RABBIT, 8),
					new Article<>(Food.MUSHROOM_STEW, 7));
	
	public FoodBuyingShop() {
		super("foodshop", "Rachat de nourriture", "Nourriture", DyeColor.CYAN, ARTICLES);
		if (!registered) {
			try {
				OlympaZTA.getInstance().economies.register(BREAD_ECO, POTATO_ECO, CARROT_ECO, COD_ECO, SALMON_ECO);
			}catch (SQLException e) {
				OlympaZTA.getInstance().sendMessage("§cUne erreur est survenue lors du démarrage d'une économie de nourriture.");
				e.printStackTrace();
			}
			registered = true;
		}
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
