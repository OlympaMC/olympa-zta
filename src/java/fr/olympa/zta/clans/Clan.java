package fr.olympa.zta.clans;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.utils.NMS;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.zta.registry.Registrable;
import fr.olympa.zta.registry.ZTARegistry;

public class Clan implements Registrable {

	private final int id;
	private final String name;
	private OlympaPlayer[] members = new OlympaPlayer[5];
	private byte chiefID = 0;

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

	public boolean addPlayer(OlympaPlayer p) {
		for (byte i = 0; i < members.length; i++) {
			if (members[i] != null) continue;
			// packets pour mettre le nouveau joueur en vert pour les anciens
			Player[] players = getPlayersArray();
			List<String> joiner = Arrays.asList(p.getName());
			NMS.sendPacket(NMS.removePlayersFromTeam(ClansManager.enemies, joiner), players);
			NMS.sendPacket(NMS.addPlayersToTeam(ClansManager.clan, joiner), players);
			// packets pour mettre les autres joueurs en vert pour le nouveau
			List<String> names = Arrays.stream(members).filter(x -> x != null).map(x -> x.getName()).collect(Collectors.toList());
			NMS.sendPacket(NMS.removePlayersFromTeam(ClansManager.enemies, names), p.getPlayer());
			NMS.sendPacket(NMS.addPlayersToTeam(ClansManager.clan, names), p.getPlayer());
			members[i] = p;
			broadcast("Le joueur " + p.getName() + " rejoint le clan.");
			return true;
		}
		return false;
	}

	public void removePlayer(OlympaPlayer p) {
		for (byte i = 0; i < members.length; i++) {
			if (!p.equals(members[i])) continue;
			broadcast("Le joueur " + p.getName() + " a quitté le clan.");
			members[i] = null;
			// packets pour mettre le joueur partant en rouge pour les restants
			Player[] players = getPlayersArray();
			List<String> leaver = Arrays.asList(p.getName());
			NMS.sendPacket(NMS.removePlayersFromTeam(ClansManager.clan, leaver), players);
			NMS.sendPacket(NMS.addPlayersToTeam(ClansManager.enemies, leaver), players);
			// packets pour mettre les autres joueurs en rouge pour le partant
			List<String> names = Arrays.stream(members).filter(x -> x != null).map(x -> x.getName()).collect(Collectors.toList());
			NMS.sendPacket(NMS.removePlayersFromTeam(ClansManager.clan, names), p.getPlayer());
			NMS.sendPacket(NMS.addPlayersToTeam(ClansManager.enemies, names), p.getPlayer());
			break;
		}
	}

	public void setChief(OlympaPlayer p) {
		for (byte i = 0; i < members.length; i++) {
			if (!p.equals(members[i])) continue;
			chiefID = i;
			broadcast("Le joueur " + p.getName() + " est désormais le chef du clan.");
			break;
		}
	}

	public void disband() {
		broadcast("§lLe clan a été dissous. Ceci est le dernier message que vous recevrez.");
		ClansManager.removeClan(this);
	}

	public OlympaPlayer getChief() {
		return members[chiefID];
	}

	public byte getChiefID() {
		return chiefID;
	}

	public byte getMembersAmount() {
		byte i = 0;
		for (OlympaPlayer member : members) if (member != null) i++;
		return i;
	}

	public OlympaPlayer getMember(byte id) {
		return members[id];
	}

	public boolean contains(OlympaPlayer p) {
		for (OlympaPlayer member : members) {
			if (p.equals(member)) return true;
		}
		return false;
	}

	public void executeAllPlayers(Consumer<Player> consumer) {
		for (OlympaPlayer member : members) {
			if (member != null) {
				Player p = member.getPlayer();
				if (p != null) consumer.accept(p);
			}
		}
	}

	public Player[] getPlayersArray() {
		Player[] playersArray = new Player[5];
		for (int i = 0; i < members.length; i++) {
			OlympaPlayer member = members[i];
			if (member != null) {
				Player p = member.getPlayer();
				if (p != null) playersArray[i] = p;
			}
		}
		return playersArray;
	}

	public void broadcast(String message) {
		String finalMessage = SpigotUtils.color(Prefix.DEFAULT + "§6" + name + " §e: " + message + " Terminé.");
		executeAllPlayers(p -> p.sendMessage(finalMessage));
	}

}
