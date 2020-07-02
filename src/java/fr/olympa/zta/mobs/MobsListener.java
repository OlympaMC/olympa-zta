package fr.olympa.zta.mobs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.mobs.custom.Mobs;
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
		OlympaPlayerZTA op = OlympaPlayerZTA.get(p);
		op.deaths++;
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
		String reason = "est mort.";
		if (cause instanceof EntityDamageByEntityEvent) {
			Entity damager = ((EntityDamageByEntityEvent) cause).getDamager();
			if (damager instanceof Zombie) {
				if (damager.hasMetadata("player")) {
					reason = "s'est fait tuer par le cadavre zombifié de §6" + damager.getMetadata("player").get(0).asString() + "§e.";
				}else reason = "s'est fait tuer par un §6infecté§e.";
			}else {
				reason = "s'est fait tuer par §6" + damager.getName() + "§e.";
			}
		}else if (cause.getCause() == DamageCause.DROWNING) {
			reason = "s'est noyé.";
		}
		e.setDeathMessage("§6§l" + p.getName() + "§r§e " + reason);
		p.setMetadata("lastDeath", new FixedMetadataValue(OlympaZTA.getInstance(), p.getLocation()));
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {
		e.getDrops().clear();
		e.setDroppedExp(0);
		
		LivingEntity entity = e.getEntity();
		if (entity.hasMetadata("inventory")) {
			int id = entity.getMetadata("inventory").get(0).asInt();
			Collections.addAll(e.getDrops(), inventories.remove(id));
		}
		if (entity.getKiller() != null) {
			OlympaPlayerZTA killer = OlympaPlayerZTA.get(entity.getKiller());
			if (entity instanceof Player) {
				killer.killedPlayers++;
			}else if (entity.getType() == EntityType.ZOMBIE) {
				killer.killedZombies++;
			}
		}
	}

	@EventHandler (priority = EventPriority.LOW)
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		PacketInjector.addPlayer(p, PacketHandlers.REMOVE_SNOWBALLS);
		PacketInjector.addPlayer(p, PacketHandlers.ITEM_DROP);

		p.setHealth(p.getHealth());
		p.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(16);

		if (!p.hasPlayedBefore()) ArmorType.CIVIL.setFull(p);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		PacketInjector.removePlayer(e.getPlayer());
	}

}
