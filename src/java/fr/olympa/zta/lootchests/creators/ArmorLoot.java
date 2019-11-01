package fr.olympa.zta.lootchests.creators;

import java.util.Random;

import org.bukkit.entity.Player;

import fr.olympa.zta.weapons.ArmorType;
import fr.olympa.zta.weapons.ArmorType.ArmorSlot;

public class ArmorLoot implements LootCreator {

	private double chance;
	private ArmorType type;

	public ArmorLoot(double chance, ArmorType type) {
		this.chance = chance;
		this.type = type;
	}

	public double getChance() {
		return chance;
	}

	public void give(Player p, Random random) {
		type.give(p, ArmorSlot.values()[random.nextInt(4)]); // pièce d'armure aléatoire
	}

}
