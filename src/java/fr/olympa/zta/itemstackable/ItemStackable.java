package fr.olympa.zta.itemstackable;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public interface ItemStackable {
	
	public String getName();
	
	public String getId();
	
	public NamespacedKey getKey();
	
	public default String getUniqueId() {
		return getClass().getSimpleName() + "-" + getId();
	}
	
	public ItemStack createItem();
	
	public default ItemStack getDemoItem() {
		return createItem();
	}
	
}
