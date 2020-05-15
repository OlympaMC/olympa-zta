package fr.olympa.zta.plots.players;

import java.util.stream.Collectors;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.editor.TextEditor;
import fr.olympa.api.editor.parsers.OlympaPlayerParser;
import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.gui.templates.PagedGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;

public class PlayerPlotGUI extends OlympaGUI {

	private final static int[] ANIMATION_SLOTS_ORDER = { 0, 1, 2, 5, 8, 7, 6, 3 };

	private final PlayerPlotsManager manager = OlympaZTA.getInstance().plotsManager;
	private final OlympaPlayerZTA player;

	private PlayerPlot plot;

	private BukkitTask progress;
	private boolean manage;
	private boolean isChief;

	public PlayerPlotGUI(OlympaPlayerZTA player) {
		super("Tom Hook", InventoryType.DISPENSER);
		this.player = player;
		this.plot = player.getPlot();
		this.isChief = plot == null ? false : plot.getOwner() == player.getId();

		setState();
	}

	private void setState() {
		inv.clear();
		Material color = Material.CYAN_STAINED_GLASS_PANE;
		if (manage) {
			if (plot.getLevel() < PlayerPlot.questsRequiredPerLevel.length) {
				inv.setItem(1, ItemUtils.item(Material.GOLD_INGOT, "§eMonter de niveau", "§8> §o" + PlayerPlot.questsRequiredPerLevel[plot.getLevel()] + " quêtes nécessaires"));
			}else inv.setItem(1, ItemUtils.item(Material.GOLD_INGOT, "§c§mMonter de niveau", "§8> §o vous avez atteint le niveau maximal"));
			inv.setItem(6, ItemUtils.item(Material.PAPER, "§eInviter un joueur"));
			inv.setItem(8, ItemUtils.item(Material.FILLED_MAP, "§cÉjecter des joueurs"));
			color = Material.MAGENTA_STAINED_GLASS_PANE;
		}else {
			ItemStack center = null;
			if (plot == null) {
				if (player.plotFind != null) {
					color = Material.RED_STAINED_GLASS_PANE;
					center = ItemUtils.item(Material.REDSTONE, "§aNous préparons ta parcelle...");
					startProgress();
				}else center = ItemUtils.item(Material.STONE
						, "§e§lClic droit : §eAcheter ma parcelle", "§8> §o" + PlayerPlot.questsRequiredPerLevel[0] + " quêtes nécessaires"
						, "§e§lClic gauche : §eVoir mes invitations", "§8> §o " + manager.getInvitations(player).size() + " invitations");
			}else {
				color = Material.LIME_STAINED_GLASS_PANE;
				center = ItemUtils.item(Material.DIAMOND, "§e§lClic droit : §eMe téléporter", "§8> §oVous transporte à votre parcelle");
				if (isChief) {
					ItemUtils.loreAdd(center, "§e§lClic gauche : §eGérer ma parcelle", "§8> §o monter de niveau, inviter des joueurs");
				}
			}
			inv.setItem(4, center);
		}
		for (int i = 0; i < 9; i++) if (inv.getItem(i) == null) inv.setItem(i, ItemUtils.item(color, "§a"));
	}

	private void startProgress() {
		progress = new BukkitRunnable() {
			int animState = -1;
			int attemps = 30;
			@Override
			public void run() {
				if (attemps <= 0 && player.plotFind == null) {
					cancel();
					progress = null;
					plot = player.getPlot();
					isChief = true;
					setState();
					return;
				}
				if (animState != -1) inv.getItem(ANIMATION_SLOTS_ORDER[animState]).setType(Material.RED_STAINED_GLASS_PANE);
				if (++animState == ANIMATION_SLOTS_ORDER.length) animState = 0;
				inv.getItem(ANIMATION_SLOTS_ORDER[animState]).setType(Material.LIME_STAINED_GLASS_PANE);
				attemps--;
			}
		}.runTaskTimer(OlympaZTA.getInstance(), 0, 3L);
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (manage) {
			if (slot == 1 && plot.getLevel() < PlayerPlot.questsRequiredPerLevel.length) {
				plot.setLevel(plot.getLevel() + 1, true);
				Prefix.DEFAULT_GOOD.sendMessage(p, "Ta parcelle a monté de niveau !");
				manage = false;
				setState();
			}else if (slot == 6) {
				Prefix.DEFAULT.sendMessage(p, "Entre le nom du joueur que tu souhaites inviter.");
				new TextEditor<>(p, (target) -> {
					manager.invite(target, plot);
					Prefix.DEFAULT_GOOD.sendMessage(p, "Tu viens d'inviter " + target.getName() + " a rejoindre ta parcelle !");
					Prefix.DEFAULT_GOOD.sendMessage(target.getPlayer(), p.getName() + " t'as invité à rejoindre sa parcelle !");
					create(p);
				}, () -> create(p), false, OlympaPlayerParser.<OlympaPlayerZTA>parser()).enterOrLeave();
			}else if (slot == 8) {
				new PagedGUI<OlympaPlayerInformations>("Liste des invités", DyeColor.MAGENTA, plot.getPlayers().stream().map(x -> AccountProvider.getPlayerInformations(x)).collect(Collectors.toList()), (x) -> PlayerPlotGUI.super.create(p)) {

					@Override
					public ItemStack getItemStack(OlympaPlayerInformations object) {
						return ItemUtils.skull("§e" + object.getName(), object.getName(), "§8> §oéjecter le joueur");
					}

					@Override
					public void click(OlympaPlayerInformations existing, Player p) {
						plot.kick(existing);
						super.removeItem(existing);
						Prefix.DEFAULT_GOOD.sendMessage(p, "Tu viens d'expulser " + existing.getName() + " de ta parcelle.");
					}

				}.create(p);
			}
			return true;
		}

		if (slot != 4) return true;
		if (plot == null && progress == null) {
			if (click.isRightClick()) {
				manager.initSearch(player);
				setState();
			}else if (click.isLeftClick()) {
				new PagedGUI<PlayerPlot>("Liste des invitations", DyeColor.CYAN, manager.getInvitations(player)) {

					@Override
					public ItemStack getItemStack(PlayerPlot object) {
						return ItemUtils.item(Material.STONE_BRICKS, "§eParcelle de §l" + AccountProvider.getPlayerInformations(object.getOwner()).getName());
					}

					@Override
					public void click(PlayerPlot existing, Player p) {
						existing.addPlayer(player);
						plot = existing;
						setState();
						PlayerPlotGUI.super.create(p);
					}

				};
			}
		}else {
			if (click.isRightClick() || !isChief) {
				p.closeInventory();
				p.teleport(plot.getLocation().toLocation().add(0, 2, 0));
			}else if (click.isLeftClick()) {
				manage = true;
				setState();
			}
		}
		return true;
	}

	@Override
	public boolean onClose(Player p) {
		if (progress != null) progress.cancel();
		return true;
	}

}
