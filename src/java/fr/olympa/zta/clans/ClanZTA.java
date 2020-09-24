package fr.olympa.zta.clans;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.olympa.api.clans.Clan;
import fr.olympa.api.clans.ClanPlayerInterface;
import fr.olympa.api.clans.ClansManager;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.lines.TimerLine;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.scoreboard.sign.Scoreboard;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.clans.plots.ClanPlayerDataZTA;
import fr.olympa.zta.clans.plots.ClanPlot;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ClanZTA extends Clan<ClanZTA, ClanPlayerDataZTA> {

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
	
	private long plotExpirationReset = -1;
	
	private ClanPlot cachedPlot;

	public ClanZTA(ClansManager<ClanZTA, ClanPlayerDataZTA> manager, int id, String name, OlympaPlayerInformations chief, int maxSize, double money, long created, long plotExpirationReset) {
		super(manager, id, name, chief, maxSize, money, created);
		this.plotExpirationReset = plotExpirationReset;
	}

	public ClanZTA(ClansManager<ClanZTA, ClanPlayerDataZTA> manager, int id, String name, OlympaPlayerInformations chief, int maxSize) {
		super(manager, id, name, chief, maxSize);
	}
	
	public ClansManagerZTA getClansManager() {
		return super.getClansManager();
	}

	public ClanPlot getCachedPlot() {
		return cachedPlot;
	}
	
	public void setCachedPlot(ClanPlot cachedPlot) {
		this.cachedPlot = cachedPlot;
	}
	
	public boolean resetExpirationTime() {
		if (this.plotExpirationReset == -1) return false;
		this.plotExpirationReset = -1;
		updateResetExpiration();
		return true;
	}
	
	public void setResetExpirationTime() {
		this.plotExpirationReset = System.currentTimeMillis() + 7 * 24 * 3600 * 1000; // 1 semaine d'avertissement pour expiration
		updateResetExpiration();
	}
	
	private void updateResetExpiration() {
		try {
			getClansManager().plotExpirationResetColumn.updateValue(this, plotExpirationReset, Types.BIGINT);
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void removedOnlinePlayer(ClanPlayerInterface<ClanZTA, ClanPlayerDataZTA> oplayer) {
		super.removedOnlinePlayer(oplayer);
		
		OlympaZTA.getInstance().scoreboards.removePlayerScoreboard((OlympaPlayerZTA) oplayer);
		OlympaZTA.getInstance().scoreboards.create((OlympaPlayerZTA) oplayer);
	}

	@Override
	public void memberJoin(ClanPlayerInterface<ClanZTA, ClanPlayerDataZTA> member) {
		super.memberJoin(member);

		Scoreboard<OlympaPlayerZTA> scoreboard = OlympaZTA.getInstance().scoreboards.getPlayerScoreboard((OlympaPlayerZTA) member);
		scoreboard.addLine(FixedLine.EMPTY_LINE);
		scoreboard.addLine(header);
		scoreboard.addLine(players);
		
		if (getChief().equals(member.getInformation()) && cachedPlot == null && plotExpirationReset != -1) {
			if (plotExpirationReset > System.currentTimeMillis()) {
				BaseComponent[] components = TextComponent.fromLegacyText(format("§cLe paiement de la parcelle n'a pas été réitéré, celle-ci est donc arrivée à expiration."));
				BaseComponent lastCompo = components[components.length - 1];
				lastCompo.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§eClique ici pour ne plus afficher ce message.")));
				lastCompo.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/clans dismissExpirationMessage"));
				member.getPlayer().spigot().sendMessage(components);
			}else resetExpirationTime();
		}
	}

	@Override
	public void disband() {
		super.disband();
		
		if (getCachedPlot() != null) {
			ClanPlot plot = getCachedPlot();
			plot.setClan(null, true);
			plot.updateSign();
		}
	}

}
