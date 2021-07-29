package fr.olympa.zta.mobs;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent.SlotType;

import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.spigot.customevents.AsyncPlayerAfkEvent;
import fr.olympa.api.spigot.customevents.AsyncPlayerMoveRegionsEvent;
import fr.olympa.api.spigot.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.spigot.holograms.Hologram;
import fr.olympa.api.spigot.lines.FixedLine;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.itemstackable.Artifacts;
import fr.olympa.zta.itemstackable.ItemDropBehavior;
import fr.olympa.zta.itemstackable.ItemStackable;
import fr.olympa.zta.itemstackable.ItemStackableManager;
import fr.olympa.zta.loot.creators.FoodCreator.Food;
import fr.olympa.zta.mobs.MobSpawning.SpawnType.SpawningFlag;
import fr.olympa.zta.mobs.custom.Mobs;
import fr.olympa.zta.packetslistener.PacketHandlers;
import fr.olympa.zta.weapons.ArmorType;
import fr.olympa.zta.weapons.Knife;
import fr.olympa.zta.weapons.guns.AmmoType;
import net.citizensnpcs.api.CitizensAPI;

public class PlayersListener implements Listener {
	
	private static final PotionEffect BOOTS_EFFECT = new PotionEffect(PotionEffectType.JUMP, 99999999, 1, false, false);

	private static final DecimalFormat DAMAGE_FORMAT = new DecimalFormat("0.00");
	
	private static final double ZTA_MAX_HEALTH = 40;
	
	private Map<Player, List<ItemStack>> keptItems = new HashMap<>();
	
	private List<String> playersWithPacks = new ArrayList<>();
	private Map<Player, Location> packPositions = new HashMap<>();
	private Location packWaitingRoom;
	
	private Random random = new Random();
	
	public PlayersListener(Location packWaitingRoom) {
		this.packWaitingRoom = packWaitingRoom;
		
		OlympaCore.getInstance().registerPackListener((player, server, set) -> {
			if (set && server.equals(OlympaCore.getInstance().getServerName())) return;
			playersWithPacks.remove(player);
		});
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		if (CitizensAPI.getNPCRegistry().isNPC(p)) return;
		OlympaPlayerZTA op = OlympaPlayerZTA.get(p);
		op.deaths.increment();
		
		List<ItemStack> kept = new ArrayList<>();
		ItemStack[] contents = p.getInventory().getContents();
		for (int i = 0; i < contents.length; i++) {
			ItemStack itemStack = contents[i];
			if (itemStack != null) {
				ItemDropBehavior behavior;
				ItemStackable stackable = ItemStackableManager.getStackable(itemStack);
				if (stackable != null) {
					behavior = stackable.loot(p, itemStack);
				}else if (OlympaZTA.getInstance().beautyQuestsLink != null && OlympaZTA.getInstance().beautyQuestsLink.isQuestItem(itemStack)) {
					behavior = ItemDropBehavior.KEEP;
				}else if (itemStack.getType().name().startsWith("LEATHER_") || itemStack.getType() == Material.DRIED_KELP) {
					behavior = ItemDropBehavior.DISAPPEAR; // désactive la sauvegarde du stuff de base (armure civile en cuir)
				}else behavior = ItemDropBehavior.DROP;
				
				switch (behavior) {
				case DISAPPEAR:
					contents[i] = null;
					break;
				case KEEP:
					kept.add(itemStack);
					contents[i] = null;
					break;
				case DROP:
				default:
					break;
				}
			}
		}
		keptItems.put(p, kept);
		Location loc = p.getLocation();
		ItemStack[] armor = Arrays.copyOfRange(contents, 36, 40);
		Bukkit.getScheduler().runTask(OlympaZTA.getInstance(), () -> Mobs.spawnMomifiedZombie(loc, armor, contents, p));
		
		EntityDamageEvent cause = p.getLastDamageCause();
		String reason = "est mort.";
		if (cause instanceof EntityDamageByEntityEvent) {
			Entity damager = ((EntityDamageByEntityEvent) cause).getDamager();
			if (damager instanceof Projectile) {
				ProjectileSource source = ((Projectile) damager).getShooter();
				if (source instanceof Entity) damager = (Entity) source;
			}
			if (damager instanceof Drowned) {
				reason = "s'est fait tuer par un §cnoyé§e.";
			}else if (damager instanceof Zombie) {
				if (damager.hasMetadata("player")) {
					reason = "s'est fait tuer par le cadavre zombifié de §6" + damager.getMetadata("player").get(0).asString() + "§e.";
				}else reason = "s'est fait tuer par un §cinfecté§e.";
			}else if (damager instanceof Player) {
				OlympaPlayer oplayer = AccountProviderAPI.getter().get(damager.getUniqueId());
				if (oplayer != null) reason = "s'est fait tuer par " + oplayer.getGroup().getColor() + damager.getName() + "§e.";
			}
		}else if (cause != null && cause.getCause() == DamageCause.DROWNING) {
			reason = "s'est noyé.";
		}
		Prefix.DEFAULT.sendMessage(p, "§oTon cadavre a repris vie en %d %d %d...", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		e.setDeathMessage(op.getGroup().getColor() + p.getName() + "§e " + reason);
		e.getDrops().clear();
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		giveStartItems(e.getPlayer());
		List<ItemStack> items = keptItems.remove(e.getPlayer());
		if (items != null) e.getPlayer().getInventory().addItem(items.toArray(ItemStack[]::new));
	}
	
	@EventHandler (priority = EventPriority.LOW)
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		for (PacketHandlers handler : PacketHandlers.values()) handler.addPlayer(p);
		
		if (p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() != 40) p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(ZTA_MAX_HEALTH);
		
		if (p.getHealth() == 0) {
			p.setHealth(ZTA_MAX_HEALTH);
			Prefix.DEFAULT_BAD.sendMessage(p, "Tu étais encore en combat lors de ta dernière déconnexion...");
			p.teleport(OlympaZTA.getInstance().hub.getSpawnpoint());
		}else p.setHealth(p.getHealth()); // pour update la barre
		
		p.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(20);
		p.setWalkSpeed(0.21f);
		
		for (AmmoType ammoType : AmmoType.values()) {
			p.discoverRecipe(ammoType.getRecipe().getKey());
		}
		
		if (p.hasPlayedBefore()) {
			Bukkit.getScheduler().runTaskAsynchronously(OlympaZTA.getInstance(), () -> {
				try {
					int loaded = OlympaZTA.getInstance().gunRegistry.loadFromItems(e.getPlayer().getInventory().getContents());
					if (loaded != 0) OlympaZTA.getInstance().sendMessage("§6%d §eobjet(s) chargé(s) depuis l'inventaire de §6%s§e.", loaded, p.getName());
				}catch (Exception ex) {
					OlympaZTA.getInstance().sendMessage("§cUne erreur est survenue lors du chargement de l'inventaire de %s.", p.getName());
					ex.printStackTrace();
				}
			});
		}else {
			p.sendTitle("§eOlympa §6§lZTA", "§eBienvenue !", 2, 50, 7);
			p.setHealth(ZTA_MAX_HEALTH);
			giveStartItems(p);
		}
		
		if (!playersWithPacks.contains(p.getName())) {
			OlympaZTA.getInstance().getTask().runTask(() -> {
				p.setResourcePack("https://drive.google.com/uc?export=download&id=1meIjucmWnLxC9hzjNAd8cN7LK2k-M6t7", "BF241BBD1F48F7FD068F32350DC3F4F285F676C1");
			});
		}else OlympaZTA.getInstance().sendMessage("Le joueur %s a déjà son pack de resources pour le ZTA.", p.getName());
	}
	
	@EventHandler
	public void onOlympaPlayerLoad(OlympaPlayerLoadEvent e) {
		int loaded;
		try {
			loaded = OlympaZTA.getInstance().gunRegistry.loadFromItems(e.<OlympaPlayerZTA>getOlympaPlayer().getEnderChestContents());
			if (loaded != 0) OlympaZTA.getInstance().sendMessage("§6%d §eobjet(s) chargé(s) depuis l'enderchest de §6%s§e.", loaded, e.getPlayer().getName());
		}catch (Exception ex) {
			OlympaZTA.getInstance().sendMessage("§cUne erreur est survenue lors du chargement de l'enderchest de %s.", e.getPlayer().getName());
			ex.printStackTrace();
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		Location oldPosition = packPositions.remove(p);
		if (oldPosition != null) p.teleport(oldPosition);
		OlympaPlayerZTA oplayer = AccountProviderAPI.getter().get(p.getUniqueId());
		if (oplayer == null) return;
		Bukkit.getScheduler().runTaskAsynchronously(OlympaZTA.getInstance(), () -> {
			OlympaZTA.getInstance().gunRegistry.launchEvictItems(e.getPlayer().getInventory().getContents());
			OlympaZTA.getInstance().gunRegistry.launchEvictItems(oplayer.getEnderChestContents());
		});
	}
	
	@EventHandler
	public void onResourcePack(PlayerResourcePackStatusEvent e) {
		Player p = e.getPlayer();
		switch (e.getStatus()) {
		case ACCEPTED:
			Location playerLocation = p.getLocation();
			if (p.getHealth() != 0 && !OlympaZTA.getInstance().hub.isInHub(playerLocation)) {
				packPositions.put(p, playerLocation);
				p.teleport(packWaitingRoom);
			}
			Prefix.DEFAULT.sendMessage(p, "§eChargement du pack de resources §6§lOlympa ZTA§e...");
			break;
		case DECLINED:
			Prefix.BAD.sendMessage(p, "Tu as désactivé l'utilisation du pack de resources. Pour plus de fun et une meilleure expérience de jeu, accepte-le depuis ton menu Multijoueur !");
			break;
		case FAILED_DOWNLOAD, SUCCESSFULLY_LOADED:
			if (e.getStatus() == Status.FAILED_DOWNLOAD) {
				Prefix.ERROR.sendMessage(p, "Une erreur est survenue lors du téléchargement du pack de resources. Reconnectez-vous pour réessayer !");
				playersWithPacks.remove(p.getName());
			}else {
				Prefix.DEFAULT_GOOD.sendMessage(p, "Le pack de resources §6§lOlympa ZTA§a est désormais chargé ! Bon jeu !");
				playersWithPacks.add(p.getName());
			}
			Location lastLoc = packPositions.remove(p);
			if (lastLoc != null) {
				Prefix.DEFAULT.sendMessage(p, "Vous allez être envoyé à votre dernière position....");
				p.teleport(lastLoc);
			}
			break;
		default:
			break;
		}
	}
	
	@EventHandler
	public void onJoinLocation(PlayerSpawnLocationEvent e) {
		Player p = e.getPlayer();
		if (!p.hasPlayedBefore()) e.setSpawnLocation(OlympaZTA.getInstance().hub.getSpawnpoint());
	}
	
	@EventHandler
	public void onPlayerMoveRegions(AsyncPlayerMoveRegionsEvent e) {
		if (e.getDifference().stream().anyMatch(region -> region.getFlag(SpawningFlag.class) != null)) {
			OlympaZTA.getInstance().lineRadar.updateHolder(OlympaZTA.getInstance().scoreboards.getPlayerScoreboard(OlympaPlayerZTA.get(e.getPlayer())));
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onHunger(FoodLevelChangeEvent e) {
		if (e.isCancelled() && !OlympaZTA.getInstance().hub.isInHub(e.getEntity().getLocation())) System.out.println("PlayersListener.onHunger() cancelled " + e.isCancelled() + " to " + e.getFoodLevel() + " for " + e.getEntity().getName());
	}
	
	/*@EventHandler (priority = EventPriority.MONITOR)
	public void onInteract(PlayerInteractEvent e) {
		if (!e.isCancelled()) System.out.println("PlayersListener.onInteract() cancelled " + e.isCancelled() + " " + e.getAction().name() + " " + (e.getHand() == null ? "null hand" : e.getHand().name()) + " " + (e.getClickedBlock() == null ? "no block" : "block"));
	}*/
	
	protected boolean isSpeedBoots(ItemStack item) {
		return item != null && item.getType() == Material.DIAMOND_BOOTS && (ItemStackableManager.getStackable(item) == Artifacts.BOOTS);
	}
	
	@EventHandler
	public void onArmor(PlayerArmorChangeEvent e) {
		if (e.getSlotType() != SlotType.FEET) return;
		boolean bootsOld = isSpeedBoots(e.getOldItem());
		boolean bootsNew = isSpeedBoots(e.getNewItem());
		if (bootsOld == bootsNew) return;
		if (bootsOld) {
			e.getPlayer().removePotionEffect(PotionEffectType.JUMP);
		}else {
			e.getPlayer().addPotionEffect(BOOTS_EFFECT);
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onDamage(EntityDamageByEntityEvent e) {
		if (e.isCancelled() || e.getDamage() == 0) return;
		if (!(e.getEntity() instanceof LivingEntity)) return;
		Player damager;
		if (!(e.getDamager() instanceof Player)) {
			if (e.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter)
				damager = shooter;
			else return;
		}else damager = (Player) e.getDamager();
		
		LivingEntity en = (LivingEntity) e.getEntity();
		Location lc = en.getEyeLocation().add(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5);
		Hologram hologram = OlympaCore.getInstance().getHologramsManager().createHologram(lc, false, true, new FixedLine<>("§4-§l" + DAMAGE_FORMAT.format(e.getFinalDamage()) + " §r§c❤"));
		new BukkitRunnable() {
			int i = 0;
			
			@Override
			public void run() {
				if (i < 15) {
					hologram.move(lc.add(0.0D, 0.1D/* + i / 15*/, 0.0D));
					i++;
				}else {
					hologram.remove();
					cancel();
				}
			}
		}.runTaskTimer(OlympaZTA.getInstance(), 20, 1);
		
		OlympaPlayerZTA player = OlympaPlayerZTA.get(damager);
		if (player != null && player.parameterHealthBar.get()) {
			if (player.healthBar == null) player.healthBar = new PlayerHealthBar(damager);
			player.healthBar.show(en, e.getFinalDamage());
		}
	}
	
	@EventHandler
	public void onAFK(AsyncPlayerAfkEvent e) {
		OlympaPlayerZTA player = OlympaPlayerZTA.get(e.getPlayer());
		if (player == null) return;
		if (e.isAfk())
			player.disablePlayTime();
		else
			player.enablePlayTime();
	}
	
	private void giveStartItems(Player p) {
		ArmorType.CIVIL.setFull(p);
		p.getInventory().addItem(Food.DRIED_KELP.get(25), Knife.BATTE.createItem(), AmmoType.LIGHT.getAmmo(5, true));
	}
	
}
