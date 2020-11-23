package fr.olympa.zta.mobs;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
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
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import fr.olympa.api.customevents.AsyncPlayerMoveRegionsEvent;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.RandomizedPicker;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.lootchests.creators.AmmoCreator;
import fr.olympa.zta.lootchests.creators.FoodCreator;
import fr.olympa.zta.lootchests.creators.FoodCreator.Food;
import fr.olympa.zta.lootchests.creators.LootCreator;
import fr.olympa.zta.lootchests.creators.MoneyCreator;
import fr.olympa.zta.mobs.custom.Mobs;
import fr.olympa.zta.mobs.custom.Mobs.Zombies;
import fr.olympa.zta.packetslistener.PacketHandlers;
import fr.olympa.zta.weapons.ArmorType;
import fr.olympa.zta.weapons.Knife;
import fr.olympa.zta.weapons.guns.AmmoType;

public class MobsListener implements Listener {

	private RandomizedPicker<LootCreator> zombieLoots = new RandomizedPicker.FixedPicker<>(0, 1, 20,
			new AmmoCreator(20, 2, 3),
			new MoneyCreator(45, 3, 9),
			new FoodCreator(15, Food.BAKED_POTATO, 3, 5),
			new AmmoCreator(12, AmmoType.LIGHT, 2, 3, false),
			new AmmoCreator(12, AmmoType.HEAVY, 2, 3, false),
			new AmmoCreator(12, AmmoType.HANDWORKED, 2, 3, false),
			new AmmoCreator(5, AmmoType.CARTRIDGE, 1, 2, false)
			);
	private Map<Player, List<ItemStack>> keptItems = new HashMap<>();
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		OlympaPlayerZTA op = OlympaPlayerZTA.get(p);
		op.deaths.increment();
		List<ItemStack> kept = new ArrayList<>();
		ItemStack[] contents = p.getInventory().getContents();
		for (int i = 0; i < contents.length; i++) {
			ItemStack itemStack = contents[i];
			if (itemStack != null) {
				if (OlympaZTA.getInstance().beautyQuestsLink != null && OlympaZTA.getInstance().beautyQuestsLink.isQuestItem(itemStack)) {
					kept.add(itemStack);
					contents[i] = null;
				}else if (itemStack.getType().name().startsWith("LEATHER_") || Knife.BATTE.isItem(itemStack)) {
					contents[i] = null; // désactive la sauvegarde du stuff de base (armure civile en cuir)
				}
			}
		}
		keptItems.put(p, kept);
		Location loc = p.getLocation();
		ItemStack[] armor = p.getInventory().getArmorContents();
		Bukkit.getScheduler().runTask(OlympaZTA.getInstance(), () -> {
			Zombie momifiedZombie = Mobs.spawnMomifiedZombie(loc, armor, contents, "§7" + p.getName() + " momifié");
			momifiedZombie.setMetadata("player", new FixedMetadataValue(OlympaZTA.getInstance(), p.getName()));
		});
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
		Prefix.DEFAULT.sendMessage(p, "§oVotre cadavre a repris vie en %d %d %d...", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		e.setDeathMessage("§6§l" + p.getName() + "§r§e " + reason);
		e.getDrops().clear();
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {
		LivingEntity entity = e.getEntity();
		
		if (entity.getKiller() != null) {
			OlympaPlayerZTA killer = OlympaPlayerZTA.get(entity.getKiller());
			if (entity instanceof Player) {
				killer.killedPlayers.increment();
			}else {
				if (!entity.hasMetadata("ztaZombieType")) return;
				Zombies zombie = (Zombies) entity.getMetadata("ztaZombieType").get(0).value();
				if (zombie == Zombies.COMMON || zombie == Zombies.DROWNED) {
					killer.killedZombies.increment();
					for (LootCreator creator : zombieLoots.pick(ThreadLocalRandom.current())) {
						e.getDrops().add(creator.create(entity.getKiller(), ThreadLocalRandom.current()).getItem());
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		giveStartItems(e.getPlayer());
		List<ItemStack> items = keptItems.remove(e.getPlayer());
		if (items != null) e.getPlayer().getInventory().addItem(items.toArray(ItemStack[]::new));
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onDamage(EntityDamageEvent e) {
		if (e.getCause() == DamageCause.FALL) {
			e.setDamage(e.getDamage() / 2);
		}else if (e.getCause() == DamageCause.ENTITY_EXPLOSION) {
			if (e.getEntity() instanceof Item) {
				e.setCancelled(true);
			}else if (e.getEntity() instanceof Player) {
				e.setDamage(e.getDamage() / 2);
			}
		}
		if (!e.isCancelled() && e.getEntity() instanceof Item) {
			OlympaZTA.getInstance().gunRegistry.ifGun(((Item) e.getEntity()).getItemStack(), OlympaZTA.getInstance().gunRegistry::removeObject);
		}
	}

	@EventHandler (priority = EventPriority.LOW)
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		for (PacketHandlers handler : PacketHandlers.values()) handler.addPlayer(p);

		p.setHealth(p.getHealth());
		p.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(16);
		p.setWalkSpeed(0.21f);
		
		for (AmmoType ammoType : AmmoType.values()) {
			p.discoverRecipe(ammoType.getRecipe().getKey());
		}
		
		Bukkit.getScheduler().runTaskAsynchronously(OlympaZTA.getInstance(), () -> {
			try {
				OlympaZTA.getInstance().sendMessage("§6%d §eobjet(s) chargés depuis l'inventaire de §6%s§e.", OlympaZTA.getInstance().gunRegistry.loadFromItems(e.getPlayer().getInventory().getContents()), p.getName());
			}catch (SQLException e1) {
				e1.printStackTrace();
			}
		});

		if (!p.hasPlayedBefore()) {
			p.teleport(OlympaZTA.getInstance().hub.getSpawnpoint());
			p.sendTitle("§eOlympa §6§lZTA", "§eBienvenue !", 2, 50, 7);
			giveStartItems(p);
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		for (PacketHandlers handler : PacketHandlers.values()) handler.removePlayer(p);
		Bukkit.getScheduler().runTaskAsynchronously(OlympaZTA.getInstance(), () -> OlympaZTA.getInstance().gunRegistry.launchEvictItems(e.getPlayer().getInventory().getContents()));
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

	@EventHandler
	public void onItemRemove(ItemDespawnEvent e) {
		OlympaZTA.getInstance().getTask().runTaskAsynchronously(() -> OlympaZTA.getInstance().gunRegistry.itemRemove(e.getEntity().getItemStack()));
	}
	
	@EventHandler
	public void onPlayerMoveRegions(AsyncPlayerMoveRegionsEvent e) {
		OlympaZTA.getInstance().lineRadar.updateHolder(OlympaZTA.getInstance().scoreboards.getPlayerScoreboard(OlympaPlayerZTA.get(e.getPlayer())));
	}
	
	private void giveStartItems(Player p) {
		ArmorType.CIVIL.setFull(p);
		p.getInventory().addItem(Food.BAKED_POTATO.get(10), Knife.BATTE.createItem());
	}
	
}
