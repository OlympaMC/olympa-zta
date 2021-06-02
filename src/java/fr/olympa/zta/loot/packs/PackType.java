package fr.olympa.zta.loot.packs;

import java.util.Arrays;

import fr.olympa.api.spigot.economy.OlympaMoney;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.api.utils.RandomizedPickerBase;
import fr.olympa.api.utils.RandomizedPickerBase.RandomizedMultiPicker;
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
			2000,
			11,
			"basique du militaire",
			RandomizedPickerBase.<LootCreator>newBuilder()
				.addAlways(new AmmoCreator(AmmoType.HEAVY, 20, 30, true))
				.addAlways(new FoodCreator(Food.COOKED_BEEF, 10, 15))
				.add(7, new ItemStackableCreator(GunType.P22))
				.add(8, new ItemStackableCreator(GunType.KSG))
				.addAlways(new ArmorCreator(ArmorType.MILITARY))
				.build(4, 4)),
	AMMOS(
			1000,
			13,
			"de munitions",
			RandomizedPickerBase.<LootCreator>newBuilder()
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
	RARE(
			10000,
			30,
			"arme rare",
			RandomizedPickerBase.<LootCreator>newBuilder()
			.add(20, new FoodCreator(Food.GOLDEN_APPLE, 10, 10))
			.add(10, new ItemStackableCreator(GunType.STONER))
			.add(15, new ItemStackableCreator(GunType.DRAGUNOV))
			.add(20, new ItemStackableCreator(GunType.SDMR))
			.build(1, 1)),
	ACCESSORIES(
			5000,
			15,
			"d'accessoires",
			RandomizedPickerBase.<LootCreator>newBuilder()
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
	;

	private int price, slot;
	private String name;
	private String[] lootsDescription;
	private RandomizedMultiPicker<LootCreator> picker;

	private PackType(int price, int slot, String name, RandomizedMultiPicker<LootCreator> picker) {
		this.price = price;
		this.slot = slot;
		this.name = name;
		this.picker = picker;
		lootsDescription = new String[picker.getObjectList().size() + picker.getAlwaysObjectList().size() + 4];
		int i = 0;
		lootsDescription[i++] = "";
		lootsDescription[i++] = "§7§nContient :";
		for (LootCreator creator : picker.getAlwaysObjectList()) lootsDescription[i++] = "§a● " + creator.getTitle();
		for (LootCreator creator : picker.getObjectList().keySet()) lootsDescription[i++] = "§8● §7" + creator.getTitle();
		lootsDescription[i++] = "";
		lootsDescription[i++] = SpigotUtils.getBarsWithLoreLength(name, Arrays.asList(lootsDescription), OlympaMoney.format(price));
	}
	
	public int getPrice() {
		return price;
	}
	
	public int getSlot() {
		return slot;
	}

	public String getName() {
		return name;
	}
	
	public String[] getLootsDescription() {
		return lootsDescription;
	}
	
	public RandomizedMultiPicker<LootCreator> getPicker() {
		return picker;
	}
	
}
