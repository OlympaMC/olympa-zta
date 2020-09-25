package fr.olympa.zta.utils.npcs;

import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.olympa.api.economy.OlympaMoney;
import fr.olympa.api.gui.templates.PagedGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaPlayerZTA;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;

public abstract class AbstractShop<T> extends Trait {

	private List<Article<T>> articles;
	
	private String shopName;
	private DyeColor color;

	protected AbstractShop(String traitName, String shopName, DyeColor color, List<Article<T>> articles) {
		super(traitName);
		this.shopName = shopName;
		this.color = color;
		this.articles = articles;
	}

	public abstract ItemStack getItemStack(T object);
	
	public abstract void click(Article<T> article, Player p);

	@EventHandler
	public void onRightClick(NPCRightClickEvent e) {
		if (e.getNPC() == super.npc) new ShopGUI().create(e.getClicker());
	}

	class ShopGUI extends PagedGUI<Article<T>> {

		public ShopGUI() {
			super(shopName, color, articles, Math.min(6, Math.max((int) Math.ceil(articles.size() / 9D), 3)));
		}

		@Override
		public ItemStack getItemStack(Article<T> object) {
			ItemStack item = AbstractShop.this.getItemStack(object.object);
			int size = -1;
			if (item.hasItemMeta()) {
				ItemMeta meta = item.getItemMeta();
				if (meta.hasLore()) size = meta.getLore().stream().mapToInt(String::length).max().getAsInt();
			}
			if (size == -1) size = ItemUtils.getName(item).length() - 4;
			String bar = "§m" + " ".repeat(size / 2);
			return ItemUtils.loreAdd(item, "", "§e" + bar + "§e[ §6§l" + OlympaMoney.format(object.price) + "§e ]" + bar + "§r");
		}

		@Override
		public void click(Article<T> existing, Player p) {
			AbstractShop.this.click(existing, p);
		}

	}

	public static class Article<T> {
		public final T object;
		public final double price;

		public Article(T object, double price) {
			this.object = object;
			this.price = price;
		}
	}
	
	public static abstract class AbstractSellingShop<T> extends AbstractShop<T> {
		
		protected AbstractSellingShop(String traitName, String shopName, DyeColor color, List<Article<T>> articles) {
			super(traitName, shopName, color, articles);
		}
		
		@Override
		public void click(Article<T> article, Player p) {
			OlympaPlayerZTA player = OlympaPlayerZTA.get(p);
			if (player.getGameMoney().withdraw(article.price)) {
				give(article.object, p);
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
			}else {
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				Prefix.DEFAULT_BAD.sendMessage(p, "Tu n'as pas assez d'argent pour acheter cet objet.");
			}
		}
		
		protected abstract void give(T object, Player p);
		
	}
	
	public static abstract class AbstractBuyingShop<T> extends AbstractShop<T> {
		
		protected AbstractBuyingShop(String traitName, String shopName, DyeColor color, List<Article<T>> articles) {
			super(traitName, shopName, color, articles);
		}
		
		@Override
		public void click(Article<T> article, Player p) {
			OlympaPlayerZTA player = OlympaPlayerZTA.get(p);
			if (take(article.object, p)) {
				player.getGameMoney().give(article.price);
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
			}else {
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				Prefix.DEFAULT_BAD.sendMessage(p, "Tu ne peux pas vendre cet objet.");
			}
		}
		
		protected abstract boolean take(T object, Player p);
		
	}

}
