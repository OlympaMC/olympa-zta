package fr.olympa.zta.lootchests.type;

import fr.olympa.api.utils.RandomizedPicker.Chanced;

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