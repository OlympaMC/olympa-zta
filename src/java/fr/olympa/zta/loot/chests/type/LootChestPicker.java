package fr.olympa.zta.loot.chests.type;

import java.util.ArrayList;
import java.util.List;

import fr.olympa.api.utils.RandomizedPicker;
import fr.olympa.api.utils.RandomizedPicker.Chanced;
import fr.olympa.zta.loot.chests.type.LootChestPicker.LootChestCreator;

public class LootChestPicker implements RandomizedPicker<LootChestCreator> {

	private List<LootChestCreator> creatorsSimple = new ArrayList<>();
	private List<LootChestCreator> creatorsAlways = new ArrayList<>();

	public LootChestPicker add(LootChestType type, double chance) {
		LootChestCreator creator = new LootChestCreator(type, chance);
		if (chance == -1) {
			creatorsAlways.add(creator);
		}else creatorsSimple.add(creator);
		return this;
	}
	
	@Override
	public int getMinItems() {
		return 1;
	}

	@Override
	public int getMaxItems() {
		return 1;
	}

	@Override
	public List<LootChestCreator> getObjectList() {
		return creatorsSimple;
	}

	@Override
	public List<LootChestCreator> getAlwaysObjectList() {
		return creatorsAlways;
	}
	
	public class LootChestCreator implements Chanced {
		
		private LootChestType type;
		private double chance;
		
		public LootChestCreator(LootChestType type, double chance) {
			this.type = type;
			this.chance = chance;
		}
		
		public LootChestType getType() {
			return type;
		}
		
		@Override
		public double getChance() {
			return chance;
		}
		
	}

}
