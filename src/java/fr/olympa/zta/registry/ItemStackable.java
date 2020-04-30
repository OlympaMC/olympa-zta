package fr.olympa.zta.registry;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import fr.olympa.zta.OlympaZTA;

/**
 * Représente un objet pour lequel on peut créer un item.<br>
 * Implémente {@link Registrable}
 */
public interface ItemStackable extends Registrable{
	
	public static final NamespacedKey PERISTENT_DATA_KEY = new NamespacedKey(OlympaZTA.getInstance(), "itemRegistry");

	public abstract ItemStack createItemStack();

	public abstract String getName();
	
	public abstract Material getItemMaterial();
	
	/**
	 * @param key Nom du paramètre
	 * @param value Valeur du paramètre
	 * @return <tt>§6§l{KEY} §r§6: §e§o{VALUE}</tt>
	 */
	public default String getFeatureLoreLine(String key, Object value){
		return "§6§l" + key + " §r§6: §e§o" + value;
	}
	
	public default List<String> getIDLoreLines(){
		return Arrays.asList("",
				"§6§lArme immatriculée §r§6:",
				"§e§m   §r§e[I" + getID() + "]§m   §r");
	}
	
	public default ItemStack addIdentifier(ItemStack item) {
		ItemMeta itemMeta = item.getItemMeta();
		itemMeta.getPersistentDataContainer().set(PERISTENT_DATA_KEY, PersistentDataType.INTEGER, getID());
		item.setItemMeta(itemMeta);
		return item;
	}

}
