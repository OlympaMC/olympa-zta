package fr.olympa.zta.clans;

import java.sql.ResultSet;
import java.sql.SQLException;

import fr.olympa.api.clans.ClanPlayerInterface;
import fr.olympa.api.clans.ClansCommand;
import fr.olympa.api.clans.ClansManager;
import fr.olympa.api.clans.gui.ClanManagementGUI;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;

public class ClansManagerZTA extends ClansManager<ClanZTA> {

	public ClansManagerZTA() throws SQLException, ReflectiveOperationException {
		super(OlympaZTA.getInstance(), "zta_clans", 5);

		new ClansCommand<>(this, "clan", "Permet de gérer les clans.", ZTAPermissions.CLANS_PLAYERS_COMMAND, "clans").register();
	}

	@Override
	protected ClanZTA provideClan(int id, String name, OlympaPlayerInformations chief, int maxSize, double money, long created, ResultSet resultSet) {
		return new ClanZTA(this, id, name, chief, maxSize, money, created);
	}

	@Override
	protected ClanZTA createClan(int id, String name, OlympaPlayerInformations chief, int maxSize) {
		return new ClanZTA(this, id, name, chief, maxSize);
	}

	@Override
	public ClanManagementGUI<ClanZTA> provideManagementGUI(ClanPlayerInterface<ClanZTA> player) {
		return new ClanZTAManagementGUI(player, this);
	}

}
