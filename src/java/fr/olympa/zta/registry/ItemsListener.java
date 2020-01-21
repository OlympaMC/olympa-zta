package fr.olympa.zta.registry;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;

public class ItemsListener implements Listener {

	@EventHandler
	public void onItemRemove(ItemDespawnEvent e) {
		ItemStackable itemStackable = ZTARegistry.getItemStackable(e.getEntity().getItemStack());
		ZTARegistry.removeObject(itemStackable);
	}

}
