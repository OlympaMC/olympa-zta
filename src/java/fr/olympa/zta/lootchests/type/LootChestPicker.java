package fr.olympa.zta.lootchests.type;

import java.util.ArrayList;
import java.util.List;

import fr.olympa.api.utils.AbstractRandomizedPicker;

public class LootChestPicker implements AbstractRandomizedPicker<LootChestCreator> {

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

}
