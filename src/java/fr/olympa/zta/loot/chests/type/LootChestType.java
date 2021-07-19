package fr.olympa.zta.loot.chests.type;

import fr.olympa.api.common.randomized.RandomizedPickerBase.ConditionalMultiPicker;
import fr.olympa.zta.bank.PhysicalMoney;
import fr.olympa.zta.itemstackable.Artifacts;
import fr.olympa.zta.itemstackable.QuestItem;
import fr.olympa.zta.loot.RandomizedInventory;
import fr.olympa.zta.loot.RandomizedInventory.LootContext;
import fr.olympa.zta.loot.creators.AmmoCreator;
import fr.olympa.zta.loot.creators.ArmorCreator;
import fr.olympa.zta.loot.creators.FoodCreator;
import fr.olympa.zta.loot.creators.FoodCreator.Food;
import fr.olympa.zta.loot.creators.ItemStackableCreator;
import fr.olympa.zta.loot.creators.LootCreator;
import fr.olympa.zta.loot.creators.MoneyCreator;
import fr.olympa.zta.loot.creators.QuestItemCreator;
import fr.olympa.zta.weapons.ArmorType;
import fr.olympa.zta.weapons.Knife;
import fr.olympa.zta.weapons.guns.Accessory;
import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.GunType;

public enum LootChestType {

	BASIC(
			"basique",
			RandomizedInventory.newBuilder()
					.add(30, new AmmoCreator.BestCreator(3, 5, 7, 0.4, 1D, 0.5D))
					.add(6, new QuestItemCreator(QuestItem.AMAS, 1, 3))
					.add(10, new QuestItemCreator(QuestItem.DECHET, 1, 2))
					.add(0.16, new QuestItemCreator(QuestItem.BATTERIE, 1, 1))
					.add(14, new MoneyCreator(PhysicalMoney.BANKNOTE_1, 2, 6))
					.add(4, new MoneyCreator(PhysicalMoney.BANKNOTE_10, 1, 1))
					.add(16, new FoodCreator(Food.BAKED_POTATO, 1, 2))
					.add(0.006, new ItemStackableCreator(Artifacts.BOOTS))
					.add(0.006, new ItemStackableCreator(Artifacts.PARACHUTE))
					.build(1, 3)),
	SAFE(
			"de zone sécurisée",
			RandomizedInventory.newBuilder()
					.add(15, new ItemStackableCreator.GunConditionned(new ItemStackableCreator(GunType.M1911), 0.4, 0.15))
					.add(5, new ItemStackableCreator.GunConditionned(new ItemStackableCreator(GunType.P22), 0.15, 0.7))
					.add(25, new AmmoCreator.BestCreator(3, 6, 8, 0.5, 1, 0))
					.add(25, new AmmoCreator(AmmoType.LIGHT, 3, 7, 0.5))
					.add(20, new FoodCreator(Food.COOKIE, 2, 3))
					.add(10, new MoneyCreator(PhysicalMoney.BANKNOTE_1, 2, 6))
					.build(1, 3)),
	EASY(
			"de zone modérée",
			RandomizedInventory.newBuilder()
			.build(1, 3)),
	MEDIUM(
			"de zone à risques",
			RandomizedInventory.newBuilder()
			.build(1, 3)),
	HARD(
			"de zone rouge",
			RandomizedInventory.newBuilder()
			.build(1, 3)),
	CIVIL(
			"civil",
			RandomizedInventory.newBuilder()
			.add(3, new ItemStackableCreator(GunType.M1911))
			.add(1.44, new ItemStackableCreator(GunType.COBRA))
			.add(2.28, new ItemStackableCreator(GunType.REM_870))
			.add(1.68, new ItemStackableCreator(GunType.UZI))
			.add(1.44, new ItemStackableCreator(GunType.M16))
			.add(2.16, new ItemStackableCreator(GunType.M1897))
			.add(1.5, new ItemStackableCreator(Accessory.STOCK_LIGHT))
			.add(0.9, new ItemStackableCreator(Accessory.CANNON_SILENT))
			.add(0.6, new ItemStackableCreator(Accessory.SCOPE_LIGHT))
			.add(6.45, new AmmoCreator(AmmoType.LIGHT, 3, 6, true))
			.add(2.15, new AmmoCreator(AmmoType.CARTRIDGE, 1, 1, true))
			.add(8.6 , new AmmoCreator(4, 9))
			.add(6.45, new AmmoCreator(AmmoType.HANDWORKED, 2, 4, false))
			.add(6.45, new AmmoCreator(AmmoType.LIGHT, 4, 8, false))
			.add(6.45, new AmmoCreator(AmmoType.CARTRIDGE, 1, 1, false))
			.add(6.45, new AmmoCreator(AmmoType.HEAVY, 1, 3, false))
			.add(4.8, new FoodCreator(Food.BREAD, 1, 3))
			.add(4.8, new FoodCreator(Food.BAKED_POTATO, 1, 2))
			.add(4.8, new FoodCreator(Food.CARROT, 1, 3))
			.add(4.8, new FoodCreator(Food.COOKED_RABBIT, 1, 2))
			.add(4.8, new FoodCreator(Food.COOKED_COD, 1, 2))
			.add(10, new ArmorCreator(ArmorType.ANTIRIOT))
			.add(3.6, new QuestItemCreator(QuestItem.DECHET))
			.add(3.6, new QuestItemCreator(QuestItem.AMAS))
			.add(0.8, new MoneyCreator(PhysicalMoney.BANKNOTE_1, 2, 5))
			.add(0.0002, new ItemStackableCreator(Artifacts.PARACHUTE))
			.add(0.0002, new ItemStackableCreator(Artifacts.BOOTS))
			.build(1, 3)),
	MILITARY(
			"militaire",
			RandomizedInventory.newBuilder()
			.add(2.0, new ItemStackableCreator(GunType.P22))
			.add(1.0, new ItemStackableCreator(GunType.SDMR))
			.add(1.2, new ItemStackableCreator(GunType.BARRETT))
			.add(1.8, new ItemStackableCreator(GunType.KSG))
			.add(4.0, new ItemStackableCreator(Knife.SURIN))
			.add(2.4, new ItemStackableCreator(Accessory.STOCK_STRONG))
			.add(1.5, new ItemStackableCreator(Accessory.CANNON_POWER))
			.add(0.5, new ItemStackableCreator(Accessory.CANNON_CAC))
			.add(0.6, new ItemStackableCreator(Accessory.SCOPE_LIGHT))
			.add(7.55, new AmmoCreator(AmmoType.HEAVY, 3, 6, true))
			.add(2.85, new AmmoCreator(AmmoType.CARTRIDGE, 1, 1, true))
			.add(7.52, new AmmoCreator(4, 9))
			.add(7.52, new AmmoCreator(AmmoType.HANDWORKED, 1, 2, false))
			.add(7.52, new AmmoCreator(AmmoType.LIGHT, 1, 4, false))
			.add(7.52, new AmmoCreator(AmmoType.CARTRIDGE, 1, 2, false))
			.add(7.52, new AmmoCreator(AmmoType.HEAVY, 4, 8, false))
			.add(5.2, new FoodCreator(Food.COOKED_BEEF, 1, 2))
			.add(5.2, new FoodCreator(Food.MUSHROOM_STEW, 1, 1))
			.add(5.2, new FoodCreator(Food.GOLDEN_CARROT, 1, 2))
			.add(5.2, new FoodCreator(Food.COOKIE, 1, 3))
			.add(5.2, new FoodCreator(Food.BREAD, 1, 3))
			.add(0.1, new ArmorCreator(ArmorType.MILITARY))
			.add(4.905, new QuestItemCreator(QuestItem.DECHET))
			.add(4.905, new QuestItemCreator(QuestItem.AMAS))
			.add(1.09, new MoneyCreator(PhysicalMoney.BANKNOTE_1, 2, 5))
			.add(0.0002, new ItemStackableCreator(Artifacts.PARACHUTE))
			.add(0.0002, new ItemStackableCreator(Artifacts.BOOTS))
			.build(1, 3)),
	CONTRABAND(
			"de contrebandier",
			RandomizedInventory.newBuilder()
			.add(1.65, new ItemStackableCreator(GunType.G19))
			.add(1.54, new ItemStackableCreator(GunType.SKORPION))
			.add(1.43, new ItemStackableCreator(GunType.AK_20))
			.add(1.54, new ItemStackableCreator(GunType.BENELLI))
			.add(1.54, new ItemStackableCreator(GunType.LUPARA))
			.add(3.3,  new ItemStackableCreator(Knife.BICHE))
			.add(1.6,  new ItemStackableCreator(Accessory.STOCK_LIGHT))
			.add(1,    new ItemStackableCreator(Accessory.CANNON_STABILIZER))
			.add(0.4,  new ItemStackableCreator(Accessory.SCOPE_LIGHT))
			.add(8.24, new AmmoCreator(AmmoType.HANDWORKED, 3, 6, true))
			.add(2.15, new AmmoCreator(AmmoType.CARTRIDGE, 1, 1, true))
			.add(7.38, new AmmoCreator(4, 9))
			.add(7.74, new AmmoCreator(AmmoType.HANDWORKED, 4, 8, false))
			.add(5.16, new AmmoCreator(AmmoType.LIGHT, 1, 1, false))
			.add(6.88, new AmmoCreator(AmmoType.CARTRIDGE, 1, 2, false))
			.add(6.45, new AmmoCreator(AmmoType.HEAVY, 1, 3, false))
			.add(5, new FoodCreator(Food.COOKED_PORKCHOP, 1, 2))
			.add(5, new FoodCreator(Food.COOKED_SALMON, 1, 2))
			.add(5, new FoodCreator(Food.GOLDEN_CARROT, 1, 2))
			.add(5, new FoodCreator(Food.COOKIE, 1, 3))
			.add(5, new FoodCreator(Food.BREAD, 1, 3))
			.add(9, new ArmorCreator(ArmorType.GANGSTER))
			.add(3.6, new QuestItemCreator(QuestItem.DECHET))
			.add(3.6, new QuestItemCreator(QuestItem.AMAS))
			.add(0.8, new MoneyCreator(PhysicalMoney.BANKNOTE_1, 2, 5))
			.add(0.0002, new ItemStackableCreator(Artifacts.PARACHUTE))
			.add(0.0002, new ItemStackableCreator(Artifacts.BOOTS))
			.build(1, 3)),
					;
	
	private String name;
	private ConditionalMultiPicker<LootCreator, LootContext> picker;

	private LootChestType(String name, ConditionalMultiPicker<LootCreator, LootContext> picker) {
		this.name = name;
		this.picker = picker;
	}

	public String getName() {
		return name;
	}
	
	public ConditionalMultiPicker<LootCreator, LootContext> getPicker() {
		return picker;
	}

}
