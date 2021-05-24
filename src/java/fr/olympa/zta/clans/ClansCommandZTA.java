package fr.olympa.zta.clans;

import fr.olympa.api.clans.ClansCommand;
import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.zta.ZTAPermissions;
import fr.olympa.zta.clans.plots.ClanPlayerDataZTA;

public class ClansCommandZTA extends ClansCommand<ClanZTA, ClanPlayerDataZTA> {
	
	public ClansCommandZTA(ClansManagerZTA manager) {
		super(manager, "Permet de gérer les clans.", ZTAPermissions.CLANS_PLAYERS_COMMAND, ZTAPermissions.CLANS_MANAGE_COMMAND, "clans");
	}
	
	@Cmd (player = true, hide = true)
	public void dismissExpirationMessage(CommandContext cmd) {
		ClanZTA clan = getPlayerClan(true);
		if (clan == null) return;
		if (clan.resetExpirationTime()) {
			sendSuccess("Vous ne recevrez plus le message d'expiration de la parcelle.");
		}else {
			sendError("Il n'y a pas de parcelle expirée.");
		}
	}
	
	@Cmd (player = true)
	public void refreshSize(CommandContext cmd) {
		ClanZTA clan = getPlayerClan(true);
		if (clan == null) return;
		int maxSize = manager.getMaxSize(getOlympaPlayer());
		if (maxSize > clan.getMaxSize()) {
			sendSuccess("Modification de la taille du clan...");
			clan.setMaxSize(maxSize);
		}else {
			sendError("La taille du clan ne peut être modifiée.");
		}
	}
	
}
