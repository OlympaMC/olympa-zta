package fr.olympa.zta.utils;

import java.util.StringJoiner;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.utils.spigot.SpigotUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class PhysicalMoney {

	public static final ItemStack BANKNOTE_1 = ItemUtils.item(Material.BRICK, "§eBillet de 1");
	public static final ItemStack BANKNOTE_10 = ItemUtils.item(Material.NETHER_BRICK, "§eBillet de 10");
	public static final ItemStack BANKNOTE_100 = ItemUtils.item(Material.PHANTOM_MEMBRANE, "§eBillet de 100");
	
	public static boolean withdraw(Player p, int amount, boolean checkAmount) {
		if (amount == 0) return true;
		if (checkAmount && getPlayerMoney(p) < amount) {
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cVous n'avez pas assez d'argent."));
			return false;
		}
		Inventory inv = p.getInventory();
		
		int b1 = SpigotUtils.getItemAmount(inv, BANKNOTE_1);
		if (b1 != 0) SpigotUtils.removeItems(inv, getBanknote(BANKNOTE_1, b1)); // enlève le total de billet de 1
		amount -= b1; // réduit la quantité d'or à prendre
		if (amount <= 0) return true;
		
		int b10 = SpigotUtils.getItemAmount(inv, BANKNOTE_10) * 10;
		if (b10 != 0) SpigotUtils.removeItems(inv, getBanknote(BANKNOTE_10, b10));
		amount -= b10; // enlève la valeur du total de pièces
		if (amount <= 0){
			give(p, Math.abs(amount)); // si enlevé plus que nécessaire, rendre la monnaie manquante
			return true;
		}
		int b100 = SpigotUtils.getItemAmount(inv, BANKNOTE_100) * 100;
		if (b100 != 0) SpigotUtils.removeItems(inv, getBanknote(BANKNOTE_100, b100));
		amount -= b100; // idem
		if (amount <= 0){
			give(p, Math.abs(amount));
			return true;
		}
		return true;
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
		return is.isSimilar(BANKNOTE_1) || is.isSimilar(BANKNOTE_10) || is.isSimilar(BANKNOTE_100);
	}
	
	public static int getPlayerMoney(Player p){
		Inventory inv = p.getInventory();
		return SpigotUtils.getItemAmount(inv, BANKNOTE_1) + SpigotUtils.getItemAmount(inv, BANKNOTE_10) * 10 + SpigotUtils.getItemAmount(inv, BANKNOTE_100) * 100;
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
