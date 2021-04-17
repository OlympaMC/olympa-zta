package fr.olympa.zta.mobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import fr.olympa.api.customevents.AsyncPlayerMoveRegionsEvent;
import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.loot.creators.FoodCreator.Food;
import fr.olympa.zta.mobs.custom.Mobs;
import fr.olympa.zta.packetslistener.PacketHandlers;
import fr.olympa.zta.weapons.ArmorType;
import fr.olympa.zta.weapons.Knife;
import fr.olympa.zta.weapons.guns.AmmoType;
import net.citizensnpcs.api.CitizensAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class PlayersListener implements Listener {
	
	//private final PotionEffect PARACHUTE_EFFECT = new PotionEffect(PotionEffectType.SLOW_FALLING, 30, 7, false, false, true);
	
	private Map<Player, List<ItemStack>> keptItems = new HashMap<>();
	
	private Map<Player, Location> packPositions = new HashMap<>();
	private Location packWaitingRoom;
	
	private List<Player> parachuting = new ArrayList<>();
	
	public PlayersListener(Location packWaitingRoom) {
		this.packWaitingRoom = packWaitingRoom;
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
			Zombie momifiedZombie = Mobs.spawnMomifiedZombie(loc, armor, contents, "§7§l" + p.getName() + "§7 momifié");
			momifiedZombie.setMetadata("player", new FixedMetadataValue(OlympaZTA.getInstance(), p.getName()));
		});
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
				OlympaPlayer oplayer = AccountProvider.get(damager.getUniqueId());
				if (oplayer != null) reason = "s'est fait tuer par " + oplayer.getGroup().getColor() + damager.getName() + "§e.";
			}
		}else if (cause != null && cause.getCause() == DamageCause.DROWNING) {
			reason = "s'est noyé.";
		}
		Prefix.DEFAULT.sendMessage(p, "§oVotre cadavre a repris vie en %d %d %d...", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
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
		
		if (p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() != 40) p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40);
		
		if (p.getHealth() == 0) {
			p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
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
			giveStartItems(p);
		}
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
		for (PacketHandlers handler : PacketHandlers.values()) handler.removePlayer(p);
		OlympaPlayerZTA oplayer = AccountProvider.get(p.getUniqueId());
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
		case FAILED_DOWNLOAD:
		case SUCCESSFULLY_LOADED:
			Prefix.DEFAULT_GOOD.sendMessage(p, e.getStatus() == Status.FAILED_DOWNLOAD ? "Une erreur est survenue lors du téléchargement du pack de resources. Reconnectez-vous pour réessayer !" : "Le pack de resources §6§lOlympa ZTA§a est désormais chargé ! Bon jeu !");
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
		OlympaZTA.getInstance().lineRadar.updateHolder(OlympaZTA.getInstance().scoreboards.getPlayerScoreboard(OlympaPlayerZTA.get(e.getPlayer())));
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		//System.out.println("PlayersListener.onMove() " + p.getFallDistance() + " " + p.isOnGround());
		if (p.getFallDistance() > 3) {
			if (p.getInventory().getChestplate() != null && (p.getInventory().getChestplate().getType() == Material.DIAMOND_CHESTPLATE)) {
				//p.addPotionEffect(PARACHUTE_EFFECT);
				Vector velocity = p.getVelocity();
				if (velocity.getY() > -0.1) return;
				velocity.setY(velocity.getY() * 0.8);
				p.setVelocity(velocity);
				//p.setFallDistance(0);
				p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§b▶ ▷ §e§lParachute déployé§b ◁ ◀"));
			}
		}
	}
	
	private void giveStartItems(Player p) {
		ArmorType.CIVIL.setFull(p);
		p.getInventory().addItem(Food.BAKED_POTATO.get(10), Knife.BATTE.createItem(), AmmoType.LIGHT.getAmmo(5, true));
	}
	
}
