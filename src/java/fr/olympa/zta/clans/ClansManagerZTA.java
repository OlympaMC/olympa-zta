package fr.olympa.zta.clans;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.sql.SQLColumn;
import fr.olympa.api.spigot.clans.ClanPlayerInterface;
import fr.olympa.api.spigot.clans.ClansManager;
import fr.olympa.api.spigot.clans.gui.ClanManagementGUI;
import fr.olympa.api.spigot.customevents.ScoreboardCreateEvent;
import fr.olympa.api.spigot.lines.FixedLine;
import fr.olympa.api.spigot.lines.TimerLine;
import fr.olympa.api.spigot.scoreboard.sign.Scoreboard;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import fr.olympa.zta.clans.plots.ClanPlayerDataZTA;
import fr.olympa.zta.settings.ClanBoardSetting;

public class ClansManagerZTA extends ClansManager<ClanZTA, ClanPlayerDataZTA> {

	private static FixedLine<Scoreboard<OlympaPlayerZTA>> header = new FixedLine<>("§7Mon clan:");
	private static TimerLine<Scoreboard<OlympaPlayerZTA>> players = new TimerLine<>((x) -> {
		ClanZTA clan = x.getOlympaPlayer().getClan();
		Player p = (Player) x.getOlympaPlayer().getPlayer();
		List<String> players = new ArrayList<>(clan.getMembers().size());
		int first = 0;
		int offline = 0;
		boolean inHub = OlympaZTA.getInstance().hub.isInHub(p.getLocation());
		for (ClanPlayerDataZTA member : clan.getMembers()) {
			String memberName = member.getPlayerInformations().getName();
			if (!member.isConnected()) {
				if (players.size() < 5 && x.getOlympaPlayer().parameterClanBoard.get() == ClanBoardSetting.ONLINE_FIVE) players.add(offline, "§c○ " + memberName);
			}else if (member.getConnectedPlayer() == x.getOlympaPlayer()) {
				if (players.size() >= 5 && offline < players.size()) players.remove(players.size() - 1);
				players.add(0, "§6● §l" + memberName);
				first = 1;
				offline++;
			}else {
				Location loc = ((Entity) member.getConnectedPlayer().getPlayer()).getLocation();
				if (players.size() >= 5 && offline < players.size()) players.remove(players.size() - 1);
				char dir = inHub != OlympaZTA.getInstance().hub.isInHub(loc) ? 'x' : SpigotUtils.getDirectionToLocation(p, loc);
				String health = String.valueOf((int) ((Damageable) member.getConnectedPlayer().getPlayer()).getHealth()) + "❤";
				players.add(first, "§e● " + memberName + " §l" + dir + " §c(" + health + ")");
				offline++;
			}
		}
		return String.join("\n", players);
	}, OlympaZTA.getInstance(), 10);
	
	protected SQLColumn<ClanZTA> plotExpirationResetColumn;
	
	public ClansManagerZTA() throws SQLException, ReflectiveOperationException {
		super(OlympaZTA.getInstance(), OlympaZTA.getInstance().getServerNameID() + "_clans");

		new ClansCommandZTA(this).register();
	}

	@Override
	protected String getClansCommand() {
		return "clan";
	}
	
	@Override
	protected ClanZTA provideClan(int id, String name, String tag, OlympaPlayerInformations chief, int maxSize, double money, long created, ResultSet resultSet) throws SQLException {
		return new ClanZTA(this, id, name, tag, chief, maxSize, money, created, resultSet.getLong("plot_expiration_reset"));
	}

	@Override
	protected ClanZTA createClan(int id, String name, String tag, OlympaPlayerInformations chief, int maxSize) {
		return new ClanZTA(this, id, name, tag, chief, maxSize);
	}

	@Override
	public ClanManagementGUI<ClanZTA, ClanPlayerDataZTA> provideManagementGUI(ClanPlayerInterface<ClanZTA, ClanPlayerDataZTA> player) {
		return new ClanZTAManagementGUI(player, player.getClan(), this);
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
	public int getMaxSize(ClanPlayerInterface<ClanZTA, ClanPlayerDataZTA> p) {
		if (ZTAPermissions.GROUP_LEGENDE.hasPermission(p)) return 14;
		if (ZTAPermissions.GROUP_HEROS.hasPermission(p)) return 12;
		if (ZTAPermissions.GROUP_SAUVEUR.hasPermission(p)) return 11;
		if (ZTAPermissions.GROUP_RODEUR.hasPermission(p)) return 10;
		if (ZTAPermissions.GROUP_SURVIVANT.hasPermission(p)) return 8;
		return 7;
	}
	
	public int getMaxSize() {
		return 14;
	}
	
	@Override
	public List<SQLColumn<ClanZTA>> addDBClansCollums(List<SQLColumn<ClanZTA>> columns) {
		columns = super.addDBClansCollums(columns);
		columns.add(plotExpirationResetColumn = new SQLColumn<ClanZTA>("plot_expiration_reset", "BIGINT NOT NULL DEFAULT -1", Types.BIGINT).setUpdatable());
		return columns;
	}
	
	@EventHandler
	public void onScoreboardCreate(ScoreboardCreateEvent<OlympaPlayerZTA> e) {
		if (e.getOlympaPlayer().getClan() != null && e.getOlympaPlayer().parameterClanBoard.get() != ClanBoardSetting.NEVER) addLines(e.getScoreboard());
	}
	
	public void addLines(Scoreboard<OlympaPlayerZTA> scoreboard) {
		scoreboard.addLines(FixedLine.EMPTY_LINE);
		scoreboard.addLines(header);
		scoreboard.addLines(players);
	}
	
	public void updateBoardParameter(OlympaPlayerZTA player, ClanBoardSetting setting) {
		if (setting == ClanBoardSetting.NEVER || setting == ClanBoardSetting.ONLINE_FIVE) OlympaZTA.getInstance().scoreboards.refresh(player);
	}
	
}
