package fr.olympa.zta.weapons.skins;

import org.bukkit.inventory.ItemStack;

public interface Skinable {
	
	public ItemStack getSkinItem(Skin skin);
	
	public Skin getSkinOfItem(ItemStack item);
	
	public void setSkin(Skin skin, ItemStack item);
	
}
