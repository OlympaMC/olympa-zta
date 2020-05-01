package fr.olympa.zta.plots.players;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;

public class PlotChunkGenerator extends ChunkGenerator {

	public static final int ROAD_WIDTH = 6;
	public static final int PLOT_CHUNK_SIZE = 3;
	public static final int WORLD_LEVEL = 10;
	public static final int PLOT_WIDTH = PLOT_CHUNK_SIZE * 16 - ROAD_WIDTH;

	private Material[] materials = { Material.GRASS_BLOCK, Material.PODZOL, Material.COARSE_DIRT };
	private float[] chances = { 0.55f, 0.25f, 0.2f };

	@Override
	public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
		ChunkData chunk = createChunkData(world);

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {

				//set biome plaine partout
				for (int y = 0; y <= 255; y++)
					biome.setBiome(x, y, z, Biome.PLAINS);

				//set couche basse
				chunk.setBlock(x, 0, z, Material.BEDROCK);

				//set couches hautes
				//si route, set stone
				if ((Math.floorMod(chunkX * 16 + x, PLOT_WIDTH + ROAD_WIDTH) >= PLOT_WIDTH &&
						Math.floorMod(chunkX * 16 + x, PLOT_WIDTH + ROAD_WIDTH) < PLOT_WIDTH + ROAD_WIDTH) ||
						(Math.floorMod(chunkZ * 16 + z, PLOT_WIDTH + ROAD_WIDTH) >= PLOT_WIDTH &&
						Math.floorMod(chunkZ * 16 + z, PLOT_WIDTH + ROAD_WIDTH) < PLOT_WIDTH + ROAD_WIDTH)) {

					//set stone pour les routes
					for (int y = 1; y <= WORLD_LEVEL; y++) {
						chunk.setBlock(x, y, z, random.nextDouble() < 0.5 ? Material.ANDESITE : Material.STONE);
					}
				}else { //si plot, set terre et herbe pour les plots
					for (int y = 1; y < WORLD_LEVEL; y++) {
						chunk.setBlock(x, y, z, Material.DIRT);
					}
					Material type = randomObject(random, materials, chances);
					chunk.setBlock(x, WORLD_LEVEL, z, type);
					if (type == Material.GRASS_BLOCK && random.nextDouble() < 0.4) chunk.setBlock(x, WORLD_LEVEL + 1, z, Material.GRASS);
				}

				//si bord de plot, set demies dalles

				if (((Math.floorMod(chunkX * 16 + x, PLOT_WIDTH + ROAD_WIDTH) == PLOT_WIDTH ||
						Math.floorMod(chunkX * 16 + x, PLOT_WIDTH + ROAD_WIDTH) == PLOT_WIDTH + ROAD_WIDTH - 1) &&
						!(Math.floorMod(chunkZ * 16 + z, PLOT_WIDTH + ROAD_WIDTH) > PLOT_WIDTH &&
								Math.floorMod(chunkZ * 16 + z, PLOT_WIDTH + ROAD_WIDTH) < PLOT_WIDTH
								+ ROAD_WIDTH))
						||
						((Math.floorMod(chunkZ * 16 + z, PLOT_WIDTH + ROAD_WIDTH) == PLOT_WIDTH ||
						Math.floorMod(chunkZ * 16 + z, PLOT_WIDTH + ROAD_WIDTH) == PLOT_WIDTH + ROAD_WIDTH - 1) &&
								!(Math.floorMod(chunkX * 16 + x, PLOT_WIDTH + ROAD_WIDTH) > PLOT_WIDTH &&
										Math.floorMod(chunkX * 16 + x, PLOT_WIDTH + ROAD_WIDTH) < PLOT_WIDTH
										+ ROAD_WIDTH))
						||
						(Math.floorMod(chunkX * 16 + x, PLOT_WIDTH + ROAD_WIDTH) == PLOT_WIDTH + ROAD_WIDTH - 1 &&
						Math.floorMod(chunkZ * 16 + z, PLOT_WIDTH + ROAD_WIDTH) == PLOT_WIDTH + ROAD_WIDTH - 1)) {

					chunk.setBlock(x, WORLD_LEVEL + 1, z, Material.SMOOTH_STONE_SLAB);
				}

			}
		}
		return chunk;
	}

	@Override
	public boolean isParallelCapable() {
		return true;
	}

	private <T> T randomObject(Random randomG, T[] objects, float[] chances) {
		float random = randomG.nextFloat();
		float acumulatedChance = 0f;

		for (int i = 0; i < chances.length; i++) {
			acumulatedChance += chances[i];
			if (acumulatedChance >= random) {
				return objects[i];
			}
		}
		return objects[objects.length - 1];
	}

}
