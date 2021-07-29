package fr.olympa.zta.loot.crates;

import java.util.stream.Collectors;

import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.spigot.command.ComplexCommand;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;

public class CratesCommand extends ComplexCommand {
	
	private CratesManager crates;
	
	public CratesCommand(CratesManager crates) {
		super(OlympaZTA.getInstance(), "crates", "Gérer les caisses.", ZTAPermissions.CRATES_COMMAND);
		this.crates = crates;
		addArgumentParser("CRATE_TYPE", CrateType.class);
	}
	
	@Cmd (player = true, min = 1, args = "CRATE_TYPE", syntax = "<type de caisse>")
	public void spawn(CommandContext cmd) {
		crates.spawnCrate(player.getLocation(), cmd.getArgument(0));
		sendSuccess("Une caisse a été droppée.");
	}
	
	@Cmd
	public void list(CommandContext cmd) {
		sendSuccess("Liste des caisses sur le monde:\n%s", crates.getRunning()
				.stream()
				.map(x -> "§7➤ §d" + SpigotUtils.convertLocationToHumanString(x.getLocation()) + "§5, de type §d" + x.getType().name() + "§5, a atterri: §d" + x.hasLanded() + "§5, vide: §d" + (ItemUtils.getInventoryContentsLength(x.getInventory()) == 0))
				.collect(Collectors.joining("\n")));
	}
	
}
