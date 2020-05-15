package fr.olympa.zta.clans;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

import fr.olympa.api.clans.ClansManager;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;

public class ClansManagerZTA extends ClansManager<ClanZTA> {

	public ClansManagerZTA() throws SQLException, ReflectiveOperationException {
		super(OlympaZTA.getInstance(), "zta_clans", Collections.EMPTY_LIST, 5);

		new ZTAClansCommand<>(this, "clan", "Permet de gérer les clans.", ZTAPermissions.CLANS_PLAYERS_COMMAND, "clans").register();
	}

	@Override
	protected ClanZTA provideClan(int id, String name, long chief, int maxSize, double money, long created, ResultSet resultSet) {
		return new ClanZTA(this, id, name, chief, maxSize, money, created);
	}

	@Override
	protected ClanZTA createClan(int id, String name, long chief, int maxSize) {
		return new ClanZTA(this, id, name, chief, maxSize);
	}

}
