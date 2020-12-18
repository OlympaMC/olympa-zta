package fr.olympa.zta.clans;

import java.sql.SQLException;

import fr.olympa.api.clans.Clan;
import fr.olympa.api.clans.ClanPlayerInterface;
import fr.olympa.api.clans.ClansManager;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.scoreboard.sign.Scoreboard;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.clans.plots.ClanPlayerDataZTA;
import fr.olympa.zta.clans.plots.ClanPlot;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ClanZTA extends Clan<ClanZTA, ClanPlayerDataZTA> {
	
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
			getClansManager().plotExpirationResetColumn.updateValue(this, plotExpirationReset);
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void removedOnlinePlayer(ClanPlayerInterface<ClanZTA, ClanPlayerDataZTA> oplayer) {
		super.removedOnlinePlayer(oplayer);
		
		OlympaZTA.getInstance().scoreboards.create((OlympaPlayerZTA) oplayer);
	}

	@Override
	public void memberJoin(ClanPlayerInterface<ClanZTA, ClanPlayerDataZTA> member) {
		super.memberJoin(member);

		Scoreboard<OlympaPlayerZTA> scoreboard = OlympaZTA.getInstance().scoreboards.getPlayerScoreboard((OlympaPlayerZTA) member);
		if (scoreboard != null) getClansManager().addLines(scoreboard); // has not been loaded
		
		if (getChief().equals(member.getInformation())) {
			if (cachedPlot == null) {
				if (plotExpirationReset != -1) {
					if (plotExpirationReset > System.currentTimeMillis()) {
						BaseComponent[] components = TextComponent.fromLegacyText(format("§cLe paiement de la parcelle n'a pas été réitéré, celle-ci est donc arrivée à expiration."));
						BaseComponent lastCompo = components[components.length - 1];
						lastCompo.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§eClique ici pour ne plus afficher ce message.")));
						lastCompo.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/clans dismissExpirationMessage"));
						member.getPlayer().spigot().sendMessage(components);
					}else resetExpirationTime();
				}
			}else {
				if (cachedPlot.getNextPayment() - System.currentTimeMillis() < ClanPlot.PAYMENT_DURATION_MILLIS) {
					member.getPlayer().sendMessage(format("La parcelle du clan n'a pas été payée cette semaine. Celle-ci risque d'expirer par défaut de paiement."));
				}
			}
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
