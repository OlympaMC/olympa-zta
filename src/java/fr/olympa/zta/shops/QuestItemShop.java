package fr.olympa.zta.shops;

import java.util.Arrays;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import fr.olympa.zta.itemstackable.QuestItem;
import fr.olympa.zta.utils.npcs.AbstractShop.AbstractBuyingShop;

public class QuestItemShop extends AbstractBuyingShop<QuestItem> {
	
	private static final List<Article<QuestItem>> ARTICLES = Arrays.asList(
			new Article<>(QuestItem.DECHET, 10),
			new Article<>(QuestItem.AMAS, 12));
	
	public QuestItemShop() {
		super("questitemshop", "Rachat de pièeces", "Pièces détachées", DyeColor.MAGENTA, ARTICLES);
	}
	
	@Override
	protected int take(QuestItem object, Player p, boolean shift) {
		PlayerInventory inv = p.getInventory();
		if (shift) {
			int amount = Math.min(object.getItemAmount(inv), 64);
			if (amount > 0) object.removeItems(inv, amount);
			return amount;
		}
		if (object.containsAmount(inv, 1)) {
			object.removeItems(inv, 1);
			return 1;
		}
		return 0;
	}
	
	@Override
	public ItemStack getItemStack(QuestItem object) {
		return object.createItem();
	}
	
}
