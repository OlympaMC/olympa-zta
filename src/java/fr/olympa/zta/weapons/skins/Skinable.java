package fr.olympa.zta.weapons.skins;

import org.bukkit.inventory.ItemStack;

public interface Skinable {
	
	public ItemStack getSkinItem(Skin skin);
	
	public void setSkin(Skin skin, ItemStack item);
	
}
