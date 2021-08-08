package fr.olympa.zta.loot.chests.type;

import fr.olympa.api.common.randomized.RandomizedPickerBase.ConditionalMultiPicker;
import fr.olympa.api.common.randomized.RandomizedPickerBuilder;
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
import fr.olympa.zta.loot.creators.ItemStackableCreator.GunConditionned;
import fr.olympa.zta.loot.creators.LootCreator;
import fr.olympa.zta.loot.creators.MoneyCreator;
import fr.olympa.zta.loot.creators.QuestItemCreator;
import fr.olympa.zta.weapons.ArmorType;
import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.GunType;

public enum LootChestType {
	
	BASIC(
			"basique",
			RandomizedInventory.newBuilder()
			.add(32, new AmmoCreator.BestCreator(3, 5, 7, 0.4, 1D, 0.5D))
			.add(7, new QuestItemCreator(QuestItem.AMAS, 1, 3))
			.add(10, new QuestItemCreator(QuestItem.DECHET, 1, 2))
			.add(0.2, new QuestItemCreator(QuestItem.BOITIER_PROG, 1, 1))
			.add(14, new MoneyCreator(PhysicalMoney.BANKNOTE_1, 1, 4))
			.add(1, new MoneyCreator(PhysicalMoney.BANKNOTE_10, 1, 1))
			.add(16, new FoodCreator(Food.BAKED_POTATO, 2, 4))
			.add(0.004, new ItemStackableCreator(Artifacts.BOOTS))
			.add(0.004, new ItemStackableCreator(Artifacts.PARACHUTE))
			.build(RandomizedPickerBuilder.<Integer>newBuilder().add(0.3, 1).add(0.4, 2).add(0.3, 3).build())),
	SAFE(
			"de zone sécurisée",
			RandomizedInventory.newBuilder()
			.add(15, new GunConditionned(GunType.M1911, 0.4, 0.15))
			.add(4, new GunConditionned(GunType.P22, 0.15, 0.7))
			.add(25, new AmmoCreator.BestCreator(3, 4, 7, 0.5, 1, 0))
			.add(25, new AmmoCreator(AmmoType.LIGHT, 3, 7, 0.5))
			.add(21, new FoodCreator(Food.COOKIE, 3, 5))
			.add(8, new MoneyCreator(PhysicalMoney.BANKNOTE_1, 1, 4))
			.build(RandomizedPickerBuilder.<Integer>newBuilder().add(0.3, 1).add(0.4, 2).add(0.3, 3).build())),
	EASY(
			"de zone modérée",
			RandomizedInventory.newBuilder()
			.add(2, new GunConditionned(GunType.UZI, 0.15, 0.7))
			.add(3, new GunConditionned(GunType.LUPARA, 0.15, 0.7))
			.add(25, new AmmoCreator.BestCreator(2, 4, 7, 0.4, 1, 0))
			.add(20, new AmmoCreator.AmmoConditionned(new AmmoCreator(AmmoType.LIGHT, 3, 7, 0.4), 0.7))
			.add(20, new AmmoCreator(AmmoType.CARTRIDGE, 3, 7, 0.5))
			.add(15, new FoodCreator(Food.COOKED_SALMON, 2, 4))
			.add(8, new MoneyCreator(PhysicalMoney.BANKNOTE_1, 1, 4))
			.add(2.6, new ArmorCreator(ArmorType.GANGSTER, 0.2))
			.build(RandomizedPickerBuilder.<Integer>newBuilder().add(0.3, 1).add(0.4, 2).add(0.3, 3).build(), 6)),
	MEDIUM(
			"de zone à risques",
			RandomizedInventory.newBuilder()
			.add(0.005, new ItemStackableCreator(GunType.DRAGUNOV))
			.add(2, new GunConditionned(GunType.AK_20, 0.15, 0.6))
			.add(3, new GunConditionned(GunType.BENELLI, 0.15, 0.7))
			.add(25, new AmmoCreator.BestCreator(2, 4, 6, 0.4, 1, 0))
			.add(22, new AmmoCreator.AmmoConditionned(new AmmoCreator(AmmoType.HANDWORKED, 3, 7, 0.4), 0.7))
			.add(20, new AmmoCreator(AmmoType.CARTRIDGE, 3, 7, 0.5))
			.add(15, new FoodCreator(Food.COOKED_SALMON, 2, 4))
			.add(8, new MoneyCreator(PhysicalMoney.BANKNOTE_1, 1, 5))
			.add(1.9, new ArmorCreator(ArmorType.ANTIRIOT, 0.3))
			.build(RandomizedPickerBuilder.<Integer>newBuilder().add(0.25, 1).add(0.4, 2).add(0.35, 3).build(), 6)),
	HARD(
			"de zone rouge",
			RandomizedInventory.newBuilder()
			.add(0.005, new ItemStackableCreator(GunType.DRAGUNOV))
			.add(2, new GunConditionned(GunType.M16, 0.15, 0.5))
			.add(3, new GunConditionned(GunType.G19, 0.15, 0.7))
			.add(25, new AmmoCreator.BestCreator(2, 4, 6, 0.4, 1, 0))
			.add(24, new AmmoCreator.AmmoConditionned(new AmmoCreator(AmmoType.HEAVY, 3, 7, 0.4), 0.7))
			.add(20, new AmmoCreator(AmmoType.HANDWORKED, 3, 7, 0.5))
			.add(15.2, new FoodCreator(Food.COOKED_BEEF, 3, 4))
			.add(8, new MoneyCreator(PhysicalMoney.BANKNOTE_1, 2, 5))
			.add(0.5, new ArmorCreator(ArmorType.MILITARY, 0.5))
			.build(RandomizedPickerBuilder.<Integer>newBuilder().add(0.28, 1).add(0.4, 2).add(0.32, 3).build(), 5.2)),
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
