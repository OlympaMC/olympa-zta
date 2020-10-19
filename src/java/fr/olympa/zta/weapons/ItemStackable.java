package fr.olympa.zta.weapons;

import org.bukkit.inventory.ItemStack;

public interface ItemStackable {
	
	public ItemStack createItem();
	
	public default ItemStack getDemoItem() {
		return createItem();
	}
	
}
