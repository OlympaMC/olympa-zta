package fr.olympa.zta.mobs;

import fr.olympa.api.common.randomized.RandomizedPickerBase.ConditionalPicker;
import fr.olympa.zta.mobs.MobSpawning.MobSpawningContext;
import fr.olympa.zta.mobs.custom.Mobs.Zombies;

public record MobSpawningConfig(int minDistance, int minPlayerDistance, int spawnAmount, int maxEntitiesPerChunk, ConditionalPicker<Zombies, MobSpawningContext> zombiePicker) {}
/*public class MobSpawningConfig {
	
	private final int minDistance;
	private final int spawnAmount;
	private final int maxEntitiesPerChunk;
	private final ConditionalPicker<Zombies, MobSpawningContext> zombiePicker;
	
	public MobSpawningConfig(int minDistance, int spawnAmount, int maxEntitiesPerChunk, ConditionalPicker<Zombies, MobSpawningContext> zombiePicker) {
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
	
	public ConditionalPicker<Zombies, MobSpawningContext> zombiePicker() {
		return zombiePicker;
	}
	
}
*/