package fr.olympa.zta.registry;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.zta.weapons.Weapon;

public class ItemStackableInstantiator<T extends ItemStackable> {

	public final Class<T> clazz;
	private ItemStack demoItem;

	public ItemStackableInstantiator(Class<T> clazz) {
		this.clazz = clazz;
		try {
			demoItem = ItemUtils.item((Material) clazz.getDeclaredField("TYPE").get(null), "§e" + clazz.getDeclaredField("NAME").get(null));
			if (Weapon.class.isAssignableFrom(clazz)) {
				ItemMeta meta = demoItem.getItemMeta();
				meta.setCustomModelData(1);
				demoItem.setItemMeta(meta);
			}
		}catch (ReflectiveOperationException ex) {
			ex.printStackTrace();
		}
	}

	public T create() throws ReflectiveOperationException {
		return clazz.getDeclaredConstructor(int.class).newInstance(ZTARegistry.generateID());
	}

	public ItemStack getDemoItem() {
		return demoItem;
	}

}