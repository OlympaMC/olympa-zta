package fr.olympa.zta.weapons;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import fr.olympa.api.spigot.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.itemstackable.Bandage;
import fr.olympa.zta.itemstackable.Brouilleur;
import fr.olympa.zta.weapons.guns.GunRegistry;
import fr.olympa.zta.weapons.guns.bullets.Bullet;

public class WeaponsListener implements Listener {

	public static final NamespacedKey KNIFE_KEY = new NamespacedKey(OlympaZTA.getInstance(), "knife");
	public static final NamespacedKey GRENADE_KEY = new NamespacedKey(OlympaZTA.getInstance(), "grenade");

	public static boolean cancelDamageEvent = false; // dommage caus?? par le contact d'une balle

	private static final List<Material> NOT_WEAPON = Arrays.asList(Material.DIAMOND_AXE, Material.DIAMOND_HOE, Material.DIAMOND_SHOVEL, Material.DIAMOND_PICKAXE);

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
		if (weapon != null) {
			weapon.onEntityHit(e);
			if (damager.getFallDistance() > 0 && !damager.isOnGround())
				e.setDamage(e.getDamage() * 1.5F);
			else e.setDamage(e.getDamage() + ThreadLocalRandom.current().nextDouble() - 0.5);
		}else if (NOT_WEAPON.contains(item.getType())) {
			e.setCancelled(true);
			Prefix.DEFAULT_BAD.sendMessage(damager, "Vous ne pouvez pas utiliser un outil comme arme.");
		}
	}

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent e) {
		if (!e.getEntity().hasMetadata("bullet")) return;
		Bullet bullet;
		try {
			bullet = (Bullet) e.getEntity().getMetadata("bullet").get(0).value();
		}catch (ClassCastException | NullPointerException ex) {
			bullet = null;
			OlympaZTA.getInstance().sendMessage("??cImpossible de trouver une instance Bullet.");
		} // ??a arrive quand des balles ??taient pr??sentes dans des chunks qui ont ??t?? unload?? pendant le runtime
		if (bullet != null) bullet.hit(e);
	}

	@EventHandler (priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.useItemInHand() == Result.DENY) return;
		if (e.getHand() == EquipmentSlot.OFF_HAND || e.getAction() == Action.PHYSICAL) return;

		Weapon weapon = getWeapon(e.getItem());
		if (weapon != null) weapon.onInteract(e);
	}
	
	public boolean checkItemHotBar(Player p, ItemStack gunItem, Weapon gun, int hotBarSlot, int slotOnHand) {
//		System.out.println("DEBUG hotBarSlot " + hotBarSlot + " slotOnHand " + slotOnHand);
		ItemStack itemStackOnHotBar = p.getInventory().getItem(hotBarSlot);
		if (itemStackOnHotBar != null && itemStackOnHotBar.getMaxStackSize() > itemStackOnHotBar.getAmount() && itemStackOnHotBar.isSimilar(gunItem)) {
			return false;
		}
//		System.out.println("DEBUG hotBarSlot " + hotBarSlot + " slotOnHand " + slotOnHand + " - 2");
		if (itemStackOnHotBar == null) {
			if (hotBarSlot == slotOnHand)
				gun.itemHeld(p, gunItem, null);
			return true;
		}
		return false;
	}

	public void checkIfHeld(Player p, ClickType click, ItemStack gunItem, Weapon gun, int clickedSlot, boolean isPlayerInv) {
		int slotOnHand = p.getInventory().getHeldItemSlot();
		ItemStack itemStackOnHand = p.getInventory().getItemInMainHand();
		if ((click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT) && itemStackOnHand.getType() == Material.AIR) {
			if (!isPlayerInv) {
				for (int i = 8; 0 <= i && slotOnHand <= i; i--) {
//					System.out.println("DEBUG !isPlayerInv SLOT " + clickedSlot + " slotOnHand " + slotOnHand+ " i " + i);
					if (checkItemHotBar(p, gunItem, gun, i, slotOnHand)) {
						break;
					}
				}
			} else if (clickedSlot > 8) { // Bug when shift-clic from inv (not hotbar) to gui
				for (int i = 0; 8 >= i && slotOnHand >= i; i++) {
//					System.out.println("DEBUG CLICKED > 8 SLOT " + clickedSlot + " slotOnHand " + slotOnHand + " i " + i);
					if (checkItemHotBar(p, gunItem, gun, i, slotOnHand)) {
						break;
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		@Nullable
		ItemStack item = e.getCurrentItem();
		@Nullable
		Weapon previous = getWeapon(item);
		boolean isClickOnGuiInventory = e.getClickedInventory() != e.getWhoClicked().getInventory();
		if (item != null && previous != null && !e.isCancelled())
			checkIfHeld(p, e.getClick(), item, previous, e.getSlot(), !isClickOnGuiInventory);
		if (isClickOnGuiInventory) return;
		ItemStack cursor = e.getCursor();
		if (e.getClick() == ClickType.RIGHT && item != null) {
			if (cursor == null || cursor.getType() == Material.AIR) {
				OlympaZTA.getInstance().gunRegistry.ifGun(item, gun -> {
					gun.itemClick((Player) e.getWhoClicked(), item);
					e.setCancelled(true);
				});
			}
		}
		if (!e.isCancelled()) {
			if (previous != null) previous.itemNoLongerHeld(p, item);
			Weapon next = getWeapon(cursor);
			if (next != null && p.getInventory().getHeldItemSlot() == e.getSlot()) next.itemHeld(p, cursor, previous); // add check for inv slot
		}
	}

	@EventHandler
	public void onSwap(PlayerSwapHandItemsEvent e) {
		Player p = e.getPlayer();

		Weapon previous = getWeapon(e.getOffHandItem());
		if (previous != null) previous.itemNoLongerHeld(p, e.getOffHandItem());
		Weapon next = getWeapon(e.getMainHandItem());
		if (next != null) next.itemHeld(p, e.getMainHandItem(), previous);
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
		if (e.getEntity() instanceof Player p && p.getInventory().getItemInMainHand().getType() == Material.AIR) {
			ItemStack item = e.getItem().getItemStack();
			int prevAmount = p.getInventory().getItemInMainHand().getAmount();
			Bukkit.getScheduler().runTask(OlympaZTA.getInstance(), () -> {
				ItemStack mainHand = p.getInventory().getItemInMainHand();
				if (mainHand.getAmount() != prevAmount && mainHand.isSimilar(item))
					checkHeld(p, item, true);
			});
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

	@EventHandler (priority = EventPriority.HIGH)
	public void onPlayerLoad(OlympaPlayerLoadEvent e) {
		Bukkit.getScheduler().runTask(OlympaZTA.getInstance(), () -> checkHeld(e.getPlayer(), e.getPlayer().getInventory().getItemInMainHand(), true));
	}

	public static Weapon getWeapon(ItemStack item) {
		if (item == null) return null;
		if (!item.hasItemMeta()) return null;
		ItemMeta meta = item.getItemMeta();
		if (meta.getPersistentDataContainer().has(GunRegistry.GUN_KEY, PersistentDataType.INTEGER))
			return OlympaZTA.getInstance().gunRegistry.getGun(meta.getPersistentDataContainer().get(GunRegistry.GUN_KEY, PersistentDataType.INTEGER));
		else if (meta.getPersistentDataContainer().has(GRENADE_KEY, PersistentDataType.INTEGER))
			return Grenade.values()[meta.getPersistentDataContainer().get(GRENADE_KEY, PersistentDataType.INTEGER)];
		else if (meta.getPersistentDataContainer().has(KNIFE_KEY, PersistentDataType.INTEGER))
			return Knife.values()[meta.getPersistentDataContainer().get(KNIFE_KEY, PersistentDataType.INTEGER)];
		else if (meta.getPersistentDataContainer().has(Bandage.BANDAGE.getKey(), PersistentDataType.BYTE))
			return Bandage.BANDAGE;
		else if (meta.getPersistentDataContainer().has(Brouilleur.BROUILLEUR.getKey(), PersistentDataType.BYTE))
			return Brouilleur.BROUILLEUR;
		return null;
	}

	private Weapon checkHeld(Player p, ItemStack item, boolean held) {
		Weapon weapon = getWeapon(item);
		if (weapon != null)
			if (held)
				weapon.itemHeld(p, item, null);
			else weapon.itemNoLongerHeld(p, item);
		return weapon;
	}

}
