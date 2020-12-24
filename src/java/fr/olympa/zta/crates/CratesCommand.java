package fr.olympa.zta.crates;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
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
	
}
