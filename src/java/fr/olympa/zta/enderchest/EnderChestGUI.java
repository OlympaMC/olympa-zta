package fr.olympa.zta.enderchest;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.zta.OlympaPlayerZTA;

public class EnderChestGUI extends OlympaGUI {

	private OlympaPlayerZTA player;
	private boolean change = false;

	public EnderChestGUI(OlympaPlayerZTA player) {
		super("Enderchest de " + player.getName(), 1);
		this.player = player;
		inv.setContents(player.getEnderChest());
	}

	public boolean onClickCursor(Player p, ItemStack current, ItemStack cursor, int slot) {
		change = true;
		return false;
	}

	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		change = true;
		return false;
	}

	public boolean onClose(Player p) {
		if (change) {
			player.setEnderChest(inv.getContents());
		}
		return true;
	}

}
