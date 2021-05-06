package fr.olympa.zta.primes;

import java.sql.SQLException;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.economy.OlympaMoney;
import fr.olympa.api.editor.TextEditor;
import fr.olympa.api.editor.parsers.MoneyAmountParser;
import fr.olympa.api.editor.parsers.PlayerParser;
import fr.olympa.api.gui.Inventories;
import fr.olympa.api.gui.templates.PagedGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.weapons.Knife;

public class PrimesGUI extends PagedGUI<Prime> {
	
	protected PrimesGUI(List<Prime> bounties) {
		super("Primes", DyeColor.RED, bounties, 5);
		
		setBarItem(2, ItemUtils.name(Knife.BATTE.createItem(), "§d§lDéposer une prime"));
	}
	
	@Override
	protected void setItem(int mainSlot, Prime object) {
		int slot = setMainItem(mainSlot, getItemStack(object));
		ItemStack item = inv.getItem(slot);
		ItemUtils.skull(item::setItemMeta, item, object.getTarget().getName());
	}
	
	@Override
	public ItemStack getItemStack(Prime object) {
		return ItemUtils.item(Material.PLAYER_HEAD, "§d" + object.getTarget().getName() + "§7 : §l" + OlympaMoney.format(object.getBounty()), SpigotUtils.wrapAndAlign("Enchère déposée\npar §n" + object.getBuyer().getName(), 30).toArray(String[]::new));
	}
	
	@Override
	public void click(Prime existing, Player p, ClickType click) {
		Prefix.DEFAULT.sendMessage(p, "§oEn travaux...");
	}
	
	@Override
	protected boolean onBarItemClick(Player p, ItemStack current, int barSlot, ClickType click) {
		if (barSlot == 2) {
			Prefix.DEFAULT_GOOD.sendMessage(p, "Écris le nom du joueur sur lequel mettre ta prime.");
			new TextEditor<>(p, target -> {
				Prefix.DEFAULT_GOOD.sendMessage(p, "Écris le prix auquel mettre la tête de %s à prix.", target.getName());
				OlympaPlayerZTA player = OlympaPlayerZTA.get(p);
				new TextEditor<>(p, bounty -> {
					if (player.getGameMoney().withdraw(bounty)) {
						OlympaZTA.getInstance().getTask().runTaskAsynchronously(() -> {
							try {
								OlympaZTA.getInstance().primes.addPrime(player, OlympaPlayerZTA.get(target), bounty);
								Prefix.DEFAULT_GOOD.sendMessage(p, "Ta prime a été créée !");
								p.openInventory(inv);
							}catch (SQLException e) {
								Prefix.ERROR.sendMessage(p, "Une erreur est survenue pendant la création de ta prime...");
								Inventories.closeAndExit(p);
								e.printStackTrace();
							}
						});
					}else {
						Prefix.DEFAULT_BAD.sendMessage(p, "Tu n'as pas assez d'argent.");
						Inventories.closeAndExit(p);
					}
				}, () -> p.openInventory(inv), false, new MoneyAmountParser(player)).enterOrLeave();
			}, () -> p.openInventory(inv), false, PlayerParser.PLAYER_PARSER).enterOrLeave();
		}
		return true;
	}
	
}
