package fr.olympa.zta.shops;

import java.util.Arrays;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.zta.itemstackable.Bandage;
import fr.olympa.zta.itemstackable.Brouilleur;
import fr.olympa.zta.itemstackable.ItemStackable;
import fr.olympa.zta.utils.npcs.AbstractShop.AbstractSellingShop;
import fr.olympa.zta.weapons.guns.GunType;

public class GunShop extends AbstractSellingShop<ItemStackable> {
	
	private static final List<AbstractArticle<ItemStackable>> ARTICLES = Arrays.asList(
			new Article<>(GunType.M1897, 100, false),
			new Article<>(GunType.COBRA, 300, false),
			new Article<>(GunType.KSG, 500, false),
			new Article<>(GunType.BENELLI, 600, false),
			new Article<>(GunType.SKORPION, 9000, false),
			new Article<>(GunType.M16, 1700, false),
			new Article<>(GunType.SDMR, 3000, false),
			new Article<>(GunType.DRAGUNOV, 65000, false),
			new Article<>(Bandage.BANDAGE, 130, false),
			new Article<>(Brouilleur.BROUILLEUR, 5500, false)
			);
	
	public GunShop() {
		super("gunShop", "Armurerie", "Armurerie", DyeColor.ORANGE, ARTICLES);
	}
	
	@Override
	protected void give(ItemStackable object, Player p, int amount) {
		SpigotUtils.giveItems(p, object.createItem());
	}
	
	@Override
	public ItemStack getItemStack(ItemStackable object) {
		return object.getDemoItem().clone();
	}
	
}
