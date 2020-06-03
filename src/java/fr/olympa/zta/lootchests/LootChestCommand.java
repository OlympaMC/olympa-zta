package fr.olympa.zta.lootchests;

import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import fr.olympa.zta.lootchests.type.LootChestType;
import fr.olympa.zta.mobs.MobSpawning.SpawnType;

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
				chest.register(chestBlock);
				sendSuccess("Le coffre de loot a été créé ! ID: " + chest.getID() + ", type: " + type.getName());
			}else {
				chest.setLootType(type);
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

	@Cmd (args = "HARD|MEDIUM|EASY|SAFE", min = 1)
	public void scan(CommandContext cmd) {
		try {
			SpawnType spawn = SpawnType.valueOf(cmd.<String>getArgument(0).toUpperCase());
			new Scan().start(sender, spawn);
		}catch (IllegalArgumentException ex) {
			sendError("Il n'y a pas de zone avec le nom %s.", cmd.getArgument(0));
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

		LootChest chest = manager.getLootChest(chestBlock);
		if (chest == null) {
			sendError("Ce coffre n'est pas un coffre de loot.");
			return null;
		}
		return chest;
	}

}
