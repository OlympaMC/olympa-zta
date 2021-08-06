package fr.olympa.zta.mobs;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaZTA;

public class MapScan { // last working scan : https://gitlab.com/olympa/olympazta/-/commit/9e31ec544f2363357fdb38d0fc5729e79b12bcbe

	private static final double DIVIDE = 5D;
	
	private Map<Integer, Thread> threads = new HashMap<>();

	private CommandSender sender;

	private int chunkProcessed;
	
	public void start(CommandSender sender, int minX, int minZ, int maxX, int maxZ, double forward) {
		this.sender = sender;
		
		Prefix.INFO.sendMessage(sender, "Démarrage du scan des blocs %d %d à %d %d, avec un pourcentage d'avancée initial de %f%%.", minX, minZ, maxX, maxZ, forward);
		
		MobsListener.removeEntities = true;
		
		World world = OlympaZTA.getInstance().mobSpawning.world;
		int minChunkX = minX >> 4;
		int minChunkZ = minZ >> 4;
		int maxChunkX = maxX >> 4;
		int maxChunkZ = maxZ >> 4;
		int xD = (int) Math.ceil((maxChunkX - minChunkX) / DIVIDE);
		int zD = (int) Math.ceil((maxChunkZ - minChunkZ) / DIVIDE);
		int xForward = (int) (xD * forward / 100D);
		int zForward = (int) (zD * forward / 100D);
		int id = 0;
		for (int x = minChunkX; x < maxChunkX; x += xD) {
			for (int z = minChunkZ; z < maxChunkZ; z += zD) {
				startThread(id++, world, x + xForward, z + zForward, x + xD, z + zD);
			}
		}
	}
	
	private void startThread(int id, World world, int minChunkX, int minChunkZ, int maxChunkX, int maxChunkZ) {
		Prefix.INFO.sendMessage(sender, "Démarrage du thread #%d pour le scan des chunks de %d %d à %d %d.", id, minChunkX, minChunkZ, maxChunkX, maxChunkZ);
		Thread thread = new Thread(() -> {
			for (int xChunk = minChunkX; xChunk < maxChunkX; xChunk++) {
				for (int zChunk = minChunkZ; zChunk < maxChunkZ; zChunk++) {
					Chunk chunk = world.getChunkAt(xChunk, zChunk);
					if (!chunk.isLoaded()) {
						chunk.load();
						chunk.unload();
					}
					chunkProcessed++;
				}
			}
			threads.remove(id);
			Prefix.INFO.sendMessage(sender, "Thread #%d terminé (restants : %d).", id, threads.size());
			if (threads.isEmpty()) MobsListener.removeEntities = false;
		}, "Scan #" + id);
		thread.start();
		threads.put(id, thread);
	}

}
