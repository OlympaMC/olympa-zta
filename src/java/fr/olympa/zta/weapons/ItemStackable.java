package fr.olympa.zta.weapons;

import org.bukkit.inventory.ItemStack;

public interface ItemStackable {
	
	public String getName();
	
	public String getId();
	
	public ItemStack createItem();
	
	public default ItemStack getDemoItem() {
		return createItem();
	}
	
}
