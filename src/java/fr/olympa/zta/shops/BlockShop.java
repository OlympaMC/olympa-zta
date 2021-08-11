package fr.olympa.zta.shops;

import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.zta.utils.npcs.AbstractShop.AbstractSellingShop;

public class BlockShop extends AbstractSellingShop<ItemStack> {

	protected BlockShop(String traitName, String blocksType, DyeColor color, List<AbstractArticle<ItemStack>> articles) {
		super(traitName, "Magasins de blocs" + (blocksType == null ? "" : " - " + blocksType), "Blocs" + (blocksType == null ? "" : " - " + blocksType), color, articles);
	}

	@Override
	public ItemStack getItemStack(ItemStack object) {
		return object.clone();
	}

	@Override
	public void give(ItemStack object, Player p, int amount) {
		SpigotUtils.giveItems(p, ItemUtils.clone(object, amount));
	}
	
	protected static AbstractArticle<ItemStack> prepare(Material material, double price) {
		return new Article<>(new ItemStack(material), price);
	}
	
	protected static AbstractArticle<ItemStack> prepare(ItemStack item, double price) {
		return new Article<>(item, price);
	}
	
}
