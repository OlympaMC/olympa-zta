package fr.olympa.zta.lootchests.creators;

import java.util.Random;

import org.bukkit.entity.Player;

import fr.olympa.zta.weapons.knives.Knife;

public class KnifeCreator implements LootCreator {

	private double chance;
	private Knife type;

	public KnifeCreator(double chance, Knife type) {
		this.chance = chance;
		this.type = type;
	}

	public double getChance() {
		return chance;
	}

	public Loot create(Player p, Random random) {
		return new Loot(type.getItem());
	}

}
