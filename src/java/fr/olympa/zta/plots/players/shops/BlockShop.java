package fr.olympa.zta.plots.players.shops;

import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.zta.utils.AbstractShop;

public class BlockShop extends AbstractShop<Material> {

	protected BlockShop(String traitName, String shopName, DyeColor color, List<Article<Material>> articles) {
		super(traitName, shopName, color, articles);
	}

	@Override
	public ItemStack getItemStack(Material object) {
		return new ItemStack(object);
	}

	@Override
	public void click(Material object, Player p) {
		p.getInventory().addItem(new ItemStack(object));
	}

}
