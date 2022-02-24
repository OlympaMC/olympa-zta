package fr.olympa.zta.primes;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.olympa.api.spigot.editor.TextEditor;
import fr.olympa.api.spigot.editor.parsers.MoneyAmountParser;
import fr.olympa.api.spigot.editor.parsers.PlayerParser;
import fr.olympa.api.spigot.gui.Inventories;
import fr.olympa.api.spigot.gui.OlympaGUI;
import fr.olympa.api.spigot.gui.templates.PagedView;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;

public class PrimesGUI extends PagedView<Prime> {
	
	public static final NumberFormat numberFormat = new DecimalFormat("0");
	private static final ItemStack primeItem, primeUnavailableItem;
	
	static {
		primeItem = new ItemStack(Material.STICK);
		ItemMeta meta = primeItem.getItemMeta();
		meta.setCustomModelData(1);
		meta.addEnchant(Enchantment.DURABILITY, 1, true);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.setDisplayName("§d§lDéposer une prime");
		meta.setLore(SpigotUtils.wrapAndAlign("Choisis le nom du joueur et la quantité d'Omegas transmis à celui qui le tuera.", 30));
		primeItem.setItemMeta(meta);
		
		primeUnavailableItem = new ItemStack(Material.STICK);
		meta = primeUnavailableItem.getItemMeta();
		meta.setCustomModelData(1);
		meta.setDisplayName("§c§mDéposer une prime");
		List<String> lore = SpigotUtils.wrapAndAlign("Choisis le nom du joueur et la quantité d'Omegas transmis à celui qui le tuera.", 30);
		lore.add("");
		lore.add("§8> §cVous avez déjà fait une prime.");
		meta.setLore(lore);
		primeUnavailableItem.setItemMeta(meta);
	}
	
	private OlympaPlayerZTA player;
	private boolean canStart;
	
	protected PrimesGUI(OlympaPlayerZTA player, List<Prime> bounties) {
		super(DyeColor.RED, bounties);
		this.player = player;
		
		canStart = bounties.stream().noneMatch(x -> x.getBuyer().getId() == player.getId());
	}
	
	@Override
	public void init() {
		super.init();
		right.setItem(2, canStart ? primeItem : primeUnavailableItem);
	}
	
	@Override
	protected void setObjectItem(int mainSlot, Prime object) {
		left.setItem(mainSlot, getItemStack(object));
		ItemStack item = left.getItem(mainSlot);
		ItemUtils.skull(item::setItemMeta, item, object.getTarget().getName());
	}
	
	@Override
	public ItemStack getItemStack(Prime prime) {
		List<String> lore = SpigotUtils.wrapAndAlign("Enchère déposée\npar §n" + prime.getBuyer().getName(), 30);
		lore.add("");
		lore.add("§8> §7Expire dans " + Utils.durationToString(numberFormat, prime.getExpiration() - System.currentTimeMillis()));
		if (player.getId() == prime.getBuyer().getId()) {
			lore.add("");
			lore.add("§6> §eClique pour annuler §6<");
		}
		return ItemUtils.item(Material.PLAYER_HEAD, "§d" + prime.getTarget().getName() + "§7 : §l" + prime.getBountyFormatted(), lore.toArray(String[]::new));
	}
	
	@Override
	public void click(Prime prime, Player p, ClickType click) {
		if (prime.getBuyer().getId() == player.getId()) {
			OlympaZTA.getInstance().primes.removePrime(prime, () -> {
				Prefix.DEFAULT_GOOD.sendMessage(p, "La prime a été supprimée. Tu as récupéré tes %s.", prime.getBountyFormatted());
				player.getGameMoney().give(prime.getBounty());
				
				itemChanged();
				canStart = true;
				right.setItem(2, primeItem);
			}, null);
		}
	}
	
	@Override
	protected boolean onBarItemClick(Player p, ItemStack current, int barSlot, ClickType click) {
		if (canStart && barSlot == 2) {
			Prefix.DEFAULT_GOOD.sendMessage(p, "Écris le nom du joueur sur lequel mettre ta prime.");
			Inventory inv = p.getOpenInventory().getTopInventory();
			new TextEditor<>(p, target -> {
				if (target == p) {
					Prefix.BAD.sendMessage(p, "Tu ne vas pas mettre ta propre tête à prix...");
					p.openInventory(inv);
				}else {
					Prefix.DEFAULT_GOOD.sendMessage(p, "Écris le prix auquel mettre la tête de %s à prix.", target.getName());
					OlympaPlayerZTA player = OlympaPlayerZTA.get(p);
					new TextEditor<>(p, bounty -> {
						if (player.getGameMoney().withdraw(bounty)) {
							OlympaZTA.getInstance().getTask().runTaskAsynchronously(() -> {
								try {
									OlympaZTA.getInstance().primes.addPrime(player, OlympaPlayerZTA.get(target), bounty);
									Prefix.DEFAULT_GOOD.sendMessage(p, "Ta prime a été créée !");
									super.itemChanged();
									OlympaZTA.getInstance().getTask().runTask(() -> p.openInventory(inv));
								}catch (SQLException e) {
									e.printStackTrace();
									Prefix.ERROR.sendMessage(p, "Une erreur est survenue pendant la création de ta prime...");
									OlympaZTA.getInstance().getTask().runTask(() -> Inventories.closeAndExit(p));
								}
							});
						}else {
							Prefix.BAD.sendMessage(p, "Tu n'as pas assez d'argent.");
							Inventories.closeAndExit(p);
						}
					}, () -> p.openInventory(inv), false, new MoneyAmountParser(player, 500, Double.MAX_VALUE)).enterOrLeave();
				}
			}, () -> p.openInventory(inv), false, PlayerParser.PLAYER_PARSER).enterOrLeave();
		}
		return true;
	}
	
	public OlympaGUI toGUI() {
		return super.toGUI("Primes", 5);
	}
	
}
