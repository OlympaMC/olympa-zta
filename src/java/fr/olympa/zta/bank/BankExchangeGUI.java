package fr.olympa.zta.bank;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.economy.OlympaMoney;
import fr.olympa.api.spigot.gui.OlympaGUI;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaPlayerZTA;

public class BankExchangeGUI extends OlympaGUI {

	private static ItemStack add = ItemUtils.item(Material.GREEN_STAINED_GLASS_PANE, "§a↑ Augmenter le montant de la transaction", "§8§l> Clic gauche : §7Augmenter de 1", "§8§l> Clic droit : §7Augmenter de 10", "§8§l> Clic central : §7Augmenter de 100");
	private static ItemStack remove = ItemUtils.item(Material.RED_STAINED_GLASS_PANE, "§c↓ Diminuer le montant de la transaction", "§8§l> Clic gauche : §7Baisser de 1", "§8§l> Clic droit : §7Baisser de 10", "§8§l> Clic central : §7Baisser de 100");
	private static ItemStack transfer = ItemUtils.item(Material.PRISMARINE_CRYSTALS, 1, "§bDéposer sur son compte en banque", "§8> §oTransfère les billets de votre", " §8§o inventaire à votre compte");
	private static ItemStack withdraw = ItemUtils.item(Material.NAUTILUS_SHELL, 1, "§bRetirer de mon compte", "§8> §oDonne de l'argent de votre", "§8§o compte sous forme de billets");

	private OlympaPlayerZTA player;

	private int amount = 0;

	public BankExchangeGUI(OlympaPlayerZTA player) {
		super("Échanger ma monnaie", 1);
		this.player = player;
		this.player.getGameMoney().observe("bank_gui", this::updateMoney);

		inv.setItem(0, ItemUtils.item(Material.PLAYER_HEAD, "§e§lMa monnaie"));
		updateMoney();
		ItemStack moneyItem = inv.getItem(0);
		ItemUtils.skull(moneyItem::setItemMeta, moneyItem, player.getName());

		inv.setItem(2, add);
		inv.setItem(4, remove);
		inv.setItem(3, ItemUtils.item(Material.PAPER, null));
		updateCounter();

		inv.setItem(7, transfer);
		inv.setItem(8, withdraw);
	}

	private void updateCounter() {
		ItemUtils.name(inv.getItem(3), "§eTransaction: §6§l" + OlympaMoney.format(amount));
	}

	private void updateMoney() {
		ItemUtils.lore(inv.getItem(0), "§8> Compte bancaire : §7§l" + player.getGameMoney().getFormatted(), "§8> Mon porte-feuille : §7§l" + PhysicalMoney.getPlayerMoney((Player) player.getPlayer()) + OlympaMoney.OMEGA);
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (slot == 2 || slot == 4) {
			int toChange = 0;
			if (click.isRightClick()) {
				toChange = 10;
			}else if (click.isLeftClick()) {
				toChange = 1;
			}else if (click == ClickType.MIDDLE) {
				toChange = 100;
			}

			if (slot == 2) {
				amount += toChange;
			}else {
				amount -= toChange;
				if (amount < 0) amount = 0;
			}

			updateCounter();
		}else if (slot == 7) {
			if (amount == 0) return true;
			int money = PhysicalMoney.getPlayerMoney(p);
			int amount = this.amount;
			if (money < amount) amount = money;
			PhysicalMoney.withdraw(p, amount, false);
			player.getGameMoney().give(amount);
			Prefix.DEFAULT_GOOD.sendMessage(p, "Tu as transféré %s sur ton compte en banque.", OlympaMoney.format(amount));
			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
		}else if (slot == 8) {
			if (amount == 0) return true;
			int money = (int) player.getGameMoney().get();
			int amount = this.amount;
			if (money < amount) amount = money;
			player.getGameMoney().withdraw(amount);
			PhysicalMoney.give(p, amount);
			Prefix.DEFAULT_GOOD.sendMessage(p, "Tu as retiré %s de ton compte en banque.", OlympaMoney.format(amount));
			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
		}
		return true;
	}

	@Override
	public boolean onClose(Player p) {
		player.getGameMoney().unobserve("bank_gui");
		return true;
	}
	
	@Override
	public boolean noDoubleClick() {
		return false;
	}

}
