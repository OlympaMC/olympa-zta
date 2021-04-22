package fr.olympa.zta.loot.crates;

import java.util.ArrayList;
import java.util.List;

import fr.olympa.zta.loot.creators.AmmoCreator;
import fr.olympa.zta.loot.creators.ArmorCreator;
import fr.olympa.zta.loot.creators.FoodCreator;
import fr.olympa.zta.loot.creators.FoodCreator.Food;
import fr.olympa.zta.loot.creators.ItemStackableCreator;
import fr.olympa.zta.loot.creators.LootCreator;
import fr.olympa.zta.loot.creators.MoneyCreator;
import fr.olympa.zta.loot.creators.QuestItemCreator;
import fr.olympa.zta.loot.creators.QuestItemCreator.QuestItem;
import fr.olympa.zta.utils.PhysicalMoney;
import fr.olympa.zta.weapons.ArmorType;
import fr.olympa.zta.weapons.Knife;
import fr.olympa.zta.weapons.guns.Accessory;
import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.GunType;

public enum CrateType {

	BASIC(
			new MoneyCreator(-1, PhysicalMoney.BANKNOTE_10, 3, 9),
			new MoneyCreator(20, PhysicalMoney.BANKNOTE_100, 1, 2),
			new AmmoCreator(15, 5, 15),
			new AmmoCreator(15, AmmoType.HEAVY, 4, 8, true),
			new AmmoCreator(15, AmmoType.LIGHT, 4, 8, true),
			new AmmoCreator(15, AmmoType.HANDWORKED, 4, 8, true),
			new AmmoCreator(15, AmmoType.CARTRIDGE, 3, 6, true),
			new FoodCreator(20, Food.GOLDEN_CARROT, 3, 7),
			new FoodCreator(20, Food.COOKED_BEEF, 5, 15),
			new ItemStackableCreator(1, GunType.STONER),
			new ItemStackableCreator(7, GunType.DRAGUNOV),
			new ItemStackableCreator(8, GunType.SDMR),
			new ItemStackableCreator(13, GunType.M16),
			new ItemStackableCreator(13, GunType.BENELLI),
			new ItemStackableCreator(16, Knife.SURIN),
			new ItemStackableCreator(15, Accessory.SCOPE_STRONG),
			new ItemStackableCreator(15, Accessory.STOCK_STRONG),
			new ItemStackableCreator(15, Accessory.CANNON_DAMAGE),
			new ArmorCreator(20, ArmorType.MILITARY)
			),
	RARE(
			new MoneyCreator(-1, PhysicalMoney.BANKNOTE_10, 4, 10),
			new MoneyCreator(25, PhysicalMoney.BANKNOTE_100, 2, 3),
			new AmmoCreator(15, 5, 15),
			new AmmoCreator(15, AmmoType.HEAVY, 5, 10, true),
			new AmmoCreator(15, AmmoType.LIGHT, 5, 10, true),
			new AmmoCreator(15, AmmoType.HANDWORKED, 5, 10, true),
			new AmmoCreator(15, AmmoType.CARTRIDGE, 4, 8, true),
			new FoodCreator(15, Food.GOLDEN_CARROT, 5, 10),
			new FoodCreator(15, Food.COOKED_BEEF, 10, 15),
			new FoodCreator(15, Food.GOLDEN_APPLE, 2, 5),
			new ItemStackableCreator(5, GunType.STONER),
			new ItemStackableCreator(9, GunType.DRAGUNOV),
			new ItemStackableCreator(7, GunType.SDMR),
			new ItemStackableCreator(13, GunType.M16),
			new ItemStackableCreator(15, Knife.SURIN),
			new ItemStackableCreator(18, Accessory.SCOPE_STRONG),
			new ItemStackableCreator(18, Accessory.STOCK_STRONG),
			new ItemStackableCreator(15, Accessory.CANNON_SILENT),
			new QuestItemCreator(0.5, QuestItem.PARACHUTE),
			new QuestItemCreator(0.5, QuestItem.BOOTS),
			new ArmorCreator(20, ArmorType.MILITARY)
			),
	;

	private List<LootCreator> creatorsSimple = new ArrayList<>();
	private List<LootCreator> creatorsAlways = new ArrayList<>();

	private CrateType(LootCreator... creators) {
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
