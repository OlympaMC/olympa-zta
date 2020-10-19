package fr.olympa.zta.lootchests.type;

import java.util.ArrayList;
import java.util.List;

import fr.olympa.zta.lootchests.creators.AccessoryCreator;
import fr.olympa.zta.lootchests.creators.AmmoCreator;
import fr.olympa.zta.lootchests.creators.ArmorCreator;
import fr.olympa.zta.lootchests.creators.FoodCreator;
import fr.olympa.zta.lootchests.creators.FoodCreator.Food;
import fr.olympa.zta.lootchests.creators.GunCreator;
import fr.olympa.zta.lootchests.creators.KnifeCreator;
import fr.olympa.zta.lootchests.creators.LootCreator;
import fr.olympa.zta.lootchests.creators.MoneyCreator;
import fr.olympa.zta.lootchests.creators.QuestItemCreator;
import fr.olympa.zta.lootchests.creators.QuestItemCreator.QuestItem;
import fr.olympa.zta.weapons.ArmorType;
import fr.olympa.zta.weapons.guns.Accessory;
import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.created.Gun870;
import fr.olympa.zta.weapons.guns.created.GunAK;
import fr.olympa.zta.weapons.guns.created.GunBarrett;
import fr.olympa.zta.weapons.guns.created.GunBenelli;
import fr.olympa.zta.weapons.guns.created.GunCobra;
import fr.olympa.zta.weapons.guns.created.GunDragunov;
import fr.olympa.zta.weapons.guns.created.GunG19;
import fr.olympa.zta.weapons.guns.created.GunKSG;
import fr.olympa.zta.weapons.guns.created.GunLupara;
import fr.olympa.zta.weapons.guns.created.GunM16;
import fr.olympa.zta.weapons.guns.created.GunM1897;
import fr.olympa.zta.weapons.guns.created.GunM1911;
import fr.olympa.zta.weapons.guns.created.GunP22;
import fr.olympa.zta.weapons.guns.created.GunSDMR;
import fr.olympa.zta.weapons.guns.created.GunSkorpion;
import fr.olympa.zta.weapons.guns.created.GunStoner;
import fr.olympa.zta.weapons.guns.created.GunUZI;
import fr.olympa.zta.weapons.knives.Knife;

public enum LootChestType {

	CIVIL(
			"civil",
			new GunCreator(1.755, GunM1911.class),
			new GunCreator(1.690, GunCobra.class),
			new GunCreator(1.755, Gun870.class),
			new GunCreator(1.755, GunUZI.class),
			new GunCreator(1.690, GunM16.class),
			new GunCreator(1.755, GunM1897.class),
			new KnifeCreator(2.6, Knife.MATRAQUE),
			new AccessoryCreator(1.0, Accessory.STOCK_LIGHT),
			new AccessoryCreator(0.6, Accessory.CANNON_CAC),
			new AccessoryCreator(0.4, Accessory.SCOPE_LIGHT),
			new AmmoCreator(6.3, AmmoType.LIGHT, 2, 5, true),
			new AmmoCreator(2.1, AmmoType.CARTRIDGE, 1, 1, true),
			new AmmoCreator(8.4, 4, 9),
			new AmmoCreator(6.3, AmmoType.HANDWORKED, 2, 4, false),
			new AmmoCreator(6.3, AmmoType.LIGHT, 3, 7, false),
			new AmmoCreator(6.3, AmmoType.CARTRIDGE, 1, 1, false),
			new AmmoCreator(6.3, AmmoType.HEAVY, 1, 3, false),
			new FoodCreator(5, Food.BREAD, 1, 3),
			new FoodCreator(5, Food.BAKED_POTATO, 1, 2),
			new FoodCreator(5, Food.CARROT, 1, 3),
			new FoodCreator(5, Food.COOKED_RABBIT, 1, 2),
			new FoodCreator(5, Food.COOKED_COD, 1, 2),
			new ArmorCreator(10, ArmorType.ANTIRIOT),
			new QuestItemCreator(3.6, QuestItem.DECHET),
			new QuestItemCreator(3.6, QuestItem.AMAS),
			new MoneyCreator(0.8, 1, 3)),
	MILITARY(
			"militaire",
			new GunCreator(2.09, GunP22.class),
			new GunCreator(2.035, GunSDMR.class),
			new GunCreator(1.32, GunStoner.class),
			new GunCreator(1.32, GunBarrett.class),
			new GunCreator(2.035, GunKSG.class),
			new KnifeCreator(2.2, Knife.SURIN),
			new AccessoryCreator(0.8, Accessory.STOCK_STRONG),
			new AccessoryCreator(0.5, Accessory.CANNON_POWER),
			new AccessoryCreator(0.5, Accessory.CANNON_SILENT),
			new AccessoryCreator(0.2, Accessory.SCOPE_STRONG),
			new AmmoCreator(6.3, AmmoType.HEAVY, 2, 6, true),
			new AmmoCreator(2.1, AmmoType.CARTRIDGE, 1, 1, true),
			new AmmoCreator(6.72, 4, 9),
			new AmmoCreator(6.72, AmmoType.HANDWORKED, 1, 2, false),
			new AmmoCreator(6.72, AmmoType.LIGHT, 1, 4, false),
			new AmmoCreator(6.72, AmmoType.CARTRIDGE, 1, 2, false),
			new AmmoCreator(6.72, AmmoType.HEAVY, 3, 7, false),
			new FoodCreator(5, Food.COOKED_BEEF, 1, 2),
			new FoodCreator(5, Food.MUSHROOM_STEW, 1, 1),
			new FoodCreator(5, Food.GOLDEN_CARROT, 1, 2),
			new FoodCreator(5, Food.COOKIE, 1, 3),
			new FoodCreator(5, Food.BREAD, 1, 3),
			new ArmorCreator(12, ArmorType.MILITARY),
			new QuestItemCreator(3.6, QuestItem.DECHET),
			new QuestItemCreator(3.6, QuestItem.AMAS),
			new MoneyCreator(0.8, 1, 3)),
	CONTRABAND(
			"de contrebandier",
			new GunCreator(1.65, GunG19.class),
			new GunCreator(1.54, GunSkorpion.class),
			new GunCreator(1.43, GunAK.class),
			new GunCreator(1.54, GunBenelli.class),
			new GunCreator(1.1, GunDragunov.class),
			new GunCreator(1.54, GunLupara.class),
			new KnifeCreator(2.2, Knife.BICHE),
			new AccessoryCreator(0.8, Accessory.STOCK_LIGHT),
			new AccessoryCreator(0.5, Accessory.CANNON_DAMAGE),
			new AccessoryCreator(0.5, Accessory.CANNON_STABILIZER),
			new AccessoryCreator(0.2, Accessory.SCOPE_LIGHT),
			new AmmoCreator(7.56, AmmoType.HANDWORKED, 2, 5, true),
			new AmmoCreator(2.1, AmmoType.CARTRIDGE, 1, 1, true),
			new AmmoCreator(6.72, 4, 9),
			new AmmoCreator(7.56, AmmoType.HANDWORKED, 3, 7, false),
			new AmmoCreator(5.04, AmmoType.LIGHT, 1, 1, false),
			new AmmoCreator(6.72, AmmoType.CARTRIDGE, 1, 2, false),
			new AmmoCreator(6.3, AmmoType.HEAVY, 1, 3, false),
			new FoodCreator(5, Food.COOKED_PORKCHOP, 1, 2),
			new FoodCreator(5, Food.COOKED_SALMON, 1, 2),
			new FoodCreator(5, Food.GOLDEN_CARROT, 1, 2),
			new FoodCreator(5, Food.COOKIE, 1, 3),
			new FoodCreator(5, Food.BREAD, 1, 3),
			new ArmorCreator(12, ArmorType.GANGSTER),
			new QuestItemCreator(3.6, QuestItem.DECHET),
			new QuestItemCreator(3.6, QuestItem.AMAS),
			new MoneyCreator(0.8, 1, 3));

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
