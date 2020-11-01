package fr.olympa.zta.lootchests.type;

import java.util.ArrayList;
import java.util.List;

import fr.olympa.zta.lootchests.creators.AmmoCreator;
import fr.olympa.zta.lootchests.creators.ArmorCreator;
import fr.olympa.zta.lootchests.creators.FoodCreator;
import fr.olympa.zta.lootchests.creators.FoodCreator.Food;
import fr.olympa.zta.lootchests.creators.ItemStackableCreator;
import fr.olympa.zta.lootchests.creators.LootCreator;
import fr.olympa.zta.lootchests.creators.MoneyCreator;
import fr.olympa.zta.lootchests.creators.QuestItemCreator;
import fr.olympa.zta.lootchests.creators.QuestItemCreator.QuestItem;
import fr.olympa.zta.weapons.ArmorType;
import fr.olympa.zta.weapons.Knife;
import fr.olympa.zta.weapons.guns.Accessory;
import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.GunType;

public enum LootChestType {

	CIVIL(
			"civil",
			new ItemStackableCreator(1.62, GunType.M1911),
			new ItemStackableCreator(1.56, GunType.COBRA),
			new ItemStackableCreator(1.62, GunType.REM_870),
			new ItemStackableCreator(1.62, GunType.UZI),
			new ItemStackableCreator(1.56, GunType.M16),
			new ItemStackableCreator(1.62, GunType.M1897),
			new ItemStackableCreator(2.4, Knife.BATTE),
			new ItemStackableCreator(1.0, Accessory.STOCK_LIGHT),
			new ItemStackableCreator(0.6, Accessory.CANNON_CAC),
			new ItemStackableCreator(0.4, Accessory.SCOPE_LIGHT),
			new AmmoCreator(6.45, AmmoType.LIGHT, 2, 5, true),
			new AmmoCreator(2.15, AmmoType.CARTRIDGE, 1, 1, true),
			new AmmoCreator(8.6, 4, 9),
			new AmmoCreator(6.45, AmmoType.HANDWORKED, 2, 4, false),
			new AmmoCreator(6.45, AmmoType.LIGHT, 3, 7, false),
			new AmmoCreator(6.45, AmmoType.CARTRIDGE, 1, 1, false),
			new AmmoCreator(6.45, AmmoType.HEAVY, 1, 3, false),
			new FoodCreator(5, Food.BREAD, 1, 3),
			new FoodCreator(5, Food.BAKED_POTATO, 1, 2),
			new FoodCreator(5, Food.CARROT, 1, 3),
			new FoodCreator(5, Food.COOKED_RABBIT, 1, 2),
			new FoodCreator(5, Food.COOKED_COD, 1, 2),
			new ArmorCreator(10, ArmorType.ANTIRIOT),
			new QuestItemCreator(3.6, QuestItem.DECHET),
			new QuestItemCreator(3.6, QuestItem.AMAS),
			new MoneyCreator(0.8, 2, 5)),
	MILITARY(
			"militaire",
			new ItemStackableCreator(2.09, GunType.P22),
			new ItemStackableCreator(2.035, GunType.SDMR),
			new ItemStackableCreator(1.32, GunType.STONER),
			new ItemStackableCreator(1.32, GunType.BARRETT),
			new ItemStackableCreator(2.035, GunType.KSG),
			new ItemStackableCreator(2.2, Knife.SURIN),
			new ItemStackableCreator(0.8, Accessory.STOCK_STRONG),
			new ItemStackableCreator(0.5, Accessory.CANNON_POWER),
			new ItemStackableCreator(0.5, Accessory.CANNON_SILENT),
			new ItemStackableCreator(0.2, Accessory.SCOPE_STRONG),
			new AmmoCreator(6.45, AmmoType.HEAVY, 2, 6, true),
			new AmmoCreator(2.15, AmmoType.CARTRIDGE, 1, 1, true),
			new AmmoCreator(6.88, 4, 9),
			new AmmoCreator(6.88, AmmoType.HANDWORKED, 1, 2, false),
			new AmmoCreator(6.88, AmmoType.LIGHT, 1, 4, false),
			new AmmoCreator(6.88, AmmoType.CARTRIDGE, 1, 2, false),
			new AmmoCreator(6.88, AmmoType.HEAVY, 3, 7, false),
			new FoodCreator(5, Food.COOKED_BEEF, 1, 2),
			new FoodCreator(5, Food.MUSHROOM_STEW, 1, 1),
			new FoodCreator(5, Food.GOLDEN_CARROT, 1, 2),
			new FoodCreator(5, Food.COOKIE, 1, 3),
			new FoodCreator(5, Food.BREAD, 1, 3),
			new ArmorCreator(11, ArmorType.MILITARY),
			new QuestItemCreator(3.6, QuestItem.DECHET),
			new QuestItemCreator(3.6, QuestItem.AMAS),
			new MoneyCreator(0.8, 2, 5)),
	CONTRABAND(
			"de contrebandier",
			new ItemStackableCreator(1.65, GunType.G19),
			new ItemStackableCreator(1.54, GunType.SKORPION),
			new ItemStackableCreator(1.43, GunType.AK_20),
			new ItemStackableCreator(1.54, GunType.BENELLI),
			new ItemStackableCreator(1.1, GunType.DRAGUNOV),
			new ItemStackableCreator(1.54, GunType.LUPARA),
			new ItemStackableCreator(2.2, Knife.BICHE),
			new ItemStackableCreator(0.8, Accessory.STOCK_LIGHT),
			new ItemStackableCreator(0.5, Accessory.CANNON_DAMAGE),
			new ItemStackableCreator(0.5, Accessory.CANNON_STABILIZER),
			new ItemStackableCreator(0.2, Accessory.SCOPE_LIGHT),
			new AmmoCreator(7.74, AmmoType.HANDWORKED, 2, 5, true),
			new AmmoCreator(2.15, AmmoType.CARTRIDGE, 1, 1, true),
			new AmmoCreator(6.88, 4, 9),
			new AmmoCreator(7.74, AmmoType.HANDWORKED, 3, 7, false),
			new AmmoCreator(5.16, AmmoType.LIGHT, 1, 1, false),
			new AmmoCreator(6.88, AmmoType.CARTRIDGE, 1, 2, false),
			new AmmoCreator(6.45, AmmoType.HEAVY, 1, 3, false),
			new FoodCreator(5, Food.COOKED_PORKCHOP, 1, 2),
			new FoodCreator(5, Food.COOKED_SALMON, 1, 2),
			new FoodCreator(5, Food.GOLDEN_CARROT, 1, 2),
			new FoodCreator(5, Food.COOKIE, 1, 3),
			new FoodCreator(5, Food.BREAD, 1, 3),
			new ArmorCreator(11, ArmorType.GANGSTER),
			new QuestItemCreator(3.6, QuestItem.DECHET),
			new QuestItemCreator(3.6, QuestItem.AMAS),
			new MoneyCreator(0.8, 2, 5));

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
