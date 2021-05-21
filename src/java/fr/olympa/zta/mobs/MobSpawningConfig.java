package fr.olympa.zta.mobs;

public class MobSpawningConfig {
	
	private final int minDistance;
	private final int spawnAmount;
	private final int maxEntitiesPerChunk;
	private final double explosiveProb;
	
	public MobSpawningConfig(int minDistance, int spawnAmount, int maxEntitiesPerChunk, double explosiveProb) {
		this.minDistance = minDistance;
		this.spawnAmount = spawnAmount;
		this.maxEntitiesPerChunk = maxEntitiesPerChunk;
		this.explosiveProb = explosiveProb;
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
	
	public double explosiveProb() {
		return explosiveProb;
	}
	
}
