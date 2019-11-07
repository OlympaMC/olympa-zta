package fr.olympa.zta.weapons;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.ItemUtils;

public enum ArmorType {
	CIVIL("LEATHER", "Bandana", "Veste", "Jeans", "Baskets"),
	GANGSTER("GOLDEN", "Cagoule en Kevlar", "Veste en Kevlar", "Pantalon en Kevlar", "Chaussures en Kevlar"),
	ANTIRIOT("CHAINMAIL", "Casque anti-émeutes", "Plastron anti-émeutes", "Jambières anti-émeutes", "Bottes anti-émeutes"),
	MILITARY("IRON", "Casque en Kevlar renforcé", "Plastron en Kevlar renforcé", "Jambières en Kevlar renforcé", "Bottes en Kevlar renforcé");

	private String type;
	private String helmet;
	private String chestplate;
	private String leggings;
	private String boots;

	private ArmorType(String type, String helmet, String chestplate, String leggings, String boots) {
		this.type = type;
		this.helmet = helmet;
		this.chestplate = chestplate;
		this.leggings = leggings;
		this.boots = boots;
	}

	public String getSlotName(ArmorSlot slot) {
		switch (slot) {
		case BOOTS:
			return boots;
		case CHESTPLATE:
			return chestplate;
		case HELMET:
			return helmet;
		case LEGGINGS:
			return leggings;
		}
		return "error";
	}

	public ItemStack get(ArmorSlot slot) {
		return ItemUtils.item(Material.valueOf(type + "_" + slot.name()), "§b" + getSlotName(slot));
	}

	public enum ArmorSlot {
		HELMET, CHESTPLATE, LEGGINGS, BOOTS;
	}

}
