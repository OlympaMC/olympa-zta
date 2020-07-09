package fr.olympa.zta.clans;

import java.util.StringJoiner;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.olympa.api.clans.Clan;
import fr.olympa.api.clans.ClanPlayerData;
import fr.olympa.api.clans.ClanPlayerInterface;
import fr.olympa.api.clans.ClansManager;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.lines.TimerLine;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.scoreboard.sign.Scoreboard;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.clans.plots.ClanPlot;

public class ClanZTA extends Clan<ClanZTA> {

	private static FixedLine<Scoreboard<OlympaPlayerZTA>> header = new FixedLine<>("§7Mon clan:");
	private static TimerLine<Scoreboard<OlympaPlayerZTA>> players = new TimerLine<>((x) -> {
		ClanZTA clan = x.getOlympaPlayer().getClan();
		Player p = x.getOlympaPlayer().getPlayer();
		StringJoiner joiner = new StringJoiner("\n");
		boolean inHub = OlympaZTA.getInstance().hub.isInHub(p.getLocation());
		for (ClanPlayerData<ClanZTA> member : clan.getMembers()) {
			String memberName = member.getPlayerInformations().getName();
			if (!member.isConnected()) {
				joiner.add("§c○ " + memberName);
			}else if (member.getConnectedPlayer() == x.getOlympaPlayer()) {
				joiner.add("§6● §l" + memberName);
			}else {
				Location loc = member.getConnectedPlayer().getPlayer().getLocation();
				joiner.add("§e● " + memberName + " §l" + (inHub != OlympaZTA.getInstance().hub.isInHub(loc) ? 'x' : SpigotUtils.getDirectionToLocation(p, loc)));
			}
		}
		return joiner.toString();
	}, OlympaZTA.getInstance(), 10);
	
	public ClanPlot cachedPlot;

	public ClanZTA(ClansManager<ClanZTA> manager, int id, String name, OlympaPlayerInformations chief, int maxSize, double money, long created) {
		super(manager, id, name, chief, maxSize, money, created);
	}

	public ClanZTA(ClansManager<ClanZTA> manager, int id, String name, OlympaPlayerInformations chief, int maxSize) {
		super(manager, id, name, chief, maxSize);
	}

	@Override
	protected void removedOnlinePlayer(ClanPlayerInterface<ClanZTA> oplayer) {
		super.removedOnlinePlayer(oplayer);
		
		OlympaZTA.getInstance().scoreboards.removePlayerScoreboard((OlympaPlayerZTA) oplayer);
		OlympaZTA.getInstance().scoreboards.create((OlympaPlayerZTA) oplayer);
	}

	@Override
	public void memberJoin(ClanPlayerInterface<ClanZTA> member) {
		super.memberJoin(member);

		Scoreboard<OlympaPlayerZTA> scoreboard = OlympaZTA.getInstance().scoreboards.getPlayerScoreboard((OlympaPlayerZTA) member);
		scoreboard.addLine(FixedLine.EMPTY_LINE);
		scoreboard.addLine(header);
		scoreboard.addLine(players);
	}

	@Override
	public void disband() {
		super.disband();
		
		if (cachedPlot != null) {
			ClanPlot plot = cachedPlot;
			plot.setClan(null, true);
			plot.updateSign();
		}
	}

}
