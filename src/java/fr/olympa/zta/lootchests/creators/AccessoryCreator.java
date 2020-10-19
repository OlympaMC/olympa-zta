package fr.olympa.zta.lootchests.creators;

import java.util.Random;

import org.bukkit.entity.Player;

import fr.olympa.zta.weapons.guns.Accessory;

public class AccessoryCreator implements LootCreator {

	private double chance;
	private Accessory type;

	public AccessoryCreator(double chance, Accessory type) {
		this.chance = chance;
		this.type = type;
	}

	public double getChance() {
		return chance;
	}

	public Loot create(Player p, Random random) {
		return new Loot(type.createItem());
	}

}
