package fr.olympa.zta.weapons.guns;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import fr.olympa.zta.OlympaZTA;

public class PersistentGun extends Gun {
	
	public static final NamespacedKey GUN_KEY_PERSISTENT = new NamespacedKey(OlympaZTA.getInstance(), "gunPersistent");
	public static final NamespacedKey GUN_KEY_PERSISTENT_TYPE = new NamespacedKey(OlympaZTA.getInstance(), "gunPersistentType");
	private static Map<Integer, PersistentGun> guns = new HashMap<>();
	
	private PersistentGun(int id, GunType type) {
		super(id, type);
		ready = true;
		ammos = (int) maxAmmos.getValue();
		knockback.setBaseValue(0);
		fireVolume.setBaseValue(1f);
	}
	
	@Override
	public NamespacedKey getKey() {
		return GUN_KEY_PERSISTENT;
	}
	
	@Override
	public ItemStack createItemStack(boolean accessories) {
		ItemStack item = super.createItemStack(accessories);
		ItemMeta meta = item.getItemMeta();
		meta.getPersistentDataContainer().set(GUN_KEY_PERSISTENT_TYPE, PersistentDataType.STRING, type.name());
		item.setItemMeta(meta);
		return item;
	}
	
	@Override
	public List<String> getLore(boolean accessories) {
		List<String> lore = super.getLore(accessories);
		lore.add("");
		lore.add("§7> §6Arme persistante");
		lore.add("§7  §6 conçue pour les NPCs");
		return lore;
	}
	
	@Override
	protected boolean shouldTakeItems(Player p) {
		return false;
	}
	
	public static PersistentGun create(GunType type) {
		int id;
		do {
			id = ThreadLocalRandom.current().nextInt();
		}while (guns.containsKey(id));
		PersistentGun gun = new PersistentGun(id, type);
		guns.put(id, gun);
		return gun;
	}
	
	public static PersistentGun getGun(ItemStack item) {
		if (item == null) return null;
		if (!item.hasItemMeta()) return null;
		ItemMeta meta = item.getItemMeta();
		if (meta.getPersistentDataContainer().has(GUN_KEY_PERSISTENT, PersistentDataType.INTEGER)) {
			int oldID = meta.getPersistentDataContainer().get(GUN_KEY_PERSISTENT, PersistentDataType.INTEGER);
			GunType type = GunType.valueOf(meta.getPersistentDataContainer().get(GUN_KEY_PERSISTENT_TYPE, PersistentDataType.STRING));
			PersistentGun gun = guns.get(oldID);
			if (gun == null) {
				gun = new PersistentGun(oldID, type);
			}else if (gun.type != type) {
				gun = create(type);
				meta.getPersistentDataContainer().set(GUN_KEY_PERSISTENT, PersistentDataType.INTEGER, gun.id);
				item.setItemMeta(meta);
			}
			return gun;
		}
		return null;
	}
	
}
