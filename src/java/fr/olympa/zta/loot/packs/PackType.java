package fr.olympa.zta.loot.packs;

import java.util.ArrayList;
import java.util.List;

import fr.olympa.zta.loot.creators.AmmoCreator;
import fr.olympa.zta.loot.creators.ArmorCreator;
import fr.olympa.zta.loot.creators.FoodCreator;
import fr.olympa.zta.loot.creators.FoodCreator.Food;
import fr.olympa.zta.loot.creators.ItemStackableCreator;
import fr.olympa.zta.loot.creators.LootCreator;
import fr.olympa.zta.loot.creators.MoneyCreator;
import fr.olympa.zta.utils.PhysicalMoney;
import fr.olympa.zta.weapons.ArmorType;
import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.GunType;

public enum PackType {

	BASIC(
			1000,
			10,
			"Pack de base",
			new MoneyCreator(-1, PhysicalMoney.BANKNOTE_10, 3, 9),
			new MoneyCreator(20, PhysicalMoney.BANKNOTE_100, 1, 2),
			new AmmoCreator(15, 5, 13),
			new AmmoCreator(15, AmmoType.HEAVY, 4, 8, true),
			new AmmoCreator(15, AmmoType.LIGHT, 4, 8, true),
			new AmmoCreator(15, AmmoType.HANDWORKED, 4, 8, true),
			new AmmoCreator(15, AmmoType.CARTRIDGE, 3, 6, true),
			new FoodCreator(20, Food.GOLDEN_CARROT, 3, 7),
			new FoodCreator(20, Food.COOKED_BEEF, 5, 15),
			new ItemStackableCreator(5, GunType.STONER),
			new ItemStackableCreator(7, GunType.DRAGUNOV),
			new ItemStackableCreator(8, GunType.SDMR),
			new ArmorCreator(20, ArmorType.MILITARY)
			);

	private int price, slot;
	private String name;
	private List<LootCreator> creatorsSimple = new ArrayList<>(), creatorsAlways = new ArrayList<>();
	private String[] lootsDescription;

	private PackType(int price, int slot, String name, LootCreator... creators) {
		this.price = price;
		this.slot = slot;
		this.name = name;
		for (LootCreator creator : creators) {
			if (creator.getChance() == -1) {
				creatorsAlways.add(creator);
			}else creatorsSimple.add(creator);
		}
		lootsDescription = new String[creatorsSimple.size() + creatorsAlways.size()];
		int i = 0;
		for (LootCreator creator : creatorsAlways) lootsDescription[i++] = "§a● " + creator.getTitle();
		for (LootCreator creator : creatorsSimple) lootsDescription[i++] = "§7● " + creator.getTitle();
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
	
	public List<LootCreator> getCreatorsSimple() {
		return creatorsSimple;
	}

	public List<LootCreator> getCreatorsAlways() {
		return creatorsAlways;
	}
	
	public String[] getLootsDescription() {
		return lootsDescription;
	}
	
}
