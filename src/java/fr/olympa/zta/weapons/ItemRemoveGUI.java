package fr.olympa.zta.weapons;

import java.util.Spliterator;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import fr.olympa.api.spigot.gui.templates.ItemsDropGUI;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.weapons.guns.GunRegistry;

public class ItemRemoveGUI extends ItemsDropGUI {
	
	public ItemRemoveGUI() {
		super("Dépose les items à supprimer", 2);
	}
	
	private int objects = 0; // si dans la méthode ils doivent être final...
	private int guns = 0;
	private int gunsUnloaded = 0;
	
	@Override
	protected boolean done(Player p, ClickType click, Spliterator<ItemStack> items) {
		items.forEachRemaining(item -> {
			if (item != null) {
				objects++;
				if (item.hasItemMeta()) {
					ItemMeta meta = item.getItemMeta();
					if (meta.getPersistentDataContainer().has(GunRegistry.GUN_KEY, PersistentDataType.INTEGER)) {
						if (OlympaZTA.getInstance().gunRegistry.removeObject(meta.getPersistentDataContainer().get(GunRegistry.GUN_KEY, PersistentDataType.INTEGER))) {
							guns++;
						}else gunsUnloaded++;
					}
				}
			}
		});
		Prefix.DEFAULT_GOOD.sendMessage(p, "%d items supprimés, dont %d guns (et %d guns déchargés).", objects, guns, gunsUnloaded);
		return false;
	}
	
}
