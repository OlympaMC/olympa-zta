package fr.olympa.zta.weapons;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Représente un objet pour lequel on peut créer un item.<br>
 * Implémente {@link Registrable}
 */
public interface ItemStackable extends Registrable{
	
	public abstract ItemStack createItemStack();
	
	public abstract String getName();
	
	public abstract Material getItemMaterial();
	
}
