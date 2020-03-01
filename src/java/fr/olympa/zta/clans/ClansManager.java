package fr.olympa.zta.clans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import fr.olympa.api.utils.NMS;
import fr.olympa.zta.OlympaZTA;
import net.minecraft.server.v1_15_R1.ScoreboardTeam;

public class ClansManager {

	private static Map<Player, List<Clan>> invitations = new HashMap<>();

	public static Team enemiesBukkit;
	public static ScoreboardTeam enemies;
	public static ScoreboardTeam clan;
	public static ScoreboardTeam allies;

	public static void initialize() {
		Scoreboard sc = Bukkit.getScoreboardManager().getMainScoreboard();
		try {
			enemies = createOrGetTeam(sc, "enemies", ChatColor.RED);
			clan = createOrGetTeam(sc, "clan", ChatColor.GREEN);
			allies = createOrGetTeam(sc, "allies", ChatColor.AQUA);
		}catch (ReflectiveOperationException ex) {
			ex.printStackTrace();
			OlympaZTA.getInstance().sendMessage("§cErreur lors de la création des teams.");
			return;
		}
		enemiesBukkit = sc.getTeam("enemies");
	}

	private static ScoreboardTeam createOrGetTeam(Scoreboard sc, String name, ChatColor color) throws ReflectiveOperationException {
		Team team = sc.getTeam(name);
		if (team == null) {
			team = sc.registerNewTeam(name);
			team.setColor(color);
		}
		return NMS.getNMSTeam(team);
	}

	public static void addInvitation(Player p, Clan clan) {
		List<Clan> localInvites = invitations.get(p);
		if (localInvites == null) {
			localInvites = new ArrayList<>();
			invitations.put(p, localInvites);
		}
		localInvites.add(clan);
	}

	public static List<Clan> getPlayerInvitations(Player p) {
		List<Clan> localInvites = invitations.get(p);
		return localInvites == null ? Collections.EMPTY_LIST : localInvites;
	}

	public static void clearPlayerInvitations(Player p) {
		invitations.remove(p);
	}

}
