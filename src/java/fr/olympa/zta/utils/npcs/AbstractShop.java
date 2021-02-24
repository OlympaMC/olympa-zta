package fr.olympa.zta.utils.npcs;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.olympa.api.economy.OlympaMoney;
import fr.olympa.api.gui.templates.PagedGUI;
import fr.olympa.api.holograms.Hologram.HologramLine;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.lines.AbstractLine;
import fr.olympa.api.lines.BlinkingLine;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import net.citizensnpcs.api.event.NPCRightClickEvent;

public abstract class AbstractShop<T> extends HologramTrait {

	private List<Article<T>> articles;
	
	private String shopName;
	private String holoType, holoName;
	private DyeColor color;

	protected AbstractShop(String traitName, String shopName, String holoType, String holoName, DyeColor color, List<Article<T>> articles) {
		super(traitName);
		this.shopName = shopName;
		this.holoType = holoType;
		this.holoName = holoName;
		this.color = color;
		this.articles = articles;
	}

	@Override
	protected AbstractLine<HologramLine>[] getLines() {
		return new AbstractLine[] { new BlinkingLine<HologramLine>((color, x) -> color + "§l" + holoName, OlympaZTA.getInstance(), 60, ChatColor.GOLD, ChatColor.YELLOW), new FixedLine<>("§8§n" + holoType) };
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
			ItemMeta meta = item.getItemMeta();
			List<String> lore = meta.getLore();
			if (lore == null) lore = new ArrayList<>();
			lore.add("");
			lore.add(SpigotUtils.getBarsWithLoreLength(ItemUtils.getName(item), lore, OlympaMoney.format(object.price)));
			meta.setLore(lore);
			item.setItemMeta(meta);
			return item;
		}

		@Override
		public void click(Article<T> existing, Player p, ClickType click) {
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
		
		protected AbstractSellingShop(String traitName, String shopName, String holo, DyeColor color, List<Article<T>> articles) {
			super(traitName, shopName, "Vente", holo, color, articles);
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
		
		protected AbstractBuyingShop(String traitName, String shopName, String holo, DyeColor color, List<Article<T>> articles) {
			super(traitName, shopName, "Rachat", holo, color, articles);
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
