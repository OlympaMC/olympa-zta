package fr.olympa.zta.plots.players;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;

public class PlayerPlotGUI extends OlympaGUI {

	public PlayerPlotGUI() {
		super("Acheter une parcelle", 3);
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		return false;
	}

}
