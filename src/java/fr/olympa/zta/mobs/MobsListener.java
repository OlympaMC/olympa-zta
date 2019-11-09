package fr.olympa.zta.mobs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import fr.olympa.zta.OlympaZTA;

public class MobsListener implements Listener {

	static Map<Integer, ItemStack[]> inventories = new HashMap<>(50);

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		int id = inventories.size();
		inventories.put(id, e.getEntity().getInventory().getContents());
		Mobs.spawnMomifiedZombie(e.getEntity()).setMetadata("inventory", new FixedMetadataValue(OlympaZTA.getInstance(), id));
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {
		e.getDrops().clear();
		if (e.getEntity().hasMetadata("inventory")) {
			int id = e.getEntity().getMetadata("inventory").get(0).asInt();
			Collections.addAll(e.getDrops(), inventories.get(id));
			inventories.put(id, null);
		}
	}

	@EventHandler
	public void onEntityCombust(EntityCombustEvent e) {
		if (e.getEntityType() == EntityType.ZOMBIE) e.setCancelled(true);
	}

}
