package fr.olympa.zta.mobs;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

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
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.RandomizedPicker;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.lootchests.creators.AmmoCreator;
import fr.olympa.zta.lootchests.creators.LootCreator;
import fr.olympa.zta.lootchests.creators.MoneyCreator;
import fr.olympa.zta.mobs.custom.Mobs;
import fr.olympa.zta.packetslistener.PacketHandlers;
import fr.olympa.zta.packetslistener.PacketInjector;
import fr.olympa.zta.weapons.ArmorType;
import fr.olympa.zta.weapons.guns.AmmoType;

public class MobsListener implements Listener {

	private int lastId = 0;
	public Map<Integer, ItemStack[]> inventories = new HashMap<>(50);
	private RandomizedPicker<LootCreator> zombieLoots = new RandomizedPicker.FixedPicker<>(0, 1, Arrays.asList(new AmmoCreator(50, 1, 3), new MoneyCreator(20, 1, 5)));

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		int id = lastId++;
		Player p = e.getEntity();
		OlympaPlayerZTA op = OlympaPlayerZTA.get(p);
		op.deaths.add(1);
		ItemStack[] contents = p.getInventory().getContents();
		for (int i = 0; i < contents.length; i++) {
			ItemStack itemStack = contents[i];
			if (itemStack != null && itemStack.getType().name().startsWith("LEATHER_")) contents[i] = null; // désactive la sauvegarde du stuff de base (armure civile en cuir)
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
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {
		LivingEntity entity = e.getEntity();
		e.getDrops().clear();
		e.setDroppedExp(0);
		
		if (entity.hasMetadata("inventory")) {
			int id = entity.getMetadata("inventory").get(0).asInt();
			Collections.addAll(e.getDrops(), inventories.remove(id));
		}
		
		if (entity.getKiller() != null) {
			OlympaPlayerZTA killer = OlympaPlayerZTA.get(entity.getKiller());
			if (entity instanceof Player) {
				killer.killedPlayers.add(1);
			}else if (entity.getType() == EntityType.ZOMBIE) {
				killer.killedZombies.add(1);
				for (LootCreator creator : zombieLoots.pick(ThreadLocalRandom.current())) {
					e.getDrops().add(creator.create(entity.getKiller(), ThreadLocalRandom.current()).getItem());
				}
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
		p.setWalkSpeed(0.22f);

		if (!p.hasPlayedBefore()) {
			p.teleport(OlympaZTA.getInstance().hub.getSpawnpoint());
			p.sendTitle("§eOlympa §6§lZTA", "§eBienvenue !", 2, 50, 7);
			ArmorType.CIVIL.setFull(p);
			for (AmmoType ammoType : AmmoType.values()) {
				p.discoverRecipe(ammoType.getRecipe().getKey());
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		PacketInjector.removePlayer(e.getPlayer());
	}
	
	@EventHandler
	public void onResourcePack(PlayerResourcePackStatusEvent e) {
		switch (e.getStatus()) {
		case ACCEPTED:
			Prefix.DEFAULT.sendMessage(e.getPlayer(), "§eChargement du pack de resources §6§lOlympa ZTA§e...");
			break;
		case DECLINED:
			Prefix.BAD.sendMessage(e.getPlayer(), "Tu as désactivé l'utilisation du pack de resources. Pour plus de fun et une meilleure expérience de jeu, accepte-le depuis ton menu Multijoueur !");
			break;
		case FAILED_DOWNLOAD:
			Prefix.ERROR.sendMessage(e.getPlayer(), "Une erreur est survenue lors du téléchargement du pack de resources. Reconnectez-vous pour réessayer !");
			break;
		case SUCCESSFULLY_LOADED:
			Prefix.DEFAULT_GOOD.sendMessage(e.getPlayer(), "Le pack de resources §6§lOlympa ZTA§a est désormais chargé ! Bon jeu !");
			break;
		default:
			break;
		}
	}

}
