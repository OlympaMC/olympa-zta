package fr.olympa.zta.shops;

import java.util.Arrays;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.zta.utils.npcs.AbstractShop.AbstractSellingShop;
import fr.olympa.zta.weapons.guns.GunType;

public class GunShop extends AbstractSellingShop<GunType> {
	
	private static final List<AbstractArticle<GunType>> ARTICLES = Arrays.asList(
			new Article<>(GunType.BENELLI, 500),
			new Article<>(GunType.M1897, 500));
	
	public GunShop() {
		super("gunShop", "Armurerie", "Armurerie", DyeColor.ORANGE, ARTICLES);
	}
	
	@Override
	protected void give(GunType object, Player p) {
		SpigotUtils.giveItems(p, object.createItem());
	}
	
	@Override
	public ItemStack getItemStack(GunType object) {
		return object.getDemoItem().clone();
	}
	
}
