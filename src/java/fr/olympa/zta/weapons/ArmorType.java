package fr.olympa.zta.weapons;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public enum ArmorType {
	CIVIL("LEATHER", "Bandana", "Veste", "Jeans", "Baskets", true),
	GANGSTER("GOLDEN", "Cagoule en Kevlar", "Veste en Kevlar", "Pantalon en Kevlar", "Chaussures en Kevlar", true, Enchantment.PROTECTION_PROJECTILE, 0),
	ANTIRIOT("CHAINMAIL", "Casque anti-émeutes", "Plastron anti-émeutes", "Jambières anti-émeutes", "Bottes anti-émeutes", true, Enchantment.PROTECTION_ENVIRONMENTAL, 0),
	MILITARY("IRON", "Casque en Kevlar renforcé", "Plastron en Kevlar renforcé", "Jambières en Kevlar renforcé", "Bottes en Kevlar renforcé", false, Enchantment.PROTECTION_PROJECTILE, 0);

	private String type;
	private boolean unbreakable;
	private Enchantment enchantment;
	private int level;
	private ItemStack helmet;
	private ItemStack chestplate;
	private ItemStack leggings;
	private ItemStack boots;

	private ArmorType(String type, String helmet, String chestplate, String leggings, String boots, boolean unbreakable) {
		this(type, helmet, chestplate, leggings, boots, unbreakable, null, 0);
	}

	private ArmorType(String type, String helmet, String chestplate, String leggings, String boots, boolean unbreakable, Enchantment enchantment, int level) {
		this.type = type;
		this.unbreakable = unbreakable;
		this.enchantment = enchantment;
		this.level = level;
		this.helmet = createItem(ArmorSlot.HELMET, helmet);
		this.chestplate = createItem(ArmorSlot.CHESTPLATE, chestplate);
		this.leggings = createItem(ArmorSlot.LEGGINGS, leggings);
		this.boots = createItem(ArmorSlot.BOOTS, boots);
	}

	private ItemStack createItem(ArmorSlot slot, String name) {
		ItemStack item = new ItemStack(Material.valueOf(type + "_" + slot.name()));
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§b" + name);
		meta.setUnbreakable(unbreakable);
		if (enchantment != null) meta.addEnchant(enchantment, level, true);
		item.setItemMeta(meta);
		return item;
	}

	public ItemStack get(ArmorSlot slot) {
		switch (slot) {
		case BOOTS:
			return boots.clone();
		case CHESTPLATE:
			return chestplate.clone();
		case HELMET:
			return helmet.clone();
		case LEGGINGS:
			return leggings.clone();
		default:
			return null;
		}
	}

	public enum ArmorSlot {
		HELMET, CHESTPLATE, LEGGINGS, BOOTS;
	}

}