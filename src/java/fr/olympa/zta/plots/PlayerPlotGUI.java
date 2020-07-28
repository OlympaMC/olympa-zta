package fr.olympa.zta.plots;

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
import fr.olympa.api.utils.observable.ObservableList;
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
				inv.setItem(0, ItemUtils.item(Material.GOLD_INGOT, "§eMonter de niveau", "§8> §o" + PlayerPlot.questsRequiredPerLevel[plot.getLevel()] + " quêtes nécessaires"));
			}else inv.setItem(0, ItemUtils.item(Material.GOLD_INGOT, "§c§mMonter de niveau", "§8> §o vous avez atteint le niveau maximal"));
			inv.setItem(1, ItemUtils.item(Material.PAPER, "§eInviter un joueur"));
			inv.setItem(2, ItemUtils.item(Material.FILLED_MAP, "§cÉjecter des joueurs"));
			inv.setItem(4, ItemUtils.item(Material.ARROW, "§a← Revenir au menu"));
			color = Material.MAGENTA_STAINED_GLASS_PANE;
		}else {
			ItemStack center = null;
			if (plot == null) {
				if (player.plotFind != null) {
					color = Material.RED_STAINED_GLASS_PANE;
					center = ItemUtils.item(Material.REDSTONE, "§aNous préparons ta parcelle...");
					startProgress();
				}else center = ItemUtils.item(Material.STONE
						, "§e§lClic gauche : §eAcheter ma parcelle", "§8> §o" + PlayerPlot.questsRequiredPerLevel[0] + " quêtes nécessaires", "§e§lClic droit : §eVoir mes invitations", "§8> §o" + manager.getInvitations(player).size() + " invitations");
			}else {
				color = Material.LIME_STAINED_GLASS_PANE;
				center = ItemUtils.item(Material.DIAMOND, "§e§lClic gauche : §eMe téléporter", "§8> §oVous transporte à votre parcelle");
				if (isChief) {
					ItemUtils.loreAdd(center, "§e§lClic droit : §eGérer ma parcelle", "§8> §oMonter de niveau, inviter ou exclure des joueurs");
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
			if (slot == 0 && plot.getLevel() < PlayerPlot.questsRequiredPerLevel.length) {
				plot.setLevel(plot.getLevel() + 1, true);
				Prefix.DEFAULT_GOOD.sendMessage(p, "Ta parcelle a monté de niveau !");
				manage = false;
				setState();
			}else if (slot == 1) {
				Prefix.DEFAULT.sendMessage(p, "Entre le nom du joueur que tu souhaites inviter.");
				new TextEditor<>(p, (target) -> {
					if (target == player) {
						Prefix.DEFAULT_BAD.sendMessage(p, "Tu ne peux pas t'inviter toi-même...");
					}else {
						manager.invite(target, plot);
						Prefix.DEFAULT_GOOD.sendMessage(p, "Tu viens d'inviter " + target.getName() + " a rejoindre ta parcelle !");
						Prefix.DEFAULT_GOOD.sendMessage(target.getPlayer(), p.getName() + " t'as invité à rejoindre sa parcelle !");
					}
					create(p);
				}, () -> create(p), false, OlympaPlayerParser.<OlympaPlayerZTA>parser()).enterOrLeave();
			}else if (slot == 2) {
				new PlotGuestsGUI(plot).create(p);
			}else if (slot == 4) {
				manage = false;
				setState();
			}
			return true;
		}

		if (slot != 4) return true;
		if (plot == null && progress == null) {
			if (click.isLeftClick()) {
				manager.initSearch(player);
				setState();
			}else if (click.isRightClick()) {
				new PlotInvitationsGUI(manager.getInvitations(player)).create(p);
			}
		}else {
			if (click.isLeftClick() || !isChief) {
				p.closeInventory();
				p.teleport(plot.getSpawnLocation());
			}else if (click.isRightClick()) {
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

	private final class PlotInvitationsGUI extends PagedGUI<PlayerPlot> {
		private PlotInvitationsGUI(ObservableList<PlayerPlot> objects) {
			super("Liste des invitations", DyeColor.CYAN, objects, 3);
			setBarItem(2, ItemUtils.item(Material.DIAMOND, "§aRevenir au menu"));
		}

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

		@Override
		protected boolean onBarItemClick(Player p, ItemStack current, int barSlot, ClickType click) {
			PlayerPlotGUI.super.create(p);
			return true;
		}
	}

	private class PlotGuestsGUI extends PagedGUI<OlympaPlayerInformations> {
		private PlotGuestsGUI(PlayerPlot plot) {
			super("Liste des invités", DyeColor.MAGENTA, plot.getPlayers().stream().filter(x -> x != plot.getOwner()).map(x -> AccountProvider.getPlayerInformations(x)).collect(Collectors.toList()), 5);
			setBarItem(2, ItemUtils.item(Material.DIAMOND, "§aRevenir au menu"));
		}

		@Override
		public ItemStack getItemStack(OlympaPlayerInformations object) {
			ItemUtils.skull(item -> super.updateObjectItem(object, item), "§e" + object.getName(), object.getName(), "§8> §oÉjecter le joueur");
			return ItemUtils.none;
		}

		@Override
		public void click(OlympaPlayerInformations existing, Player p) {
			plot.kick(existing);
			super.removeItem(existing);
			Prefix.DEFAULT_GOOD.sendMessage(p, "Tu viens d'expulser " + existing.getName() + " de ta parcelle.");
		}

		@Override
		protected boolean onBarItemClick(Player p, ItemStack current, int barSlot, ClickType click) {
			PlayerPlotGUI.super.create(p);
			return true;
		}
	}

}
