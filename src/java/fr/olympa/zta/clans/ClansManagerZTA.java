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
import fr.olympa.zta.clans.plots.ClanPlayerDataZTA;

public class ClansManagerZTA extends ClansManager<ClanZTA, ClanPlayerDataZTA> {

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
	public ClanManagementGUI<ClanZTA, ClanPlayerDataZTA> provideManagementGUI(ClanPlayerInterface<ClanZTA, ClanPlayerDataZTA> player) {
		return new ClanZTAManagementGUI(player, this);
	}

	@Override
	protected ClanPlayerDataZTA createClanData(OlympaPlayerInformations informations) {
		return new ClanPlayerDataZTA(informations);
	}
	
	@Override
	protected ClanPlayerDataZTA provideClanData(OlympaPlayerInformations informations, ResultSet resultSet) throws SQLException {
		return new ClanPlayerDataZTA(informations);
	}
	
}
