package fr.olympa.zta.lootchests;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import fr.olympa.zta.registry.ZTARegistry;

public class ChestCommand extends ComplexCommand {
	
	public ChestCommand(){
		super(null, OlympaZTA.getInstance(), "chest", "configuration des coffres de loot", ZTAPermissions.LOOT_CHEST_COMMAND);
	}
	
	@Cmd (player = true)
	public void create(CommandContext cmd) {
		Block targetBlock = cmd.player.getTargetBlockExact(2);
		if (targetBlock == null || targetBlock.getType() != Material.CHEST) {
			sendError("Vous devez regarder un coffre.");
			return;
		}
		if (targetBlock.hasMetadata("lootchest")) {
			sendError("Ce coffre est déjà un coffre de loot. ID: " + targetBlock.getMetadata("lootchest").get(0).asInt());
			return;
		}
		LootChest chest = new LootChest();
		targetBlock.setMetadata("lootchest", new FixedMetadataValue(OlympaZTA.getInstance(), ZTARegistry.registerObject(chest)));
		sendSuccess("Le coffre de loot a été créé ! ID: " + chest.getID());
	}
	
}
