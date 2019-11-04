package fr.olympa.zta.lootchests;

import org.bukkit.Material;
import org.bukkit.block.Block;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import fr.olympa.zta.registry.ZTARegistry;

public class ChestCommand extends ComplexCommand {
	
	public ChestCommand(){
		super(null, OlympaZTA.getInstance(), "lootchest", "configuration des coffres de loot", ZTAPermissions.LOOT_CHEST_COMMAND);
	}
	
	@Cmd (player = true, args = "civil|military|contraband", min = 1)
	public void create(CommandContext cmd) {
		Block targetBlock = cmd.player.getTargetBlockExact(2);
		if (targetBlock == null || targetBlock.getType() != Material.CHEST) {
			sendError("Vous devez regarder un coffre.");
			return;
		}

		LootChest chest = (LootChest) ZTARegistry.getObject(targetBlock);
		if (chest == null) {
			chest = new LootChest();
			ZTARegistry.registerObject(targetBlock, chest);
			sendSuccess("Le coffre de loot a été créé ! ID: " + chest.getID());
		}

		switch (((String) cmd.args[0]).toLowerCase()) {
		case "civil":
		case "civile":
			chest.type = LootChestType.CIVIL;
			break;
		case "military":
		case "militaire":
			chest.type = LootChestType.MILITARY;
			break;
		case "contraband":
		case "contreband":
		case "contrebande":
			chest.type = LootChestType.CONTRABAND;
			break;
		default:
			sendError("Le type de coffre de loot spécifié n'existe pas.");
			return;
		}
		sendSuccess("Ce coffre est désormais un coffre " + chest.type.getName());
	}
	
}
