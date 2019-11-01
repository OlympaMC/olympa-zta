package fr.olympa.zta.lootchests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.olympa.zta.lootchests.creators.AmmoLoot;
import fr.olympa.zta.lootchests.creators.ArmorLoot;
import fr.olympa.zta.lootchests.creators.FoodLoot;
import fr.olympa.zta.lootchests.creators.FoodLoot.Food;
import fr.olympa.zta.lootchests.creators.ItemStackableCreator;
import fr.olympa.zta.lootchests.creators.LootCreator;
import fr.olympa.zta.weapons.ArmorType;
import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.Gun870;
import fr.olympa.zta.weapons.guns.GunCobra;
import fr.olympa.zta.weapons.guns.GunM16;
import fr.olympa.zta.weapons.guns.GunM1897;
import fr.olympa.zta.weapons.guns.GunM1911;
import fr.olympa.zta.weapons.guns.GunUZI;
import fr.olympa.zta.weapons.guns.accessories.ScopeLight;
import fr.olympa.zta.weapons.guns.accessories.StockLight;
import fr.olympa.zta.weapons.knives.KnifeBatte;

public class LootChestType {

	public static final LootChestType CIVIL = new LootChestType(Arrays.asList(
			new ItemStackableCreator<>(1.485, GunM1911.class), new ItemStackableCreator<>(1.430, GunCobra.class), new ItemStackableCreator<>(1.485, Gun870.class), new ItemStackableCreator<>(1.485, GunUZI.class), new ItemStackableCreator<>(1.430, GunM16.class), new ItemStackableCreator<>(1.485, GunM1897.class), new ItemStackableCreator<>(2.2, KnifeBatte.class),
			new ItemStackableCreator<>(1.4, StockLight.class), new ItemStackableCreator<>(0.6, ScopeLight.class),
			new AmmoLoot(5.25, AmmoType.LIGHT, 1, 2, true), new AmmoLoot(1.75, AmmoType.CARTRIDGE, 1, 2, true), new AmmoLoot(5.6, 1, 3), new AmmoLoot(5.6, AmmoType.HANDWORKED, 1, 1, false), new AmmoLoot(5.6, AmmoType.LIGHT, 1, 1, false), new AmmoLoot(5.6, AmmoType.CARTRIDGE, 1, 1, false), new AmmoLoot(5.6, AmmoType.HEAVY, 1, 1, false),
			new FoodLoot(8, Food.BREAD, 1, 3), new FoodLoot(8, Food.BAKED_POTATO, 1, 2), new FoodLoot(8, Food.CARROT, 1, 3), new FoodLoot(8, Food.COOKED_RABBIT, 1, 2), new FoodLoot(8, Food.COOKED_COD, 1, 2),
			new ArmorLoot(12, ArmorType.ANTIRIOT)
			));

	private List<LootCreator> creatorsSimple = new ArrayList<>();
	private List<LootCreator> creatorsAlways = new ArrayList<>();

	private LootChestType(List<LootCreator> creators) {
		for (LootCreator creator : creators) {
			if (creator.getChance() == -1) {
				creatorsAlways.add(creator);
			}else creatorsSimple.add(creator);
		}
	}

	public List<LootCreator> getCreatorsSimple() {
		return creatorsSimple;
	}

	public List<LootCreator> getCreatorsAlways() {
		return creatorsAlways;
	}

}
