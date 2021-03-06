package fr.olympa.zta.loot.chests;

import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.spigot.command.ComplexCommand;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import fr.olympa.zta.loot.chests.type.LootChestType;

public class LootChestCommand extends ComplexCommand {
	
	private LootChestsManager manager;

	public LootChestCommand(LootChestsManager manager) {
		super(OlympaZTA.getInstance(), "lootchest", "Permet de configurer des coffres de loot.", ZTAPermissions.LOOT_CHEST_COMMAND);
		this.manager = manager;
		super.addArgumentParser("CHESTTYPE", LootChestType.class);
	}
	
	@Cmd (player = true)
	public void allowTemp(CommandContext cmd) {
		Chest chestBlock = getTargetChest(getPlayer());
		if (chestBlock == null) return;
		
		manager.tmpAllowed.add(chestBlock.getLocation());
		sendSuccess("Le coffre est accessible jusqu'au prochain redémarrage.");
		
		LootChest chest = manager.getLootChest(chestBlock);
		if (chest != null) {
			manager.chests.remove(chest.getID());
			sendInfo("Ce coffre était un coffre de loot, il est temporairement désactivé.");
		}
	}
	
	@Cmd (player = true, args = "CHESTTYPE", syntax = "[type de coffre]")
	public void create(CommandContext cmd) {
		Chest chestBlock = getTargetChest(getPlayer());
		if (chestBlock == null) return;
		
		try {
			LootChestType type = cmd.getArgument(0, manager.pickRandomChestType(chestBlock.getLocation()));
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
		if (cmd.getArgumentsLength() == 0 || !cmd.<String>getArgument(0).equals("confirm")) {
			sendError("ATTENTION - cette commande est irréversible - vous devez exécuter /lootchest clearAllChests confirm.");
			return;
		}
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

	@Cmd (min = 4, args = { "INTEGER", "INTEGER", "INTEGER", "INTEGER", "INTEGER" }, syntax = "<xMin> <zMin> <xMax> <zMax> [init %%]")
	public void globalScan(CommandContext cmd) {
		new Scan().start(sender, cmd.getArgument(0), cmd.getArgument(1), cmd.getArgument(2), cmd.getArgument(3), cmd.getArgument(4, 0));
	}

	@Cmd (player = true)
	public void randomize(CommandContext cmd) {
		LootChest chest = getTargetLootChest(getPlayer());
		if (chest == null) return;
		LootChestType type = manager.pickRandomChestType(chest.getLocation());
		chest.setLootType(type, true);
		sendSuccess("Le coffre est devenu un coffre %s.", type.getName());
	}
	
	@Cmd
	public void randomizeAll(CommandContext cmd) {
		for (LootChest lootChest : manager.chests.values()) {
			lootChest.setLootType(manager.pickRandomChestType(lootChest.getLocation()), true);
		}
		sendSuccess("Les %d coffres de loot ont été randomisé.", manager.chests.size());
	}

	@Cmd
	public void validateAll(CommandContext cmd) {
		sendInfo("Début de l'opération...");
		int missing = 0;
		int emptied = 0;
		int removed = 0;
		for (LootChest lootChest : new ArrayList<>(manager.chests.values())) {
			Block block = lootChest.getLocation().getBlock();
			if (block.getType() == Material.CHEST) {
				Chest chest = (Chest) block.getState();
				boolean update = !chest.getInventory().isEmpty();
				if (update) {
					chest.getInventory().clear();
					emptied++;
				}
				if (manager.getLootChest(chest) == null) {
					lootChest.register(chest);
					missing++;
				}else if (update) chest.update();
			}else {
				try {
					manager.removeLootChest(lootChest.getID());
					removed++;
				}catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		sendSuccess("%d coffres corrigés, %d coffres vidés, %d coffres supprimés.", missing, emptied, removed);
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
