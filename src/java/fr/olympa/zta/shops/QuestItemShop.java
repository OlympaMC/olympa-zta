package fr.olympa.zta.shops;

import java.util.Arrays;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.zta.lootchests.creators.QuestItemCreator.QuestItem;
import fr.olympa.zta.utils.npcs.AbstractShop.AbstractBuyingShop;

public class QuestItemShop extends AbstractBuyingShop<QuestItem> {
	
	private static final List<Article<QuestItem>> ARTICLES = Arrays.asList(new Article<>(QuestItem.DECHET, 15));
	
	public QuestItemShop() {
		super("questitemshop", "Rachat de pièeces", DyeColor.MAGENTA, ARTICLES);
	}
	
	@Override
	protected boolean take(QuestItem object, Player p) {
		if (SpigotUtils.containsItems(p.getInventory(), object.getOriginalItem(), 1)) {
			SpigotUtils.removeItems(p.getInventory(), object.getOriginalItem(), 1);
			return true;
		}
		return false;
	}
	
	@Override
	public ItemStack getItemStack(QuestItem object) {
		return object.getOriginalItem();
	}
	
}
