package fr.olympa.zta.lootchests;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import fr.olympa.zta.registry.ZTARegistry;

public class ChestCommand extends ComplexCommand {
	
	public ChestCommand(){
		super(null, OlympaZTA.getInstance(), "lootchest", "configuration des coffres de loot", ZTAPermissions.LOOT_CHEST_COMMAND);
	}
	
	@Cmd (player = true, args = "civil|military|contraband", min = 1)
	public void create(CommandContext cmd) {
		Chest chestBlock = getTargetChest(cmd.player);
		if (chestBlock == null) return;
		
		LootChest chest = LootChest.getLootChest(chestBlock);
		if (chest == null) {
			chest = new LootChest(chestBlock.getLocation());
			chestBlock.getInventory().setItem(0, ItemUtils.item(Material.DIRT, String.valueOf(ZTARegistry.registerObject(chest))));
			sendSuccess("Le coffre de loot a été créé ! ID: " + chest.getID());
		}

		LootChestType type = null;
		switch (((String) cmd.args[0]).toLowerCase()) {
		case "civil":
		case "civile":
			type = LootChestType.CIVIL;
			break;
		case "military":
		case "militaire":
			type = LootChestType.MILITARY;
			break;
		case "contraband":
		case "contreband":
		case "contrebande":
			type = LootChestType.CONTRABAND;
			break;
		default:
			sendError("Le type de coffre de loot spécifié n'existe pas.");
			return;
		}
		chest.setLootType(type);
		sendSuccess("Ce coffre est désormais un coffre " + type.getName());
	}
	
	@Cmd (player = true)
	public void resetTimer(CommandContext cmd) {
		LootChest chest = getTargetLootChest(cmd.player);
		if (chest == null) return;
		
		chest.resetTimer();
		sendSuccess("Le compte à rebours de ce coffre a été réinitialisé.");
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
