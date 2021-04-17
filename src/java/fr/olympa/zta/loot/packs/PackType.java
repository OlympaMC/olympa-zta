package fr.olympa.zta.loot.packs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.olympa.api.economy.OlympaMoney;
import fr.olympa.api.utils.RandomizedPicker;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.zta.loot.creators.AmmoCreator;
import fr.olympa.zta.loot.creators.ArmorCreator;
import fr.olympa.zta.loot.creators.FoodCreator;
import fr.olympa.zta.loot.creators.FoodCreator.Food;
import fr.olympa.zta.loot.creators.ItemStackableCreator;
import fr.olympa.zta.loot.creators.LootCreator;
import fr.olympa.zta.weapons.ArmorType;
import fr.olympa.zta.weapons.guns.Accessory;
import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.GunType;

public enum PackType implements RandomizedPicker<LootCreator> {

	BASIC(
			10000,
			11,
			2,
			2,
			"basique du militaire",
			new AmmoCreator(-1, AmmoType.HEAVY, 20, 30, true),
			new FoodCreator(20, Food.COOKED_BEEF, 10, 15),
			new ItemStackableCreator(7, GunType.P22),
			new ItemStackableCreator(8, GunType.KSG),
			new ArmorCreator(20, ArmorType.MILITARY)),
	AMMOS(
			2000,
			13,
			5,
			7,
			"de munitions",
			new AmmoCreator(-1, 15, 20),
			new AmmoCreator(-1, AmmoType.HEAVY, 15, 20, true),
			new AmmoCreator(-1, AmmoType.LIGHT, 15, 20, true),
			new AmmoCreator(-1, AmmoType.HANDWORKED, 15, 20, true),
			new AmmoCreator(-1, AmmoType.CARTRIDGE, 10, 15, true),
			new AmmoCreator(25, AmmoType.HEAVY, 10, 15, false),
			new AmmoCreator(25, AmmoType.LIGHT, 10, 15, false),
			new AmmoCreator(25, AmmoType.HANDWORKED, 10, 15, false),
			new AmmoCreator(25, AmmoType.CARTRIDGE, 10, 15, false)),
	RARE(
			10000,
			30,
			1,
			1,
			"arme rare",
			new FoodCreator(20, Food.GOLDEN_APPLE, 10, 10),
			new ItemStackableCreator(10, GunType.STONER),
			new ItemStackableCreator(15, GunType.DRAGUNOV),
			new ItemStackableCreator(20, GunType.SDMR)),
	ACCESSORIES(
			5000,
			15,
			2,
			2,
			"d'accessoires",
			new AmmoCreator(-1, 10, 15),
			new ItemStackableCreator(8, Accessory.CANNON_CAC),
			new ItemStackableCreator(8, Accessory.CANNON_DAMAGE),
			new ItemStackableCreator(10, Accessory.CANNON_POWER),
			new ItemStackableCreator(8, Accessory.CANNON_SILENT),
			new ItemStackableCreator(9, Accessory.CANNON_STABILIZER),
			new ItemStackableCreator(10, Accessory.SCOPE_LIGHT),
			new ItemStackableCreator(9, Accessory.SCOPE_STRONG),
			new ItemStackableCreator(10, Accessory.STOCK_LIGHT),
			new ItemStackableCreator(8, Accessory.STOCK_STRONG)
			),
	;

	private int price, slot;
	private int min, max;
	private String name;
	private List<LootCreator> creatorsSimple = new ArrayList<>(), creatorsAlways = new ArrayList<>();
	private String[] lootsDescription;

	private PackType(int price, int slot, int min, int max, String name, LootCreator... creators) {
		this.price = price;
		this.slot = slot;
		this.min = min;
		this.max = max;
		this.name = name;
		for (LootCreator creator : creators) {
			if (creator.getChance() == -1) {
				creatorsAlways.add(creator);
			}else creatorsSimple.add(creator);
		}
		lootsDescription = new String[creatorsSimple.size() + creatorsAlways.size() + 4];
		int i = 0;
		lootsDescription[i++] = "";
		lootsDescription[i++] = "§7§nContient :";
		for (LootCreator creator : creatorsAlways) lootsDescription[i++] = "§a● " + creator.getTitle();
		for (LootCreator creator : creatorsSimple) lootsDescription[i++] = "§8● §7" + creator.getTitle();
		lootsDescription[i++] = "";
		lootsDescription[i++] = SpigotUtils.getBarsWithLoreLength(name, Arrays.asList(lootsDescription), OlympaMoney.format(price));
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
	
	public String[] getLootsDescription() {
		return lootsDescription;
	}
	
	@Override
	public int getMinItems() {
		return min;
	}
	
	@Override
	public int getMaxItems() {
		return max;
	}
	
	@Override
	public List<LootCreator> getObjectList() {
		return creatorsSimple;
	}

	@Override
	public List<LootCreator> getAlwaysObjectList() {
		return creatorsAlways;
	}
	
}
