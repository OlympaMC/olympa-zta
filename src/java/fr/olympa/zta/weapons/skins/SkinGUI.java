package fr.olympa.zta.weapons.skins;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.gui.OlympaGUI;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.zta.weapons.WeaponsListener;

public class SkinGUI extends OlympaGUI {
	
	public SkinGUI() {
		super("Choisis le skin de ton arme", 5);
		for (int i = 0; i < 45; i++) {
			if (i == 13) continue;
			ItemStack item;
			if (i == 28 || (i >= 30 && i < 35)) {
				item = ItemUtils.item(Material.RED_STAINED_GLASS_PANE, "Dépose une arme");
			}else item = ItemUtils.itemSeparator(DyeColor.GRAY);
			inv.setItem(i, item);
		}
	}
	
	private void setWeapon(ItemStack item) {
		if (WeaponsListener.getWeapon(item) instanceof Skinable skinable) {
			
		}
	}
	
	@Override
	public boolean onMoveItem(Player p, ItemStack moved) {
		setWeapon(moved);
		return false;
	}
	
	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (slot == 13) {
			if (current != null) setWeapon(null);
		}
		return super.onClick(p, current, slot, click);
	}
	
}
