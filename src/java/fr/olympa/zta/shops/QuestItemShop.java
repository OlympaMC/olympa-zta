package fr.olympa.zta.shops;

import java.util.Arrays;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import fr.olympa.api.item.ImmutableItemStack;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.zta.loot.creators.QuestItemCreator.QuestItem;
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
		ImmutableItemStack item = object.getDemoItem();
		int amount = shift ? Math.min(SpigotUtils.getItemAmount(inv, item), 64) : 1;
		if (amount > 0 && SpigotUtils.containsItems(inv, item, amount)) {
			SpigotUtils.removeItems(inv, item, amount);
			return amount;
		}
		return 0;
	}
	
	@Override
	public ItemStack getItemStack(QuestItem object) {
		return object.createItem();
	}
	
}
