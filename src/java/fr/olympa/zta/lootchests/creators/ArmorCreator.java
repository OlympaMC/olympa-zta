package fr.olympa.zta.lootchests.creators;

import java.util.Random;

import fr.olympa.zta.weapons.ArmorType;
import fr.olympa.zta.weapons.ArmorType.ArmorSlot;

public class ArmorCreator implements LootCreator {

	private double chance;
	private ArmorType type;

	public ArmorCreator(double chance, ArmorType type) {
		this.chance = chance;
		this.type = type;
	}

	public double getChance() {
		return chance;
	}

	public Loot create(Random random) {
		return new Loot(type.get(ArmorSlot.values()[random.nextInt(4)])); // pièce d'armure aléatoire
	}

}
