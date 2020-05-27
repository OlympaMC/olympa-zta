package fr.olympa.zta.clans;

import java.util.Map.Entry;
import java.util.StringJoiner;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.olympa.api.clans.Clan;
import fr.olympa.api.clans.ClanPlayerInterface;
import fr.olympa.api.clans.ClansManager;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.scoreboard.sign.Scoreboard;
import fr.olympa.api.scoreboard.sign.lines.FixedLine;
import fr.olympa.api.scoreboard.sign.lines.TimerLine;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.clans.plots.ClanPlot;

public class ClanZTA extends Clan<ClanZTA> {

	private static FixedLine<OlympaPlayerZTA> header = new FixedLine<>("§7§oMon clan:");
	private static TimerLine<OlympaPlayerZTA> players = new TimerLine<>((x) -> {
		ClanZTA clan = x.getClan();
		Player p = x.getPlayer();
		StringJoiner joiner = new StringJoiner("\n");
		boolean inHub = OlympaZTA.getInstance().hub.isInHub(p.getLocation());
		for (Entry<OlympaPlayerInformations, ClanPlayerInterface<ClanZTA>> member : clan.getMembers()) {
			String memberName = member.getKey().getName();
			if (member.getValue() == null) {
				joiner.add("§c○ " + memberName);
			}else if (member.getValue() == x) {
				joiner.add("§6● §l" + memberName);
			}else {
				Location loc = member.getValue().getPlayer().getLocation();
				joiner.add("§e● " + memberName + " §l" + (inHub != OlympaZTA.getInstance().hub.isInHub(loc) ? 'x' : SpigotUtils.getDirectionToLocation(p, loc)));
			}
		}
		return joiner.toString();
	}, OlympaZTA.getInstance(), 10);
	
	public ClanPlot cachedPlot;

	public ClanZTA(ClansManager<ClanZTA> manager, int id, String name, long chief, int maxSize, double money, long created) {
		super(manager, id, name, chief, maxSize, money, created);
	}

	public ClanZTA(ClansManager<ClanZTA> manager, int id, String name, long chief, int maxSize) {
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
