package fr.olympa.zta.lootchests.type;

import java.util.ArrayList;
import java.util.List;

import fr.olympa.zta.lootchests.creators.AmmoCreator;
import fr.olympa.zta.lootchests.creators.ArmorCreator;
import fr.olympa.zta.lootchests.creators.FoodCreator;
import fr.olympa.zta.lootchests.creators.FoodCreator.Food;
import fr.olympa.zta.lootchests.creators.ItemStackableCreator;
import fr.olympa.zta.lootchests.creators.LootCreator;
import fr.olympa.zta.weapons.ArmorType;
import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.accessories.CannonDamage;
import fr.olympa.zta.weapons.guns.accessories.CannonPower;
import fr.olympa.zta.weapons.guns.accessories.CannonSilent;
import fr.olympa.zta.weapons.guns.accessories.CannonStabilizer;
import fr.olympa.zta.weapons.guns.accessories.ScopeLight;
import fr.olympa.zta.weapons.guns.accessories.ScopeStrong;
import fr.olympa.zta.weapons.guns.accessories.StockLight;
import fr.olympa.zta.weapons.guns.accessories.StockStrong;
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
import fr.olympa.zta.weapons.knives.KnifeBatte;
import fr.olympa.zta.weapons.knives.KnifeBiche;
import fr.olympa.zta.weapons.knives.KnifeSurin;

public enum LootChestType {

	CIVIL("civil", new ItemStackableCreator<>(1.485, GunM1911.class), new ItemStackableCreator<>(1.430, GunCobra.class), new ItemStackableCreator<>(1.485, Gun870.class), new ItemStackableCreator<>(1.485, GunUZI.class), new ItemStackableCreator<>(1.430, GunM16.class), new ItemStackableCreator<>(1.485, GunM1897.class), new ItemStackableCreator<>(2.2, KnifeBatte.class),
				new ItemStackableCreator<>(1.4, StockLight.class), new ItemStackableCreator<>(0.6, ScopeLight.class),
				new AmmoCreator(5.25, AmmoType.LIGHT, 1, 2, true), new AmmoCreator(1.75, AmmoType.CARTRIDGE, 1, 2, true), new AmmoCreator(5.6, 1, 3), new AmmoCreator(5.6, AmmoType.HANDWORKED, 1, 1, false), new AmmoCreator(5.6, AmmoType.LIGHT, 1, 1, false), new AmmoCreator(5.6, AmmoType.CARTRIDGE, 1, 1, false), new AmmoCreator(5.6, AmmoType.HEAVY, 1, 1, false),
				new FoodCreator(8, Food.BREAD, 1, 3), new FoodCreator(8, Food.BAKED_POTATO, 1, 2), new FoodCreator(8, Food.CARROT, 1, 3), new FoodCreator(8, Food.COOKED_RABBIT, 1, 2), new FoodCreator(8, Food.COOKED_COD, 1, 2),
			new ArmorCreator(12, ArmorType.ANTIRIOT)),
	MILITARY("militaire", new ItemStackableCreator<>(2.09, GunP22.class), new ItemStackableCreator<>(2.035, GunSDMR.class), new ItemStackableCreator<>(1.32, GunStoner.class), new ItemStackableCreator<>(1.32, GunBarrett.class), new ItemStackableCreator<>(2.035, GunKSG.class), new ItemStackableCreator<>(2.2, KnifeSurin.class),
				new ItemStackableCreator<>(0.8, StockStrong.class), new ItemStackableCreator<>(0.5, CannonPower.class), new ItemStackableCreator<>(0.5, CannonSilent.class), new ItemStackableCreator<>(0.2, ScopeStrong.class),
				new AmmoCreator(5.25, AmmoType.HEAVY, 1, 2, true), new AmmoCreator(1.75, AmmoType.CARTRIDGE, 1, 2, true), new AmmoCreator(5.6, 1, 3), new AmmoCreator(5.6, AmmoType.HANDWORKED, 1, 1, false), new AmmoCreator(5.6, AmmoType.LIGHT, 1, 1, false), new AmmoCreator(5.6, AmmoType.CARTRIDGE, 1, 1, false), new AmmoCreator(5.6, AmmoType.HEAVY, 1, 1, false),
				new FoodCreator(8, Food.COOKED_BEEF, 1, 2), new FoodCreator(8, Food.MUSHROOM_STEW, 1, 1), new FoodCreator(8, Food.GOLDEN_CARROT, 1, 2), new FoodCreator(8, Food.COOKIE, 1, 3), new FoodCreator(8, Food.BREAD, 1, 3),
			new ArmorCreator(12, ArmorType.MILITARY)),
	CONTRABAND("de contrebandier", new ItemStackableCreator<>(1.65, GunG19.class), new ItemStackableCreator<>(1.54, GunSkorpion.class), new ItemStackableCreator<>(1.43, GunAK.class), new ItemStackableCreator<>(1.54, GunBenelli.class), new ItemStackableCreator<>(1.1, GunDragunov.class), new ItemStackableCreator<>(1.54, GunLupara.class), new ItemStackableCreator<>(2.2, KnifeBiche.class),
				new ItemStackableCreator<>(0.8, StockLight.class), new ItemStackableCreator<>(0.5, CannonDamage.class), new ItemStackableCreator<>(0.5, CannonStabilizer.class), new ItemStackableCreator<>(0.2, ScopeLight.class),
				new AmmoCreator(5.25, AmmoType.HANDWORKED, 1, 2, true), new AmmoCreator(1.75, AmmoType.CARTRIDGE, 1, 2, true), new AmmoCreator(5.6, 1, 3), new AmmoCreator(5.6, AmmoType.HANDWORKED, 1, 1, false), new AmmoCreator(5.6, AmmoType.LIGHT, 1, 1, false), new AmmoCreator(5.6, AmmoType.CARTRIDGE, 1, 1, false), new AmmoCreator(5.6, AmmoType.HEAVY, 1, 1, false),
				new FoodCreator(8, Food.COOKED_PORKCHOP, 1, 2), new FoodCreator(8, Food.COOKED_SALMON, 1, 2), new FoodCreator(8, Food.GOLDEN_CARROT, 1, 2), new FoodCreator(8, Food.COOKIE, 1, 3), new FoodCreator(8, Food.BREAD, 1, 3),
			new ArmorCreator(12, ArmorType.GANGSTER));

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
