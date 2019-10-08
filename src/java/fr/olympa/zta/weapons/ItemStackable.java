package fr.olympa.zta.weapons;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public interface ItemStackable{
	
	public abstract ItemStack createItemStack();
	
	public abstract String getName();
	
	public abstract Material getItemMaterial();
	
}
