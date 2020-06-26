package fr.olympa.zta.lootchests;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.region.tracking.TrackedRegion;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import fr.olympa.zta.lootchests.type.LootChestType;
import fr.olympa.zta.mobs.MobSpawning.SpawnType.SpawningFlag;

public class LootChestCommand extends ComplexCommand {
	
	private LootChestsManager manager;

	public LootChestCommand(LootChestsManager manager) {
		super(OlympaZTA.getInstance(), "lootchest", "configuration des coffres de loot", ZTAPermissions.LOOT_CHEST_COMMAND);
		this.manager = manager;
	}
	
	@Cmd (player = true, args = "civil|military|contraband", min = 1, syntax = "<type de coffre>")
	public void create(CommandContext cmd) {
		Chest chestBlock = getTargetChest(getPlayer());
		if (chestBlock == null) return;
		
		try {
			LootChestType type = LootChestType.valueOf(cmd.<String>getArgument(0).toUpperCase());
			LootChest chest = manager.getLootChest(chestBlock);
			if (chest == null) {
				chest = OlympaZTA.getInstance().lootChestsManager.createLootChest(chestBlock.getLocation(), type);
				sendSuccess("Le coffre de loot a été créé ! ID: " + chest.getID() + ", type: " + type.getName());
			}else {
				chest.setLootType(type, true);
				sendSuccess("Ce coffre est désormais un coffre " + type.getName());
			}
		}catch (IllegalArgumentException ex) {
			sendError("Le type de coffre de loot spécifié n'existe pas.");
		}catch (SQLException ex) {
			ex.printStackTrace();
			sendError("Une erreur est survenue lors de la création du coffre.");
		}
	}
	
	@Cmd (player = true)
	public void fix(CommandContext cmd) {
		Chest chest = getTargetChest(player);
		if (chest == null) return;
		
		sendSuccess("Left chest %s", manager.getLeftChest(chest).getLocation().toString());
	}
	
	@Cmd (player = true)
	public void remove(CommandContext cmd) {
		LootChest chest = getTargetLootChest(getPlayer());
		if (chest == null) return;

		try {
			OlympaZTA.getInstance().lootChestsManager.removeLootChest(chest.getID());
			sendSuccess("Le coffre %d a été supprimé.", chest.getID());
		}catch (SQLException ex) {
			ex.printStackTrace();
			sendError("Une erreur est survenue lors de la suppression du coffre : §l%s", ex.getMessage());
		}
	}

	@Cmd
	public void clearAllChests(CommandContext cmd) {
		int removed = 0;
		int errors = 0;
		for (Integer chest : new ArrayList<>(OlympaZTA.getInstance().lootChestsManager.chests.keySet())) {
			try {
				OlympaZTA.getInstance().lootChestsManager.removeLootChest(chest);
				removed++;
			}catch (SQLException ex) {
				ex.printStackTrace();
				errors++;
			}
		}
		sendSuccess("%d coffres supprimés avec succès, %d erreurs.", removed, errors);
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

	@Cmd (min = 4, args = { "INTEGER", "INTEGER", "INTEGER", "INTEGER", "INTEGER" }, syntax = "<xMin> <zMin> <xMax> <zMax> [init %]")
	public void globalScan(CommandContext cmd) {
		try {
			new Scan().start(sender, cmd.getArgument(0), cmd.getArgument(1), cmd.getArgument(2), cmd.getArgument(3), cmd.getArgument(4, 0));
		}catch (IllegalArgumentException ex) {
			sendError("Il n'y a pas de zone avec le nom %s.", cmd.getArgument(0));
		}
	}

	@Cmd (args = "HARD|MEDIUM|EASY|SAFE")
	public void randomize(CommandContext cmd) {
		if (cmd.getArgumentsLength() == 0) {
			if (player == null) {
				sendIncorrectSyntax();
				return;
			}
			LootChest chest = getTargetLootChest(getPlayer());
			if (chest == null) return;
			LootChestType type = pickRandomChestType(chest.getLocation());
			chest.setLootType(type, true);
			sendSuccess("Le coffre est devenu un coffre %s.", type.getName());
		}else {
			for (LootChest lootChest : manager.chests.values()) {
				lootChest.setLootType(pickRandomChestType(lootChest.getLocation()), true);
			}
			sendSuccess("Les %d coffres de loot ont été randomisé.", manager.chests.size());
		}
	}

	@Cmd
	public void validateAll(CommandContext cmd) {
		sendInfo("Début de l'opération...");
		int missing = 0;
		int removed = 0;
		for (LootChest lootChest : new ArrayList<>(manager.chests.values())) {
			Block block = lootChest.getLocation().getBlock();
			if (block.getType() == Material.CHEST) {
				Chest chest = (Chest) block.getState();
				if (manager.getLootChest(chest) == null) {
					lootChest.register(chest);
					missing++;
				}
			}else {
				try {
					manager.removeLootChest(lootChest.getID());
					removed++;
				}catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		sendSuccess("%d coffres corrigés, %d coffres supprimés.", missing, removed);
	}

	private Random random = new Random();
	private LootChestType pickRandomChestType(Location location) {
		for (TrackedRegion region : OlympaCore.getInstance().getRegionManager().getApplicableRegions(location)) {
			SpawningFlag flag = region.getFlag(SpawningFlag.class);
			if (flag == null) continue;
			return flag.type.getLootChests().pick(random).get(0).getType();
		}
		return null;
	}

	private Chest getTargetChest(Player p) {
		Block targetBlock = p.getTargetBlockExact(3);
		if (targetBlock == null || targetBlock.getType() != Material.CHEST) {
			sendError("Vous devez regarder un coffre.");
			return null;
		}
		return (Chest) targetBlock.getState();
	}

	private LootChest getTargetLootChest(Player p) {
		Chest chestBlock = getTargetChest(p);
		if (chestBlock == null) return null;

		LootChest chest = manager.getLootChest(chestBlock);
		if (chest == null) {
			sendError("Ce coffre n'est pas un coffre de loot.");
			return null;
		}
		return chest;
	}

}
