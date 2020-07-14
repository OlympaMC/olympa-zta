package fr.olympa.zta.clans;

import fr.olympa.api.clans.ClansCommand;
import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.zta.clans.plots.ClanPlayerDataZTA;

public class ClansCommandZTA extends ClansCommand<ClanZTA, ClanPlayerDataZTA> {
	
	public ClansCommandZTA(ClansManagerZTA manager, String description, OlympaPermission permission, String... aliases) {
		super(manager, description, permission, aliases);
	}
	
	@Cmd (player = true)
	public void dismissExpirationMessage(CommandContext cmd) {
		ClanZTA clan = getPlayerClan(true);
		if (clan == null) return;
		if (clan.resetExpirationTime()) {
			sendSuccess("Vous ne recevrez plus le message d'expiration de la parcelle.");
		}else {
			sendError("Il n'y a pas de parcelle expirée.");
		}
	}
	
}
