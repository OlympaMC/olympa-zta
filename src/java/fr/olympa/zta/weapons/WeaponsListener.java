package fr.olympa.zta.weapons;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.weapons.guns.GunRegistry;
import fr.olympa.zta.weapons.guns.bullets.Bullet;

public class WeaponsListener implements Listener {
	
	public static final NamespacedKey KNIFE_KEY = new NamespacedKey(OlympaZTA.getInstance(), "knife");
	public static final NamespacedKey GRENADE_KEY = new NamespacedKey(OlympaZTA.getInstance(), "grenade");

	public static boolean cancelDamageEvent = false; // dommage causé par le contact d'une balle

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		if (cancelDamageEvent) {
			cancelDamageEvent = false;
			return;
		}
		if (e.isCancelled()) return;
		if (!(e.getDamager() instanceof Player) || e.getCause() != DamageCause.ENTITY_ATTACK || !(e.getEntity() instanceof LivingEntity)) return;
		Player damager = (Player) e.getDamager();

		ItemStack item = damager.getInventory().getItemInMainHand();

		Weapon weapon = getWeapon(item);
		if (weapon != null) weapon.onEntityHit(e);
	}

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent e) {
		if (!e.getEntity().hasMetadata("bullet")) return;
		try {
			((Bullet) e.getEntity().getMetadata("bullet").get(0).value()).hit(e);
		}catch (ClassCastException | NullPointerException ex) {} // ça arrive quand des balles étaient présentes dans des chunks qui ont été unloadé pendant le runtime
	}

	@EventHandler (priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.useItemInHand() == Result.DENY) return;
		if (e.getHand() == EquipmentSlot.OFF_HAND || e.getAction() == Action.PHYSICAL) return;

		Weapon weapon = getWeapon(e.getItem());
		if (weapon != null) weapon.onInteract(e);
	}

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if (e.getClick() != ClickType.RIGHT) return;
		if (e.getClickedInventory() != e.getWhoClicked().getInventory()) return;
		
		ItemStack item = e.getCurrentItem();
		ItemStack cursor = e.getCursor();
		if (item == null) return;
		if (cursor != null && cursor.getType() != Material.AIR) return;

		OlympaZTA.getInstance().gunRegistry.ifGun(item, gun -> {
			gun.itemClick((Player) e.getWhoClicked(), item);
			e.setCancelled(true);
		});
	}

	@EventHandler
	public void onHeld(PlayerItemHeldEvent e) {
		Player p = e.getPlayer();
		Inventory inv = p.getInventory();

		ItemStack item = inv.getItem(e.getPreviousSlot());
		Weapon previous = getWeapon(item);
		if (previous != null) previous.itemNoLongerHeld(p, item);
		item = inv.getItem(e.getNewSlot());
		Weapon next = getWeapon(item);
		if (next != null) next.itemHeld(p, item, previous);
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void onDrop(PlayerDropItemEvent e) {
		if (!e.isCancelled()) checkHeld(e.getPlayer(), e.getItemDrop().getItemStack(), false);
	}

	@EventHandler
	public void onPickup(EntityPickupItemEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (p.getInventory().getItemInMainHand().getType() == Material.AIR) {
				ItemStack item = e.getItem().getItemStack();
				Bukkit.getScheduler().runTask(OlympaZTA.getInstance(), () -> {
					if (p.getInventory().getItemInMainHand().isSimilar(item)) {
						checkHeld(p, item, true);
					}
				});
			}
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		checkHeld(e.getEntity(), e.getEntity().getInventory().getItemInMainHand(), false);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		checkHeld(e.getPlayer(), e.getPlayer().getInventory().getItemInMainHand(), false);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		checkHeld(e.getPlayer(), e.getPlayer().getInventory().getItemInMainHand(), true);
	}
	
	public static Weapon getWeapon(ItemStack item) {
		if (item == null) return null;
		if (!item.hasItemMeta()) return null;
		ItemMeta meta = item.getItemMeta();
		if (meta.getPersistentDataContainer().has(KNIFE_KEY, PersistentDataType.INTEGER)) {
			return Knife.values()[meta.getPersistentDataContainer().get(KNIFE_KEY, PersistentDataType.INTEGER)];
		}else if (meta.getPersistentDataContainer().has(GRENADE_KEY, PersistentDataType.INTEGER)) {
			return Grenade.values()[meta.getPersistentDataContainer().get(GRENADE_KEY, PersistentDataType.INTEGER)];
		}else if (meta.getPersistentDataContainer().has(GunRegistry.GUN_KEY, PersistentDataType.INTEGER)) {
			return OlympaZTA.getInstance().gunRegistry.getGun(meta.getPersistentDataContainer().get(GunRegistry.GUN_KEY, PersistentDataType.INTEGER));
		}
		return null;
	}

	private Weapon checkHeld(Player p, ItemStack item, boolean held) {
		Weapon weapon = getWeapon(item);
		if (weapon != null) {
			if (held) {
				weapon.itemHeld(p, item, null);
			}else weapon.itemNoLongerHeld(p, item);
		}
		return weapon;
	}
	
}
