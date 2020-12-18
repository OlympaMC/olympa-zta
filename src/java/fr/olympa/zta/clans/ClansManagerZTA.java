package fr.olympa.zta.clans;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import fr.olympa.api.clans.ClanPlayerInterface;
import fr.olympa.api.clans.ClansManager;
import fr.olympa.api.clans.gui.ClanManagementGUI;
import fr.olympa.api.customevents.ScoreboardCreateEvent;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.lines.TimerLine;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.scoreboard.sign.Scoreboard;
import fr.olympa.api.sql.SQLColumn;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import fr.olympa.zta.clans.plots.ClanPlayerDataZTA;

public class ClansManagerZTA extends ClansManager<ClanZTA, ClanPlayerDataZTA> {

	private static FixedLine<Scoreboard<OlympaPlayerZTA>> header = new FixedLine<>("§7Mon clan:");
	private static TimerLine<Scoreboard<OlympaPlayerZTA>> players = new TimerLine<>((x) -> {
		ClanZTA clan = x.getOlympaPlayer().getClan();
		Player p = x.getOlympaPlayer().getPlayer();
		List<String> players = new ArrayList<>(5);
		int first = 0;
		int offline = 0;
		boolean inHub = OlympaZTA.getInstance().hub.isInHub(p.getLocation());
		for (ClanPlayerDataZTA member : clan.getMembers()) {
			String memberName = member.getPlayerInformations().getName();
			if (!member.isConnected()) {
				players.add(offline, "§c○ " + memberName);
			}else if (member.getConnectedPlayer() == x.getOlympaPlayer()) {
				players.add(0, "§6● §l" + memberName);
				first = 1;
				offline++;
			}else {
				Location loc = member.getConnectedPlayer().getPlayer().getLocation();
				players.add(first, "§e● " + memberName + " §l" + (inHub != OlympaZTA.getInstance().hub.isInHub(loc) ? 'x' : SpigotUtils.getDirectionToLocation(p, loc)));
				offline++;
			}
		}
		return String.join("\n", players);
	}, OlympaZTA.getInstance(), 10);
	
	protected SQLColumn<ClanZTA> plotExpirationResetColumn;
	
	public ClansManagerZTA() throws SQLException, ReflectiveOperationException {
		super(OlympaZTA.getInstance(), "zta_clans", 5);

		new ClansCommandZTA(this, "Permet de gérer les clans.", ZTAPermissions.CLANS_PLAYERS_COMMAND, "clans").register();
	}

	@Override
	protected String getClansCommand() {
		return "clan";
	}
	
	@Override
	protected ClanZTA provideClan(int id, String name, OlympaPlayerInformations chief, int maxSize, double money, long created, ResultSet resultSet) throws SQLException {
		return new ClanZTA(this, id, name, chief, maxSize, money, created, resultSet.getLong("plot_expiration_reset"));
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
	
	@Override
	public List<SQLColumn<ClanZTA>> addDBClansCollums(List<SQLColumn<ClanZTA>> columns) {
		columns = super.addDBClansCollums(columns);
		columns.add(plotExpirationResetColumn = new SQLColumn<ClanZTA>("plot_expiration_reset", "BIGINT NOT NULL DEFAULT -1", Types.BIGINT).setUpdatable());
		return columns;
	}
	
	@EventHandler
	public void onScoreboardCreate(ScoreboardCreateEvent<OlympaPlayerZTA> e) {
		if (e.getOlympaPlayer().getClan() != null) addLines(e.getScoreboard());
	}
	
	public void addLines(Scoreboard<OlympaPlayerZTA> scoreboard) {
		scoreboard.addLine(FixedLine.EMPTY_LINE);
		scoreboard.addLine(header);
		scoreboard.addLine(players);
	}
	
}
