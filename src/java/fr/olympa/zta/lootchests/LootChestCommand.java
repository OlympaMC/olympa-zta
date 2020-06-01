package fr.olympa.zta.lootchests;

import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.region.Region;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import fr.olympa.zta.mobs.MobSpawning.SpawnType;
import fr.olympa.zta.registry.ZTARegistry;

public class LootChestCommand extends ComplexCommand {
	
	public LootChestCommand(){
		super(OlympaZTA.getInstance(), "lootchest", "configuration des coffres de loot", ZTAPermissions.LOOT_CHEST_COMMAND);
	}
	
	@Cmd (player = true, args = "civil|military|contraband", min = 1, syntax = "<type de coffre>")
	public void create(CommandContext cmd) {
		Chest chestBlock = getTargetChest(getPlayer());
		if (chestBlock == null) return;
		
		LootChestType type = LootChestType.chestTypes.get(cmd.<String>getArgument(0).toLowerCase());
		if (type == null) {
			sendError("Le type de coffre de loot spécifié n'existe pas.");
			return;
		}

		LootChest chest = LootChest.getLootChest(chestBlock);
		if (chest == null) {
			chest = new LootChest(chestBlock.getLocation(), ZTARegistry.generateID(), type);
			chest.register(chestBlock);
			sendSuccess("Le coffre de loot a été créé ! ID: " + chest.getID() + ", type: " + type.getName());
		}else {
			chest.setLootType(type);
			sendSuccess("Ce coffre est désormais un coffre " + type.getName());
		}
	}
	
	@Cmd (player = true)
	public void resetTimer(CommandContext cmd) {
		LootChest chest = getTargetLootChest(getPlayer());
		if (chest == null) return;
		
		chest.resetTimer();
		sendSuccess("Le compte à rebours de ce coffre a été réinitialisé.");
	}

	@Cmd (player = true)
	public void noTime(CommandContext cmd) {
		LootChest chest = getTargetLootChest(getPlayer());
		if (chest == null) return;

		chest.setTimer(0);
		sendSuccess("Le compte à rebours de ce coffre a été mis à 0.");
	}

	private int processed = -1;
	private int chestsCreated = 0;
	private int chestsAlreadyPresent = 0;

	@Cmd (args = "HARD|MEDIUM|EASY|SAFE", min = 1)
	public void scan(CommandContext cmd) {
		if (processed != -1) {
			sendError("Un scan est déjà en cours.");
			return;
		}
		try {
			SpawnType spawn = SpawnType.valueOf(cmd.<String>getArgument(0).toUpperCase());
			processed = 0;
			new BukkitRunnable() {
				@Override
				public void run() {
					for (Region region : spawn.getRegions()) {
						for (Iterator<Block> blockIterator = region.blockList(); blockIterator.hasNext();) {
							Block block = blockIterator.next();
							if (block.getType() == Material.CHEST) {
								Chest chestBlock = (Chest) block.getState();
								if (LootChest.getLootChest(chestBlock) == null) {
									/*LootChest chest = new LootChest(chestBlock.getLocation(), ZTARegistry.generateID(), spawn);
									chest.register(chestBlock);*/
									chestsCreated++;
								}else chestsAlreadyPresent++;
							}
							processed++;
						}
					}
					processed = -1;
					chestsCreated = 0;
					chestsAlreadyPresent = 0;
				}
			}.runTaskAsynchronously(OlympaZTA.getInstance());
		}catch (IllegalArgumentException ex) {
			sendError("Il n'y a pas de zone avec le nom %s.", cmd.getArgument(0));
		}
	}

	private Chest getTargetChest(Player p) {
		Block targetBlock = p.getTargetBlockExact(2);
		if (targetBlock == null || targetBlock.getType() != Material.CHEST) {
			sendError("Vous devez regarder un coffre.");
			return null;
		}
		return (Chest) targetBlock.getState();
	}

	private LootChest getTargetLootChest(Player p) {
		Chest chestBlock = getTargetChest(p);
		if (chestBlock == null) return null;

		LootChest chest = LootChest.getLootChest(chestBlock);
		if (chest == null) {
			sendError("Ce coffre n'est pas un coffre de loot.");
			return null;
		}
		return chest;
	}

}
