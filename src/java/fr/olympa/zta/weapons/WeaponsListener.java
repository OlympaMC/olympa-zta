package fr.olympa.zta.weapons;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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

import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.registry.Registrable;
import fr.olympa.zta.registry.ZTARegistry;
import fr.olympa.zta.weapons.guns.Gun;
import fr.olympa.zta.weapons.guns.bullets.Bullet;

public class WeaponsListener implements Listener {

	public static boolean cancelDamageEvent = false; // dommage causé par le contact d'une balle

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		if (cancelDamageEvent) {
			cancelDamageEvent = false;
			return;
		}
		if (!(e.getDamager() instanceof Player) || e.getCause() != DamageCause.ENTITY_ATTACK || !(e.getEntity() instanceof LivingEntity)) return;
		Player damager = (Player) e.getDamager();

		ItemStack item = damager.getInventory().getItemInMainHand();
		if (item == null) return;

		Registrable object = ZTARegistry.get().getItemStackable(item);
		if (object != null && object instanceof Weapon) ((Weapon) object).onEntityHit(e);
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
		if (e.getItem() == null || e.getHand() == EquipmentSlot.OFF_HAND || e.getAction() == Action.PHYSICAL) return;

		Registrable object = ZTARegistry.get().getItemStackable(e.getItem());
		if (object != null && object instanceof Weapon) ((Weapon) object).onInteract(e);
	}

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if (e.getClick() != ClickType.RIGHT) return;
		if (e.getClickedInventory() != e.getWhoClicked().getInventory()) return;

		ItemStack item = e.getCurrentItem();
		if (item == null) return;

		Registrable object = ZTARegistry.get().getItemStackable(item);
		if (object instanceof Gun) {
			((Gun) object).itemClick((Player) e.getWhoClicked(), item);
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onHeld(PlayerItemHeldEvent e) {
		Player p = e.getPlayer();
		Inventory inv = p.getInventory();

		checkHeld(p, inv.getItem(e.getPreviousSlot()), false);
		checkHeld(p, inv.getItem(e.getNewSlot()), true);
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		checkHeld(e.getPlayer(), e.getItemDrop().getItemStack(), false);
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

	private void checkHeld(Player p, ItemStack item, boolean held) {
		if (item != null) {
			Registrable object = ZTARegistry.get().getItemStackable(item);
			if (object instanceof Weapon) {
				Weapon weapon = (Weapon) object;
				if (held) {
					weapon.itemHeld(p, item);
				}else weapon.itemNoLongerHeld(p, item);
			}
		}
	}

}
