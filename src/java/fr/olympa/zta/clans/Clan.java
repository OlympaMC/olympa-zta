package fr.olympa.zta.clans;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.objects.OlympaPlayerInformations;
import fr.olympa.api.scoreboard.DynamicLine;
import fr.olympa.api.scoreboard.FixedLine;
import fr.olympa.api.scoreboard.Scoreboard;
import fr.olympa.api.sql.OlympaStatement;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.NMS;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.registry.Registrable;
import fr.olympa.zta.registry.ZTARegistry;

public class Clan implements Registrable {

	public static final String TABLE_NAME = "`zta_clans`";

	private final int id;
	private final String name;
	private Map<Long, Entry<OlympaPlayerInformations, OlympaPlayerZTA>> members = new HashMap<>(8);
	private long chief;
	private int maxSize = 5;

	public Clan(String name) {
		this(name, ZTARegistry.generateID());
	}

	public Clan(String name, int id) {
		this.name = name;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public int getID() {
		return id;
	}

	public boolean addPlayer(OlympaPlayerZTA p) {
		if (members.size() >= getMaxSize()) return false;
		p.setClan(this);
		// packets pour mettre le nouveau joueur en vert pour les anciens
		Player[] players = getPlayersArray();
		List<String> joiner = Arrays.asList(p.getName());
		NMS.sendPacket(NMS.removePlayersFromTeam(ClansManager.enemies, joiner), players);
		NMS.sendPacket(NMS.addPlayersToTeam(ClansManager.clan, joiner), players);
		// packets pour mettre les autres joueurs en vert pour le nouveau
		members.put(p.getId(), new AbstractMap.SimpleEntry<>(p.getInformation(), p));
		memberJoin(p);
		broadcast("Le joueur " + p.getName() + " rejoint le clan.");
		return true;
	}

	public void removePlayer(OlympaPlayerInformations pinfo, boolean message) {
		if (message) broadcast("Le joueur " + pinfo.getName() + " a quitté le clan.");
		Entry<OlympaPlayerInformations, OlympaPlayerZTA> member = members.remove(pinfo.getID());
		// packets pour mettre le joueur partant en rouge pour les restants
		Player[] players = getPlayersArray();
		List<String> leaver = Arrays.asList(pinfo.getName());
		NMS.sendPacket(NMS.removePlayersFromTeam(ClansManager.clan, leaver), players);
		NMS.sendPacket(NMS.addPlayersToTeam(ClansManager.enemies, leaver), players);

		OlympaPlayerZTA oplayer = member.getValue();
		if (oplayer == null) { // joueur offline
			OlympaPlayerZTA.removePlayerClan(pinfo);
			return;
		}
		// packets pour mettre les autres joueurs en rouge pour le partant
		Player player = oplayer.getPlayer();
		List<String> names = members.values().stream().map(x -> x.getKey().getName()).collect(Collectors.toList());
		NMS.sendPacket(NMS.removePlayersFromTeam(ClansManager.clan, names), player);
		NMS.sendPacket(NMS.addPlayersToTeam(ClansManager.enemies, names), player);

		oplayer.setClan(null);

		OlympaZTA.getInstance().scoreboards.removePlayerScoreboard(oplayer);
		OlympaZTA.getInstance().scoreboards.create(oplayer);
	}

	private static FixedLine header = new FixedLine("§e§oMon clan:");
	private static DynamicLine<OlympaPlayerZTA> players = new DynamicLine<>((x) -> {
		Clan clan = x.getClan();
		Player p = x.getPlayer();
		StringJoiner joiner = new StringJoiner("\n");
		boolean inHub = OlympaZTA.getInstance().hub.region.isIn(p);
		for (Entry<OlympaPlayerInformations, OlympaPlayerZTA> member : clan.getMembers()) {
			String memberName = member.getKey().getName();
			if (member.getValue() == null) {
				joiner.add("§c○ " + memberName);
			}else if (member.getValue() == x) {
				joiner.add("§6● §l" + memberName);
			}else {
				Location loc = member.getValue().getPlayer().getLocation();
				joiner.add("§e● " + memberName + " §l" + (inHub != OlympaZTA.getInstance().hub.region.isIn(loc) ? 'x' : SpigotUtils.getDirectionToLocation(p, loc)));
			}
		}
		return joiner.toString();
	}, 1, 0);

	public void memberJoin(OlympaPlayerZTA member) {
		members.get(member.getId()).setValue(member);

		Player p = member.getPlayer();
		List<String> names = members.values().stream().map(x -> x.getKey().getName()).collect(Collectors.toList());
		NMS.sendPacket(NMS.removePlayersFromTeam(ClansManager.enemies, names), p);
		NMS.sendPacket(NMS.addPlayersToTeam(ClansManager.clan, names), p);

		Scoreboard scoreboard = OlympaZTA.getInstance().scoreboards.getPlayerScoreboard(member);
		scoreboard.addLine(FixedLine.EMPTY_LINE);
		scoreboard.addLine(header);
		scoreboard.addLine(players);
	}

	public void memberLeave(OlympaPlayerZTA p) {
		members.get(p.getId()).setValue(null);
	}

	public void setChief(OlympaPlayerInformations p) {
		chief = p.getID();
		broadcast("Le joueur " + p.getName() + " est désormais le chef du clan.");
	}

	public void disband() {
		broadcast("§lLe clan a été dissous. Ceci est le dernier message que vous recevrez.");
		for (Entry<OlympaPlayerInformations, OlympaPlayerZTA> member : members.values()) {
			removePlayer(member.getKey(), false);
		}
		ZTARegistry.removeObject(this);
	}

	public OlympaPlayerInformations getChief() {
		return members.get(chief).getKey();
	}

	public int getMembersAmount() {
		return members.size();
	}

	public int getMaxSize() {
		return maxSize;
	}

	public boolean contains(OlympaPlayer p) {
		return members.containsKey(p.getId());
	}

	public void executeAllPlayers(Consumer<Player> consumer) {
		for (Entry<OlympaPlayerInformations, OlympaPlayerZTA> member : members.values()) {
			if (member.getValue() != null) {
				consumer.accept(member.getValue().getPlayer());
			}
		}
	}

	public Player[] getPlayersArray() {
		Player[] playersArray = new Player[members.size()];
		int i = 0;
		for (Entry<OlympaPlayerInformations, OlympaPlayerZTA> member : members.values()) {
			if (member.getValue() != null) {
				playersArray[i] = member.getValue().getPlayer();
				i++;
			}
		}
		return playersArray;
	}

	public Collection<Entry<OlympaPlayerInformations, OlympaPlayerZTA>> getMembers() {
		return members.values();
	}

	public void broadcast(String message) {
		String finalMessage = ColorUtils.color(Prefix.DEFAULT + "§6" + name + " §e: " + message + " Terminé.");
		executeAllPlayers(p -> p.sendMessage(finalMessage));
	}

	private static OlympaStatement createStatement = new OlympaStatement("INSERT INTO " + TABLE_NAME + " (`id`, `name`, `chief`, `max_size`) VALUES (?, ?, ?, ?)");
	private static OlympaStatement updateStatement = new OlympaStatement("UPDATE " + TABLE_NAME + " SET "
			+ "`name` = ?, "
			+ "`chief` = ?, "
			+ "`max_size` = ? "
			+ "WHERE (`id` = ?)");
	private static OlympaStatement testStatement = new OlympaStatement("SELECT 1 FROM " + TABLE_NAME + " WHERE `name` = ?");

	public void createDatas() throws SQLException {
		PreparedStatement statement = createStatement.getStatement();
		statement.setInt(1, id);
		statement.setString(2, name);
		statement.setLong(3, chief);
		statement.setInt(4, maxSize);
		statement.executeUpdate();
	}

	public void updateDatas() throws SQLException {
		PreparedStatement statement = updateStatement.getStatement();
		statement.setString(1, name);
		statement.setLong(2, chief);
		statement.setInt(3, maxSize);
		statement.setInt(4, id);
		statement.executeUpdate();
	}

	public static String CREATE_TABLE_STATEMENT = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
			"  `id` INT NOT NULL," +
			"  `name` VARCHAR(45) NOT NULL," +
			"  `chief` BIGINT(20) NOT NULL," +
			"  `max_size` TINYINT(1) NOT NULL DEFAULT 5," +
			"  PRIMARY KEY (`id`))";

	public static Clan deserializeClan(ResultSet set, int id, Class<?> clazz) throws Exception {
		Clan clan = new Clan(set.getString("name"), set.getInt("id"));
		clan.maxSize = set.getInt("max_size");
		clan.chief = set.getInt("chief");
		for (OlympaPlayerInformations pinfo : OlympaPlayerZTA.getPlayersByClan(clan)){
			clan.members.put(pinfo.getID(), new AbstractMap.SimpleEntry<>(pinfo, null));
		}
		return clan;
	}

	public static boolean exists(String clanName) {
		try {
			PreparedStatement statement = testStatement.getStatement();
			statement.setString(1, clanName);
			return statement.executeQuery().next();
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}

}
