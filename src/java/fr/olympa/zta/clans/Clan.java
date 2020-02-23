package fr.olympa.zta.clans;

import java.sql.ResultSet;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.objects.OlympaPlayerInformations;
import fr.olympa.api.utils.NMS;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.registry.Registrable;
import fr.olympa.zta.registry.ZTARegistry;

public class Clan implements Registrable {

	public static final String TABLE_NAME = "`zta_clans`";

	private final int id;
	private final String name;
	public Map<Long, Entry<OlympaPlayerInformations, OlympaPlayerZTA>> members = new HashMap<>(8);
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
		joinPacket(p.getPlayer());
		broadcast("Le joueur " + p.getName() + " rejoint le clan.");
		return true;
	}

	public void removePlayer(OlympaPlayerInformations pinfo) {
		broadcast("Le joueur " + pinfo.getName() + " a quitté le clan.");
		Entry<OlympaPlayerInformations, OlympaPlayerZTA> member = members.remove(pinfo.getID());
		// packets pour mettre le joueur partant en rouge pour les restants
		Player[] players = getPlayersArray();
		List<String> leaver = Arrays.asList(pinfo.getName());
		NMS.sendPacket(NMS.removePlayersFromTeam(ClansManager.clan, leaver), players);
		NMS.sendPacket(NMS.addPlayersToTeam(ClansManager.enemies, leaver), players);
		if (member.getValue() == null) {
			OlympaPlayerZTA.removePlayerClan(pinfo);
			return;
		}
		// packets pour mettre les autres joueurs en rouge pour le partant
		Player player = member.getValue().getPlayer();
		List<String> names = members.values().stream().map(x -> x.getKey().getName()).collect(Collectors.toList());
		NMS.sendPacket(NMS.removePlayersFromTeam(ClansManager.clan, names), player);
		NMS.sendPacket(NMS.addPlayersToTeam(ClansManager.enemies, names), player);

		member.getValue().setClan(null);
	}

	public void memberJoin(OlympaPlayerZTA p) {
		members.get(p.getId()).setValue(p);
		joinPacket(p.getPlayer());
	}

	public void memberLeave(OlympaPlayerZTA p) {
		members.get(p.getId()).setValue(null);
	}

	public void joinPacket(Player p) {
		List<String> names = members.values().stream().map(x -> x.getKey().getName()).collect(Collectors.toList());
		NMS.sendPacket(NMS.removePlayersFromTeam(ClansManager.enemies, names), p);
		NMS.sendPacket(NMS.addPlayersToTeam(ClansManager.clan, names), p);
	}

	public void setChief(OlympaPlayerInformations p) {
		chief = p.getID();
		broadcast("Le joueur " + p.getName() + " est désormais le chef du clan.");
	}

	public void disband() {
		broadcast("§lLe clan a été dissous. Ceci est le dernier message que vous recevrez.");
		ClansManager.removeClan(this);
	}

	public OlympaPlayerInformations getChief() {
		return members.get(chief).getKey();
	}

	public int getMembersAmount() {
		return members.size();
	}

	/*public OlympaPlayerInformations getMember(long id) {
		if (id >= members.size()) return null;
		return members.get(id).getKey();
	}*/

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

	public void broadcast(String message) {
		String finalMessage = SpigotUtils.color(Prefix.DEFAULT + "§6" + name + " §e: " + message + " Terminé.");
		executeAllPlayers(p -> p.sendMessage(finalMessage));
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

}
