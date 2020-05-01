package fr.olympa.zta.plots.players;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;

public class PlayerPlotGUI extends OlympaGUI {

	private final static int[] ANIMATION_SLOTS_ORDER = { 0, 1, 2, 5, 8, 7, 6, 3 };

	private OlympaPlayerZTA player;
	private PlayerPlot plot;

	private BukkitTask progress;

	public PlayerPlotGUI(OlympaPlayerZTA player) {
		super("Tom Hook", InventoryType.DISPENSER);
		this.player = player;
		this.plot = player.getPlot();

		setState();
	}

	private void setState() {
		ItemStack center = null;
		Material color = Material.CYAN_STAINED_GLASS_PANE;
		if (plot == null) {
			if (player.plotFind != null) {
				center = ItemUtils.item(Material.REDSTONE, "§aNous préparons ta parcelle...");
				color = Material.RED_STAINED_GLASS_PANE;
				startProgress();
			}else center = ItemUtils.item(Material.STONE, "§eAcheter ma parcelle", "§8" + PlayerPlot.questsRequiredPerLevel[0] + " quêtes nécessaires");
		}else {
			center = ItemUtils.item(Material.DIAMOND, "§e§lClic droit : §eMe téléporter", "§8> §oVous transporte à votre parcelle");
			if (plot.getLevel() < PlayerPlot.questsRequiredPerLevel.length) {
				ItemUtils.loreAdd(center, "§e§lClic gauche : §eMonter de niveau", "§8> §o" + PlayerPlot.questsRequiredPerLevel[plot.getLevel()] + " quêtes nécessaires");
			}
		}
		for (int i = 0; i < 9; i++) inv.setItem(i, ItemUtils.item(color, "§a"));
		inv.setItem(4, center);
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
		if (slot != 4) return true;
		if (plot == null && progress == null) {
			OlympaZTA.getInstance().plotsManager.initSearch(player);
			setState();
		}else {
			if (click.isRightClick()) {
				p.closeInventory();
				p.teleport(plot.getLocation().toLocation().add(0, 2, 0));
			}else if (click.isLeftClick() && plot.getLevel() < PlayerPlot.questsRequiredPerLevel.length) {
				plot.setLevel(plot.getLevel() + 1, true);
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
