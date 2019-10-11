package fr.olympa.zta.weapons.guns.accessories;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.zta.weapons.ItemStackable;
import fr.olympa.zta.weapons.guns.Gun;

public abstract class Accessory implements ItemStackable{
	
	public final int id = new Random().nextInt();
	public int getID(){
		return id;
	}
	
	public ItemStack createItemStack(){
		List<String> lore = new ArrayList<>();
		lore.add(getEffectsDescription().length < 2 ? "Effet :" : "Effets :");
		for (int i = 0; i < getEffectsDescription().length; i++) {
			lore.add("- " + getEffectsDescription()[i]);
		}
		lore.add("");
		lore.add("Accessoire immatriculé:");
		lore.add("[I" + getID() + "]");
		return ItemUtils.item(getItemMaterial(), "§a" + getName(), lore.toArray(new String[0]));
	}
	
	public abstract String[] getEffectsDescription();
	
	public abstract void apply(Gun gun);
	
	public abstract void remove(Gun gun);
	
	public enum AccessoryType{
		
		SCOPE("Lunette", 22), CANNON("Canon", 29), STOCK("Crosse", 42);
		
		private String name;
		private int slot;
		
		private AccessoryType(String name, int slot){
			this.name = name;
			this.slot = slot;
		}
		
		public String getName(){
			return name;
		}
		
		public int getSlot(){
			return slot;
		}
		
		public boolean isEnabled(Gun gun){
			switch (this){
			case SCOPE:
				return gun.isScopeAllowed();
			case CANNON:
				return gun.isCannonAllowed();
			case STOCK:
				return gun.isStockAllowed();
			}
			return false;
		}
		
		public Accessory get(Gun gun){
			return gun.accessories[this.ordinal()];
		}
		
		public static AccessoryType getFromSlot(int slot){
			for (AccessoryType type : values()) {
				if (type.slot == slot) return type;
			}
			return null;
		}
	}
	
}
