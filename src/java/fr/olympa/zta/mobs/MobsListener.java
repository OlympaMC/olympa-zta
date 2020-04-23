package fr.olympa.zta.mobs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.packetslistener.PacketHandlers;
import fr.olympa.zta.packetslistener.PacketInjector;
import fr.olympa.zta.weapons.ArmorType;

public class MobsListener implements Listener {

	private static int lastId = 0;
	static Map<Integer, ItemStack[]> inventories = new HashMap<>(50);

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		int id = lastId++;
		Player p = e.getEntity();
		ItemStack[] contents = p.getInventory().getContents();
		for (int i = 0; i < contents.length; i++) {
			ItemStack itemStack = contents[i];
			if (itemStack != null && itemStack.getType().name().startsWith("LEATHER_")) contents[i] = null;
		}
		inventories.put(id, contents);
		Zombie momifiedZombie = Mobs.spawnMomifiedZombie(p);
		momifiedZombie.setMetadata("inventory", new FixedMetadataValue(OlympaZTA.getInstance(), id));
		momifiedZombie.setMetadata("player", new FixedMetadataValue(OlympaZTA.getInstance(), p.getName()));
		EntityDamageEvent cause = p.getLastDamageCause();
		if (cause instanceof EntityDamageByEntityEvent) {
			Entity damager = ((EntityDamageByEntityEvent) cause).getDamager();
			if (damager instanceof Zombie) {
				if (damager.hasMetadata("player")) {
					e.setDeathMessage("§6§l" + p.getName() + "§r§e s'est fait tuer par le cadavre zombifié de §6" + damager.getMetadata("player").get(0).asString() + "§e.");
				}else e.setDeathMessage("§6§l" + p.getName() + "§r§e s'est fait tuer par un §6zombie§e.");
			}else {
				e.setDeathMessage("§6§l" + p.getName() + "§r§e s'est fait tuer par §6" + damager.getName() + "§e.");
			}
		}else e.setDeathMessage("§6§l" + p.getName() + "§r§e est mort.");
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {
		e.getDrops().clear();
		if (e.getEntity().hasMetadata("inventory")) {
			int id = e.getEntity().getMetadata("inventory").get(0).asInt();
			Collections.addAll(e.getDrops(), inventories.remove(id));
		}
	}

	@EventHandler (priority = EventPriority.LOW)
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		PacketInjector.addPlayer(p, PacketHandlers.REMOVE_SNOWBALLS);
		PacketInjector.addPlayer(p, PacketHandlers.ITEM_DROP);

		p.setHealth(p.getHealth());

		if (!p.hasPlayedBefore()) ArmorType.CIVIL.setFull(p);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		PacketInjector.removePlayer(e.getPlayer());
	}

}
