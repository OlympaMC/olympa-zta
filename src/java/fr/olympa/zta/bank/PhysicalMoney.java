package fr.olympa.zta.bank;

import java.util.StringJoiner;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import fr.olympa.api.spigot.economy.OlympaMoney;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.zta.OlympaZTA;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class PhysicalMoney {

	public static final NamespacedKey BANKNOTE_KEY = new NamespacedKey(OlympaZTA.getInstance(), "banknote");
	
	public static final ItemStack BANKNOTE_1 = createBanknote(Material.NAUTILUS_SHELL, 1);
	public static final ItemStack BANKNOTE_10 = createBanknote(Material.HEART_OF_THE_SEA, 10);
	public static final ItemStack BANKNOTE_100 = createBanknote(Material.PHANTOM_MEMBRANE, 100);
	
	private static ItemStack createBanknote(Material material, int money) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§eBillet de §6§l" + money + "§6" + OlympaMoney.OMEGA);
		meta.getPersistentDataContainer().set(BANKNOTE_KEY, PersistentDataType.INTEGER, money);
		meta.setCustomModelData(1);
		item.setItemMeta(meta);
		return item;
	}
	
	public static boolean withdraw(Player p, int amount, boolean checkAmount) {
		if (amount == 0) return true;
		if (checkAmount && getPlayerMoney(p) < amount) {
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cVous n'avez pas assez d'argent."));
			return false;
		}
		Inventory inv = p.getInventory();
		
		amount -= remove(inv, 1, amount);
		if (amount <= 0) {
			give(p, -amount); // si enlevé plus que nécessaire, rendre la monnaie manquante
			return true;
		}
		
		amount -= remove(inv, 10, (int) Math.ceil(amount / 10D)) * 10;
		if (amount <= 0){
			give(p, -amount); // si enlevé plus que nécessaire, rendre la monnaie manquante
			return true;
		}
		
		amount -= remove(inv, 100, (int) Math.ceil(amount / 100D)) * 100;
		if (amount <= 0){
			give(p, -amount);
			return true;
		}
		return true;
	}
	
	private static int remove(Inventory inv, int banknote, int max) {
		int removed = 0;
		ItemStack[] items = inv.getContents();
		for (int slot = 0; slot < items.length; slot++) {
			ItemStack item = items[slot];
			if (item == null) continue;
			if (!item.hasItemMeta()) continue;
			PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
			if (data.has(BANKNOTE_KEY, PersistentDataType.INTEGER) && data.get(BANKNOTE_KEY, PersistentDataType.INTEGER).intValue() == banknote) {
				if (item.getAmount() > max - removed) {
					item.setAmount(item.getAmount() - max + removed);
					removed = max;
				}else {
					removed += item.getAmount();
					inv.setItem(slot, new ItemStack(Material.AIR));
				}
				if (removed == max) break;
			}
		}
		return removed;
	}
	
	public static void give(Player p, int amount){
		if (amount == 0) return;

		int b100 = (int) Math.floor((double) amount / 100); // calcule le nombre maximal de billets de 100 à ajouter
		amount -= b100 * 100; // enlève la valeur de ces billets de la quantité totale
		if (b100 > 0) SpigotUtils.giveItems(p, getBanknote(BANKNOTE_100, b100)); // ajoute les items
		if (amount == 0) return; // si il ne reste plus de sous à rajouter, termine

		int b10 = (int) Math.floor((double) amount / 10D);
		amount -= b10 * 10;
		if (b10 > 0) SpigotUtils.giveItems(p, getBanknote(BANKNOTE_10, b10));
		if (amount == 0) return;

		SpigotUtils.giveItems(p, getBanknote(BANKNOTE_1, amount)); // pas besoin de calculer le reste, si le code en vient ici c'est qu'il reste moins de la valeur d'un billet de 10 donc tout ajouter
	}
	
	public static ItemStack getBanknote(ItemStack originalBankNote, int amount) {
		if (amount <= 0) return null;
		ItemStack is = originalBankNote.clone();
		is.setAmount(amount);
		return is;
	}
	
	public static boolean isMoneyItem(ItemStack is){
		if (is == null) return false;
		if (!is.hasItemMeta()) return false;
		PersistentDataContainer data = is.getItemMeta().getPersistentDataContainer();
		return data.has(BANKNOTE_KEY, PersistentDataType.INTEGER);
	}
	
	public static int getPlayerMoney(Player p){
		int money = 0;
		for (ItemStack item : p.getInventory().getContents()) {
			if (item == null) continue;
			if (!item.hasItemMeta()) continue;
			PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
			if (data.has(BANKNOTE_KEY, PersistentDataType.INTEGER)) money += data.get(BANKNOTE_KEY, PersistentDataType.INTEGER).intValue() * item.getAmount();
		}
		return money;
	}
	
	public static String toString(int amount){
		StringJoiner joiner = new StringJoiner(", ");

		int b100 = (int) Math.floor((double) amount / 100D); // calcule le nombre maximal de billets de 100 à ajouter
		amount -= b100 * 100; // enlève la valeur de ces billets de la quantité totale
		if (b100 != 0) joiner.add(b100 + " billets de 100"); // si il y a des billets de 100, mettre "x blocs d'or"
		if (amount == 0) return joiner.toString(); // si il ne reste plus de sous à rajouter, termine en retournant la chaîne

		int b10 = (int) Math.floor((double) amount / 10D); // idem
		amount -= b10 * 10;
		if (b10 != 0) joiner.add(b10 + " billets de 10");
		if (amount == 0) return joiner.toString();

		joiner.add(amount + " billets de 1"); // le reste c'est des billets de 1
		return joiner.toString(); // retourne la chaîne complète
	}
	
}
