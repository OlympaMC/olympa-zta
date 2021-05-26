package fr.olympa.zta.mobs;

import fr.olympa.api.utils.RandomizedPicker;
import fr.olympa.zta.mobs.custom.Mobs.Zombies;

public class MobSpawningConfig {
	
	private final int minDistance;
	private final int spawnAmount;
	private final int maxEntitiesPerChunk;
	private final RandomizedPicker<Zombies> zombiePicker;
	
	public MobSpawningConfig(int minDistance, int spawnAmount, int maxEntitiesPerChunk, RandomizedPicker<Zombies> zombiePicker) {
		this.minDistance = minDistance;
		this.spawnAmount = spawnAmount;
		this.maxEntitiesPerChunk = maxEntitiesPerChunk;
		this.zombiePicker = zombiePicker;
	}
	
	public int minDistance() {
		return minDistance;
	}
	
	public int spawnAmount() {
		return spawnAmount;
	}
	
	public int maxEntitiesPerChunk() {
		return maxEntitiesPerChunk;
	}
	
	public RandomizedPicker<Zombies> getZombiePicker() {
		return zombiePicker;
	}
	
}
