package fr.olympa.zta.weapons;

import org.bukkit.inventory.ItemStack;

public interface ItemStackable {
	
	public String getName();
	
	public ItemStack createItem();
	
	public default ItemStack getDemoItem() {
		return createItem();
	}
	
}
