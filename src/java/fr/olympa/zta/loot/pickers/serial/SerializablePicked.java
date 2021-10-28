package fr.olympa.zta.loot.pickers.serial;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface SerializablePicked/* extends Observable*/ {
	
	public ItemStack getItemStack();
	
	public void edit(Player p, Runnable callback);
	
	public SerializableType getSerialType();
	
	public String save();
	
	public default String serialize() {
		return getSerialType().getID() + "::" + save();
	}
	
}
