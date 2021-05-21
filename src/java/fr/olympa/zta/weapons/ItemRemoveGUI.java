package fr.olympa.zta.weapons;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import fr.olympa.api.gui.Inventories;
import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.weapons.guns.GunRegistry;

public class ItemRemoveGUI extends OlympaGUI {
	
	public ItemRemoveGUI() {
		super("Dépose les items à supprimer", 3);
		
		for (int i = 18; i < 27; i++) {
			inv.setItem(i, i == 22 ? ItemUtils.done : ItemUtils.itemSeparator(DyeColor.PURPLE));
		}
	}
	
	@Override
	public boolean onMoveItem(Player p, ItemStack moved) {
		return false;
	}
	
	@Override
	public boolean onClickCursor(Player p, ItemStack current, ItemStack cursor, int slot) {
		return slot >= 18;
	}
	
	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (slot == 22) {
			Inventories.closeAndExit(p);
			int items = 0;
			int guns = 0;
			int gunsUnloaded = 0;
			for (int i = 0; i < 18; i++) {
				ItemStack item = inv.getItem(i);
				if (item != null) {
					items++;
					if (item.hasItemMeta()) {
						ItemMeta meta = item.getItemMeta();
						if (meta.getPersistentDataContainer().has(GunRegistry.GUN_KEY, PersistentDataType.INTEGER)) {
							if (OlympaZTA.getInstance().gunRegistry.removeObject(meta.getPersistentDataContainer().get(GunRegistry.GUN_KEY, PersistentDataType.INTEGER))) {
								guns++;
							}else gunsUnloaded++;
						}
					}
				}
			}
			Prefix.DEFAULT_GOOD.sendMessage(p, "%d items supprimés, dont %d guns (et %d guns déchargés).", items, guns, gunsUnloaded);
		}
		return slot >= 18;
	}
	
}
