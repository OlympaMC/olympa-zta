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

import fr.olympa.api.spigot.economy.OlympaMoney;
import fr.olympa.api.spigot.economy.fluctuating.FluctuatingEconomy;
import fr.olympa.api.spigot.gui.templates.PagedGUI;
import fr.olympa.api.spigot.holograms.Hologram.HologramLine;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.spigot.lines.AbstractLine;
import fr.olympa.api.spigot.lines.BlinkingLine;
import fr.olympa.api.spigot.lines.FixedLine;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import net.citizensnpcs.api.event.NPCRightClickEvent;

public abstract class AbstractShop<T> extends HologramTrait {

	private List<AbstractArticle<T>> articles;
	
	private String shopName;
	private String holoType, holoName;
	private DyeColor color;

	protected AbstractShop(String traitName, String shopName, String holoType, String holoName, DyeColor color, List<AbstractArticle<T>> articles) {
		super(traitName);
		this.shopName = shopName;
		this.holoType = holoType;
		this.holoName = holoName;
		this.color = color;
		this.articles = articles;
	}

	@Override
	protected AbstractLine<HologramLine>[] getLines() {
		return new AbstractLine[] { new BlinkingLine<HologramLine>((color, x) -> color + "§l" + holoName, OlympaZTA.getInstance(), 60, ChatColor.GOLD, ChatColor.YELLOW), new FixedLine<>("§7§n" + holoType) };
	}
	
	public abstract ItemStack getItemStack(T object);
	
	public abstract boolean click(AbstractArticle<T> article, Player p, ClickType click);

	public abstract String[] getLore(AbstractArticle<T> article);
	
	@EventHandler
	public void onRightClick(NPCRightClickEvent e) {
		if (e.getNPC() == super.npc) new ShopGUI().create(e.getClicker());
	}

	class ShopGUI extends PagedGUI<AbstractArticle<T>> {
		
		public ShopGUI() {
			super(shopName, color, articles, Math.min(6, Math.max((int) Math.ceil(articles.size() / 9D), 3)));
		}

		@Override
		public ItemStack getItemStack(AbstractArticle<T> object) {
			ItemStack item = AbstractShop.this.getItemStack(object.object);
			ItemMeta meta = item.getItemMeta();
			List<String> lore = meta.getLore();
			if (lore == null) lore = new ArrayList<>();
			//lore.add("");
			for (String loreLine : AbstractShop.this.getLore(object)) lore.add(loreLine);
			lore.add(/*lore.size() - AbstractShop.this.getLore(object).length, */SpigotUtils.getBarsWithLoreLength(ItemUtils.getName(item), lore, OlympaMoney.format(object.getPrice())));
			meta.setLore(lore);
			item.setItemMeta(meta);
			return item;
		}

		@Override
		public void click(AbstractArticle<T> existing, Player p, ClickType click) {
			if (AbstractShop.this.click(existing, p, click)) setItems();
		}

	}

	public abstract static class AbstractArticle<T> {
		public final T object;
		
		protected AbstractArticle(T object) {
			this.object = object;
		}
		
		public void take(int amount) {}
		
		public boolean needsUpdate() {
			return false;
		}
		
		public abstract double getPrice();
		
		public abstract boolean isStackable();
		
	}
	
	public static class Article<T> extends AbstractArticle<T> {
		public final double price;
		public final boolean stackable;

		public Article(T object, double price) {
			this(object, price, true);
		}
		
		public Article(T object, double price, boolean stackable) {
			super(object);
			this.price = price;
			this.stackable = stackable;
		}
		
		@Override
		public double getPrice() {
			return price;
		}
		
		@Override
		public boolean isStackable() {
			return stackable;
		}
		
	}
	
	public static class FluctuatingArticle<T> extends AbstractArticle<T> {
		
		private FluctuatingEconomy economy;
		
		public FluctuatingArticle(T object, FluctuatingEconomy economy) {
			super(object);
			this.economy = economy;
		}
		
		@Override
		public double getPrice() {
			return economy.getValue();
		}
		
		@Override
		public boolean needsUpdate() {
			return true;
		}
		
		@Override
		public boolean isStackable() {
			return true;
		}
		
		@Override
		public void take(int amount) {
			economy.use(amount * getPrice());
		}
		
	}
	
	public abstract static class AbstractSellingShop<T> extends AbstractShop<T> {
		
		protected AbstractSellingShop(String traitName, String shopName, String holo, DyeColor color, List<AbstractArticle<T>> articles) {
			super(traitName, shopName, "Vente", holo, color, articles);
		}
		
		@Override
		public boolean click(AbstractArticle<T> article, Player p, ClickType click) {
			OlympaPlayerZTA player = OlympaPlayerZTA.get(p);
			int amount = article.isStackable() && click.isShiftClick() ? 64 : 1;
			
			if (!player.getGameMoney().withdraw(article.getPrice() * amount)) {
				if (amount == 1) {
					amount = 0;
				}else {
					amount = (int) Math.floor(player.getGameMoney().get() / article.getPrice());
					if (!player.getGameMoney().withdraw(article.getPrice() * amount)) throw new IllegalStateException();
					Prefix.DEFAULT_GOOD.sendMessage(p, "Tu n'avais d'argent que pour acheter %dx ton item.", amount);
					OlympaZTA.getInstance().getTask().runTask(p::closeInventory);
				}
			}
			
			if (amount > 0) {
				give(article.object, p, amount);
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
			}else {
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				Prefix.DEFAULT_BAD.sendMessage(p, "Tu n'as pas assez d'argent pour acheter cet objet.");
			}
			return false;
		}
		
		@Override
		public String[] getLore(AbstractArticle<T> article) {
			return article.isStackable() ? new String[] { "", "§6➤ §eClic : acheter x1", "§6➤ §eShift+clic : acheter x64" } : new String[] { "", "§6➤ §eClic : acheter x1" };
		}
		
		protected abstract void give(T object, Player p, int amount);
		
	}
	
	public abstract static class AbstractBuyingShop<T> extends AbstractShop<T> {
		
		protected AbstractBuyingShop(String traitName, String shopName, String holo, DyeColor color, List<AbstractArticle<T>> articles) {
			super(traitName, shopName, "Rachat", holo, color, articles);
		}
		
		@Override
		public boolean click(AbstractArticle<T> article, Player p, ClickType click) {
			OlympaPlayerZTA player = OlympaPlayerZTA.get(p);
			int amount = take(article.object, p, click.isShiftClick());
			if (amount > 0) {
				player.getGameMoney().give(amount * article.getPrice());
				article.take(amount);
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
				return article.needsUpdate();
			}else {
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				Prefix.DEFAULT_BAD.sendMessage(p, "Tu ne peux pas vendre cet objet.");
				return false;
			}
		}
		
		@Override
		public String[] getLore(AbstractArticle<T> article) {
			return new String[] { "", "§6➤ §eClic : vendre x1", "§6➤ §eShift+clic : vendre x64" };
		}
		
		protected abstract int take(T object, Player p, boolean shift);
		
	}

}
