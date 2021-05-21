package fr.olympa.zta.mobs;

public record MobSpawningConfig(int minDistance, int spawnAmount, int maxEntitiesPerChunk, double explosiveProb) {}
