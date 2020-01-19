package fr.olympa.zta.weapons.guns.accessories;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.zta.registry.ItemStackable;
import fr.olympa.zta.registry.ZTARegistry;
import fr.olympa.zta.weapons.guns.Gun;

public abstract class Accessory implements ItemStackable{
	
	private final int id;

	public Accessory(int id) {
		this.id = id;
	}

	public Accessory() {
		this.id = ZTARegistry.generateID();
	}
	
	public int getID() {
		return id;
	}

	public ItemStack createItemStack(){
		List<String> lore = new ArrayList<>();
		lore.add("§6§l" + (getEffectsDescription().length < 2 ? "Effet" : "Effets") + " §r§6:");
		for (int i = 0; i < getEffectsDescription().length; i++) {
			lore.add("§e- " + getEffectsDescription()[i]);
		}
		lore.addAll(getIDLoreLines());
		return ItemUtils.item(getItemMaterial(), "§a" + getName(), lore.toArray(new String[0]));
	}
	
	public abstract String[] getEffectsDescription();
	
	public abstract AccessoryType getType();

	public abstract void apply(Gun gun);
	
	public abstract void remove(Gun gun);
	
	public enum AccessoryType{
		
		SCOPE("Lunette", 13), CANNON("Canon", 20), STOCK("Crosse", 33);
		
		private String name;
		private int slot;
		private ItemStack available;
		private ItemStack unavailable;
		
		private AccessoryType(String name, int slot){
			this.name = name;
			this.slot = slot;
			this.available = ItemUtils.item(Material.LIME_STAINED_GLASS_PANE, "§aEmplacement disponible : " + name);
			this.unavailable = ItemUtils.item(Material.RED_STAINED_GLASS_PANE, "§cEmplacement indisponible : " + name);
		}
		
		public String getName(){
			return name;
		}
		
		public int getSlot(){
			return slot;
		}
		
		public ItemStack getAvailableItemSlot(){
			return available;
		}
		
		public ItemStack getUnavailableItemSlot(){
			return unavailable;
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
		
		public Accessory get(Gun gun) {
			switch (this) {
			case SCOPE:
				return gun.scope;
			case CANNON:
				return gun.cannon;
			case STOCK:
				return gun.stock;
			}
			return null;
		}

		public static AccessoryType getFromSlot(int slot){
			for (AccessoryType type : values()) {
				if (type.slot == slot) return type;
			}
			return null;
		}
	}
	
}
