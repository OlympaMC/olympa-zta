package fr.olympa.zta.weapons.guns.accessories;

import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.OlympaItemBuild;
import fr.olympa.zta.weapons.ItemStackable;
import fr.olympa.zta.weapons.guns.Gun;

public abstract class Accessory implements ItemStackable{
	
	public ItemStack createItemStack(){
		String[] lore = new String[getEffectsDescription().length + 1];
		lore[0] = getEffectsDescription().length < 2 ? "Effet :" : "Effets :";
		for (int i = 0; i < getEffectsDescription().length; i++) {
			lore[i + 1] = "- " + getEffectsDescription()[i];
		}
		return new OlympaItemBuild(getItemMaterial(), "§a" + getName()).lore(lore).build();
	}
	
	public abstract String[] getEffectsDescription();
	
	public abstract void apply(Gun gun);
	
	public abstract void remove(Gun gun);
	
}
