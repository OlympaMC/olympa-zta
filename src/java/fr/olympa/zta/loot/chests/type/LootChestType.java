package fr.olympa.zta.loot.chests.type;

import java.util.ArrayList;
import java.util.List;

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

public enum LootChestType {

	CIVIL(
			"civil",
			new ItemStackableCreator(3, GunType.M1911),
			new ItemStackableCreator(1.44, GunType.COBRA),
			new ItemStackableCreator(2.28, GunType.REM_870),
			new ItemStackableCreator(1.68, GunType.UZI),
			new ItemStackableCreator(1.44, GunType.M16),
			new ItemStackableCreator(2.16, GunType.M1897),
			new ItemStackableCreator(1.5, Accessory.STOCK_LIGHT),
			new ItemStackableCreator(0.9, Accessory.CANNON_SILENT),
			new ItemStackableCreator(0.6, Accessory.SCOPE_LIGHT),
			new AmmoCreator(6.45, AmmoType.LIGHT, 3, 6, true),
			new AmmoCreator(2.15, AmmoType.CARTRIDGE, 1, 1, true),
			new AmmoCreator(8.6, 4, 9),
			new AmmoCreator(6.45, AmmoType.HANDWORKED, 2, 4, false),
			new AmmoCreator(6.45, AmmoType.LIGHT, 4, 8, false),
			new AmmoCreator(6.45, AmmoType.CARTRIDGE, 1, 1, false),
			new AmmoCreator(6.45, AmmoType.HEAVY, 1, 3, false),
			new FoodCreator(4.8, Food.BREAD, 1, 3),
			new FoodCreator(4.8, Food.BAKED_POTATO, 1, 2),
			new FoodCreator(4.8, Food.CARROT, 1, 3),
			new FoodCreator(4.8, Food.COOKED_RABBIT, 1, 2),
			new FoodCreator(4.8, Food.COOKED_COD, 1, 2),
			new ArmorCreator(10, ArmorType.ANTIRIOT),
			new QuestItemCreator(3.6, QuestItem.DECHET),
			new QuestItemCreator(3.6, QuestItem.AMAS),
			new MoneyCreator(0.8, PhysicalMoney.BANKNOTE_1, 2, 5)),
	MILITARY(
			"militaire",
			new ItemStackableCreator(2.0, GunType.P22),
			new ItemStackableCreator(1.0, GunType.SDMR),
			new ItemStackableCreator(1.2, GunType.BARRETT),
			new ItemStackableCreator(1.8, GunType.KSG),
			new ItemStackableCreator(4.0, Knife.SURIN),
			new ItemStackableCreator(2.4, Accessory.STOCK_STRONG),
			new ItemStackableCreator(1.5, Accessory.CANNON_POWER),
			new ItemStackableCreator(0.5, Accessory.CANNON_CAC),
			new ItemStackableCreator(0.6, Accessory.SCOPE_LIGHT),
			new AmmoCreator(7.55, AmmoType.HEAVY, 3, 6, true),
			new AmmoCreator(2.85, AmmoType.CARTRIDGE, 1, 1, true),
			new AmmoCreator(7.52, 4, 9),
			new AmmoCreator(7.52, AmmoType.HANDWORKED, 1, 2, false),
			new AmmoCreator(7.52, AmmoType.LIGHT, 1, 4, false),
			new AmmoCreator(7.52, AmmoType.CARTRIDGE, 1, 2, false),
			new AmmoCreator(7.52, AmmoType.HEAVY, 4, 8, false),
			new FoodCreator(5.2, Food.COOKED_BEEF, 1, 2),
			new FoodCreator(5.2, Food.MUSHROOM_STEW, 1, 1),
			new FoodCreator(5.2, Food.GOLDEN_CARROT, 1, 2),
			new FoodCreator(5.2, Food.COOKIE, 1, 3),
			new FoodCreator(5.2, Food.BREAD, 1, 3),
			new ArmorCreator(0.1, ArmorType.MILITARY),
			new QuestItemCreator(4.905, QuestItem.DECHET),
			new QuestItemCreator(4.905, QuestItem.AMAS),
			new MoneyCreator(1.09, PhysicalMoney.BANKNOTE_1, 2, 5)),
	CONTRABAND(
			"de contrebandier",
			new ItemStackableCreator(1.65, GunType.G19),
			new ItemStackableCreator(1.54, GunType.SKORPION),
			new ItemStackableCreator(1.43, GunType.AK_20),
			new ItemStackableCreator(1.54, GunType.BENELLI),
			new ItemStackableCreator(1.54, GunType.LUPARA),
			new ItemStackableCreator(3.3, Knife.BICHE),
			new ItemStackableCreator(1.6, Accessory.STOCK_LIGHT),
			new ItemStackableCreator(1, Accessory.CANNON_STABILIZER),
			new ItemStackableCreator(0.4, Accessory.SCOPE_LIGHT),
			new AmmoCreator(8.24, AmmoType.HANDWORKED, 3, 6, true),
			new AmmoCreator(2.15, AmmoType.CARTRIDGE, 1, 1, true),
			new AmmoCreator(7.38, 4, 9),
			new AmmoCreator(7.74, AmmoType.HANDWORKED, 4, 8, false),
			new AmmoCreator(5.16, AmmoType.LIGHT, 1, 1, false),
			new AmmoCreator(6.88, AmmoType.CARTRIDGE, 1, 2, false),
			new AmmoCreator(6.45, AmmoType.HEAVY, 1, 3, false),
			new FoodCreator(5, Food.COOKED_PORKCHOP, 1, 2),
			new FoodCreator(5, Food.COOKED_SALMON, 1, 2),
			new FoodCreator(5, Food.GOLDEN_CARROT, 1, 2),
			new FoodCreator(5, Food.COOKIE, 1, 3),
			new FoodCreator(5, Food.BREAD, 1, 3),
			new ArmorCreator(9, ArmorType.GANGSTER),
			new QuestItemCreator(3.6, QuestItem.DECHET),
			new QuestItemCreator(3.6, QuestItem.AMAS),
			new MoneyCreator(0.8, PhysicalMoney.BANKNOTE_1, 2, 5));

	private List<LootCreator> creatorsSimple = new ArrayList<>();
	private List<LootCreator> creatorsAlways = new ArrayList<>();
	private String name;

	private LootChestType(String name, LootCreator... creators) {
		this.name = name;
		for (LootCreator creator : creators) {
			if (creator.getChance() == -1) {
				creatorsAlways.add(creator);
			}else creatorsSimple.add(creator);
		}
	}

	public String getName() {
		return name;
	}

	public List<LootCreator> getCreatorsSimple() {
		return creatorsSimple;
	}

	public List<LootCreator> getCreatorsAlways() {
		return creatorsAlways;
	}

}
