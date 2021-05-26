package fr.olympa.zta.loot.crates;

import fr.olympa.api.utils.RandomizedPicker.PickerBuilder;
import fr.olympa.api.utils.RandomizedPicker.RandomizedMultiPicker;
import fr.olympa.zta.itemstackable.QuestItem;
import fr.olympa.zta.loot.creators.AmmoCreator;
import fr.olympa.zta.loot.creators.ArmorCreator;
import fr.olympa.zta.loot.creators.FoodCreator;
import fr.olympa.zta.loot.creators.FoodCreator.Food;
import fr.olympa.zta.loot.creators.ItemStackableCreator;
import fr.olympa.zta.loot.creators.LootCreator;
import fr.olympa.zta.loot.creators.MoneyCreator;
import fr.olympa.zta.loot.creators.QuestItemCreator;
import fr.olympa.zta.utils.PhysicalMoney;
import fr.olympa.zta.weapons.ArmorType;
import fr.olympa.zta.weapons.Knife;
import fr.olympa.zta.weapons.guns.Accessory;
import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.GunType;

public enum CrateType {

	BASIC(
			"basique",
			new PickerBuilder<LootCreator>()
			.addAlways(new MoneyCreator(PhysicalMoney.BANKNOTE_10, 3, 9))
			.add(20, new MoneyCreator(PhysicalMoney.BANKNOTE_100, 1, 2))
			.add(15, new AmmoCreator(5, 15))
			.add(15, new AmmoCreator(AmmoType.HEAVY, 4, 8, true))
			.add(15, new AmmoCreator(AmmoType.LIGHT, 4, 8, true))
			.add(15, new AmmoCreator(AmmoType.HANDWORKED, 4, 8, true))
			.add(15, new AmmoCreator(AmmoType.CARTRIDGE, 3, 6, true))
			.add(20, new FoodCreator(Food.GOLDEN_CARROT, 3, 7))
			.add(20, new FoodCreator(Food.COOKED_BEEF, 5, 15))
			.add(1, new ItemStackableCreator(GunType.STONER))
			.add(7, new ItemStackableCreator(GunType.DRAGUNOV))
			.add(8, new ItemStackableCreator(GunType.SDMR))
			.add(13, new ItemStackableCreator(GunType.M16))
			.add(13, new ItemStackableCreator(GunType.BENELLI))
			.add(16, new ItemStackableCreator(Knife.SURIN))
			.add(15, new ItemStackableCreator(Accessory.SCOPE_STRONG))
			.add(15, new ItemStackableCreator(Accessory.STOCK_STRONG))
			.add(15, new ItemStackableCreator(Accessory.CANNON_DAMAGE))
			.add(20, new ArmorCreator(ArmorType.MILITARY))
			.build(6, 8)
			),
	RARE(
			"rare",
			new PickerBuilder<LootCreator>()
			.addAlways(new MoneyCreator(PhysicalMoney.BANKNOTE_10, 4, 10))
			.add(25, new MoneyCreator(PhysicalMoney.BANKNOTE_100, 2, 3))
			.add(15, new AmmoCreator(5, 15))
			.add(15, new AmmoCreator(AmmoType.HEAVY, 5, 10, true))
			.add(15, new AmmoCreator(AmmoType.LIGHT, 5, 10, true))
			.add(15, new AmmoCreator(AmmoType.HANDWORKED, 5, 10, true))
			.add(15, new AmmoCreator(AmmoType.CARTRIDGE, 4, 8, true))
			.add(15, new FoodCreator(Food.GOLDEN_CARROT, 5, 10))
			.add(15, new FoodCreator(Food.COOKED_BEEF, 10, 15))
			.add(15, new FoodCreator(Food.GOLDEN_APPLE, 2, 5))
			.add(5, new ItemStackableCreator(GunType.STONER))
			.add(9, new ItemStackableCreator(GunType.DRAGUNOV))
			.add(7, new ItemStackableCreator(GunType.SDMR))
			.add(13, new ItemStackableCreator(GunType.M16))
			.add(15, new ItemStackableCreator(Knife.SURIN))
			.add(18, new ItemStackableCreator(Accessory.SCOPE_STRONG))
			.add(18, new ItemStackableCreator(Accessory.STOCK_STRONG))
			.add(15, new ItemStackableCreator(Accessory.CANNON_SILENT))
			.add(0.5, new QuestItemCreator(QuestItem.PARACHUTE))
			.add(0.5, new QuestItemCreator(QuestItem.BOOTS))
			.add(20, new ArmorCreator(ArmorType.MILITARY))
			.build(6, 8)
			),
	;

	private String name;
	private RandomizedMultiPicker<LootCreator> picker;

	private CrateType(String name, RandomizedMultiPicker<LootCreator> picker) {
		this.name = name;
		this.picker = picker;
	}
	
	public String getName() {
		return name;
	}
	
	public RandomizedMultiPicker<LootCreator> getPicker() {
		return picker;
	}
}
