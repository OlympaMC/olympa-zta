package fr.olympa.zta.shops;

import java.util.Arrays;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.zta.itemstackable.Bandage;
import fr.olympa.zta.itemstackable.ItemStackable;
import fr.olympa.zta.utils.npcs.AbstractShop.AbstractSellingShop;
import fr.olympa.zta.weapons.guns.GunType;

public class GunShop extends AbstractSellingShop<ItemStackable> {
	
	private static final List<AbstractArticle<ItemStackable>> ARTICLES = Arrays.asList(
			new Article<>(GunType.M1897, 100),
			new Article<>(GunType.COBRA, 300),
			new Article<>(GunType.KSG, 500),
			new Article<>(GunType.BENELLI, 600),
			new Article<>(GunType.SKORPION, 9000),
			new Article<>(GunType.M16, 1700),
			new Article<>(GunType.SDMR, 3000),
			new Article<>(GunType.DRAGUNOV, 65000),
			new Article<>(Bandage.BANDAGE, 75)
			);
	
	public GunShop() {
		super("gunShop", "Armurerie", "Armurerie", DyeColor.ORANGE, ARTICLES);
	}
	
	@Override
	protected void give(ItemStackable object, Player p) {
		SpigotUtils.giveItems(p, object.createItem());
	}
	
	@Override
	public ItemStack getItemStack(ItemStackable object) {
		return object.getDemoItem().clone();
	}
	
}
