package fr.olympa.zta.loot.packs;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.common.randomized.RandomizedPickerBase.ConditionalMultiPicker;
import fr.olympa.api.spigot.economy.OlympaMoney;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.itemstackable.Artifacts;
import fr.olympa.zta.loot.RandomizedInventory;
import fr.olympa.zta.loot.RandomizedInventory.LootContext;
import fr.olympa.zta.loot.creators.AmmoCreator;
import fr.olympa.zta.loot.creators.ArmorCreator;
import fr.olympa.zta.loot.creators.FoodCreator;
import fr.olympa.zta.loot.creators.FoodCreator.Food;
import fr.olympa.zta.loot.creators.ItemStackableCreator;
import fr.olympa.zta.loot.creators.LootCreator;
import fr.olympa.zta.weapons.ArmorType;
import fr.olympa.zta.weapons.guns.Accessory;
import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.GunType;

public enum PackType {

	BASIC(
			11,
			2000,
			0,
			"basique du militaire",
			RandomizedInventory.newBuilder()
				.addAlways(new AmmoCreator(AmmoType.HEAVY, 20, 30, true))
				.addAlways(new FoodCreator(Food.COOKED_BEEF, 10, 15))
				.add(7, new ItemStackableCreator(GunType.P22))
				.add(8, new ItemStackableCreator(GunType.KSG))
				.addAlways(new ArmorCreator(ArmorType.MILITARY))
				.build(4, 4)),
	AMMOS(
			13,
			1000,
			0,
			"de munitions",
			RandomizedInventory.newBuilder()
			.addAlways(new AmmoCreator(32, 64))
			.addAlways(new AmmoCreator(AmmoType.HEAVY, 64, 64, true))
			.addAlways(new AmmoCreator(AmmoType.LIGHT, 64, 64, true))
			.addAlways(new AmmoCreator(AmmoType.HANDWORKED, 64, 64, true))
			.addAlways(new AmmoCreator(AmmoType.CARTRIDGE, 48, 48, true))
			.add(25, new AmmoCreator(AmmoType.HEAVY, 32, 32, false))
			.add(25, new AmmoCreator(AmmoType.LIGHT, 32, 32, false))
			.add(25, new AmmoCreator(AmmoType.HANDWORKED, 32, 32, false))
			.add(25, new AmmoCreator(AmmoType.CARTRIDGE, 24, 24, false))
			.build(5, 7)),
	ACCESSORIES(
			15,
			5000,
			0,
			"d'accessoires",
			RandomizedInventory.newBuilder()
			.addAlways(new AmmoCreator(10, 15))
			.add(8, new ItemStackableCreator(Accessory.CANNON_CAC))
			.add(8, new ItemStackableCreator(Accessory.CANNON_DAMAGE))
			.add(10, new ItemStackableCreator(Accessory.CANNON_POWER))
			.add(8, new ItemStackableCreator(Accessory.CANNON_SILENT))
			.add(9, new ItemStackableCreator(Accessory.CANNON_STABILIZER))
			.add(10, new ItemStackableCreator(Accessory.SCOPE_LIGHT))
			.add(9, new ItemStackableCreator(Accessory.SCOPE_STRONG))
			.add(10, new ItemStackableCreator(Accessory.STOCK_LIGHT))
			.add(8, new ItemStackableCreator(Accessory.STOCK_STRONG))
			.build(2, 2)),
	RARE(
			30,
			20000,
			0,
			"arme rare",
			RandomizedInventory.newBuilder()
			.add(20, new FoodCreator(Food.GOLDEN_APPLE, 10, 10))
			.add(15, new ItemStackableCreator(GunType.DRAGUNOV))
			.add(20, new ItemStackableCreator(GunType.SDMR))
					.add(10, new ItemStackableCreator(Artifacts.PARACHUTE))
			.build(1, 1)),
	EPIQUE(
			32,
			0,
			6.49,
			"épique",
			RandomizedInventory.newBuilder()
			.add(10, new FoodCreator(Food.GOLDEN_APPLE, 10, 10))
			.add(15, new ItemStackableCreator(GunType.STONER))
			.add(15, new ItemStackableCreator(GunType.BARRETT))
			.add(10, new ItemStackableCreator(GunType.SDMR))
			.add(10, new ItemStackableCreator(GunType.DRAGUNOV))
			.add(8, new ItemStackableCreator(Artifacts.PARACHUTE))
			.add(8, new ItemStackableCreator(Artifacts.BOOTS))
			.build(4, 4)),
	;

	private int slot;
	private int price;
	private double priceReal;
	private String name;
	private ConditionalMultiPicker<LootCreator, LootContext> picker;

	private ItemStack item;
	
	private PackType(int slot, int price, double priceReal, String name, ConditionalMultiPicker<LootCreator, LootContext> picker) {
		this.price = price;
		this.slot = slot;
		this.priceReal = priceReal;
		this.name = name;
		this.picker = OlympaZTA.getInstance().pickers.registerPicker(picker, "packs/" + name(), LootCreator.class, LootContext.class);
		List<String> lootsDescription = new ArrayList<>();
		lootsDescription.add("");
		lootsDescription.add("§7§nContient :");
		for (LootCreator creator : picker.getAlwaysObjectList()) lootsDescription.add("§a● " + creator.getTitle());
		for (LootCreator creator : picker.getObjectList().keySet()) lootsDescription.add("§8● §7" + creator.getTitle());
		lootsDescription.add("");
		if (priceReal != 0) {
			lootsDescription.add(" §7§oSur la boutique...");
			lootsDescription.add(SpigotUtils.getBarsWithLoreLength(name, lootsDescription, priceReal + "€"));
		}else {
			lootsDescription.add(SpigotUtils.getBarsWithLoreLength(name, lootsDescription, OlympaMoney.format(price)));
		}
		
		item = ItemUtils.item(Material.CHEST, "§ePack " + name, lootsDescription.toArray(String[]::new));
	}
	
	public int getSlot() {
		return slot;
	}
	
	public int getPrice() {
		return price;
	}
	
	public double getPriceReal() {
		return priceReal;
	}
	
	public String getName() {
		return name;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public ConditionalMultiPicker<LootCreator, LootContext> getPicker() {
		return picker;
	}
	
}
