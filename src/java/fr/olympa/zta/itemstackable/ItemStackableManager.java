package fr.olympa.zta.itemstackable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class ItemStackableManager {
	
	public static List<ItemStackable> stackables = new ArrayList<>();
	public static BiMap<NamespacedKey, ItemStackable> stackablesNBT = HashBiMap.create();
	
	public static NamespacedKey register(ItemStackable stackable) {
		NamespacedKey key = new NamespacedKey("olympa-zta", stackable.getUniqueId().toLowerCase(Locale.ROOT));
		if (stackablesNBT.putIfAbsent(key, stackable) != null) return key;
		stackables.add(stackable);
		return key;
	}
	
	public static ItemStackable getStackable(String id) {
		return stackables.stream().filter(x -> x.getUniqueId().equals(id)).findAny().orElse(null);
	}
	
	public static ItemStack processItem(ItemStack item, ItemStackable stackable) {
		ItemMeta meta = item.getItemMeta();
		NamespacedKey key = stackablesNBT.inverse().get(stackable);
		if (key == null) key = register(stackable);
		meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 0);
		item.setItemMeta(meta);
		return item;
	}
	
	public static ItemStackable getStackable(ItemStack item) {
		if (item == null) return null;
		if (!item.hasItemMeta()) return null;
		PersistentDataContainer dataContainer = item.getItemMeta().getPersistentDataContainer();
		if (dataContainer.isEmpty()) return null;
		for (NamespacedKey key : dataContainer.getKeys()) {
			ItemStackable stackable = stackablesNBT.get(key);
			if (stackable != null) return stackable;
		}
		return null;
	}

}
