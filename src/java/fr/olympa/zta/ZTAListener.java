package fr.olympa.zta;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.olympa.zta.packetslistener.PacketHandlers;
import fr.olympa.zta.packetslistener.PacketInjector;
import fr.olympa.zta.weapons.Weapon;
import fr.olympa.zta.weapons.WeaponsRegistry;
import fr.olympa.zta.weapons.guns.Gun;
import fr.olympa.zta.weapons.guns.bullets.Bullet;

public class ZTAListener implements Listener{
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e){
		PacketInjector.addPlayer(e.getPlayer(), PacketHandlers.REMOVE_SNOWBALLS);
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e){
		PacketInjector.removePlayer(e.getPlayer());
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent e){
		if (!e.getEntity().hasMetadata("bullet")) return;
		((Bullet) e.getEntity().getMetadata("bullet").get(0).value()).hit(e);
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e){
		if (e.getItem() == null || e.getHand() == EquipmentSlot.OFF_HAND || e.getAction() == Action.PHYSICAL) return;
		
		Weapon weapon = WeaponsRegistry.getWeapon(e.getItem());
		if (weapon != null) weapon.onInteract(e);
	}
	
	public static boolean cancelDamageEvent = false; // dommage causé par le contact d'une balle
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e){
		if (cancelDamageEvent) {
			cancelDamageEvent = false;
			return;
		}
		if (!(e.getDamager() instanceof Player) || e.getCause() != DamageCause.ENTITY_ATTACK || !(e.getEntity() instanceof LivingEntity)) return;
		Player damager = (Player) e.getDamager();
		
		ItemStack item = damager.getInventory().getItemInMainHand();
		if (item == null) return;
		
		Weapon weapon = WeaponsRegistry.getWeapon(item);
		if (weapon != null) weapon.onEntityHit(e);
	}
	
	@EventHandler
	public void onHeld(PlayerItemHeldEvent e){
		Player p = e.getPlayer();
		Inventory inv = p.getInventory();
		
		ItemStack item = inv.getItem(e.getPreviousSlot());
		if (item != null) {
			Weapon weapon = WeaponsRegistry.getWeapon(item);
			if (weapon instanceof Gun) ((Gun) weapon).itemNoLongerHeld(p, item);
		}
		
		item = inv.getItem(e.getNewSlot());
		if (item != null) {
			Weapon weapon = WeaponsRegistry.getWeapon(item);
			if (weapon instanceof Gun) ((Gun) weapon).itemHeld(p, item);
		}
	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent e){
		ItemStack item = e.getItemDrop().getItemStack();
		
		Weapon weapon = WeaponsRegistry.getWeapon(item);
		if (weapon != null) weapon.onDrop(e);
	}
	
}
