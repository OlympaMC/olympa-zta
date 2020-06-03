package fr.olympa.zta.lootchests;

import java.sql.SQLException;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.region.Region;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.lootchests.type.LootChestCreator;
import fr.olympa.zta.mobs.MobSpawning.SpawnType;

public class Scan {

	private final LootChestsManager manager = OlympaZTA.getInstance().lootChestsManager;

	private int processed = 0;
	private int chestsCreated = 0;
	private int chestsAlreadyPresent = 0;
	private BukkitTask messages = null;

	public void start(CommandSender sender, SpawnType spawn) {
		Random random = new Random();

		Prefix.INFO.sendMessage(sender, "Démarrage de l'opération...");

		new Thread(() -> {
			for (Region region : spawn.getRegions()) {
				Location min = region.getMin();
				Location max = region.getMax();
				for (int x = min.getBlockX(); x < max.getBlockX(); x++) {
					for (int z = min.getBlockZ(); z < max.getBlockZ(); z++) {
						if (!region.isIn(min.getWorld(), x, min.getBlockY(), z)) continue;
						for (int y = 1; y < 150; y++) {
							Block block = min.getWorld().getBlockAt(x, y, z);
							if (block.getType() == Material.CHEST) {
								Chunk chunk = block.getChunk();
								if (!chunk.isForceLoaded()) {
									Bukkit.getScheduler().runTask(OlympaZTA.getInstance(), () -> chunk.setForceLoaded(true));
									Bukkit.getScheduler().runTaskLater(OlympaZTA.getInstance(), () -> chunk.setForceLoaded(false), 6000);
								}
								Bukkit.getScheduler().runTask(OlympaZTA.getInstance(), () -> {
									Chest chestBlock = (Chest) block.getState();
									if (manager.getLootChest(chestBlock) == null) {
										LootChestCreator creator = spawn.getLootChests().pick(random).get(0);
										try {
											LootChest chest = manager.createLootChest(chestBlock.getLocation(), creator.getType());
											chest.register(chestBlock);
											chestsCreated++;
										}catch (SQLException e) {
											e.printStackTrace();
										}
									}else chestsAlreadyPresent++;
								});
							}
							processed++;
						}
					}
				}
			}
			Prefix.DEFAULT_GOOD.sendMessage(sender, "Scan %s terminé ! %d blocs traités, %d coffres créés, %d coffres déjà présents.", spawn.name, processed, chestsCreated, chestsAlreadyPresent);
			messages.cancel();
			messages = null;
		}).start();
		messages = Bukkit.getScheduler().runTaskTimerAsynchronously(OlympaZTA.getInstance(), () -> {
			Prefix.INFO.sendMessage(sender, "Scan %s en cours... %d blocs traités, %d coffres créés, %d coffres déjà présents.", spawn.name, processed, chestsCreated, chestsAlreadyPresent);
		}, 100, 600);
	}

}
