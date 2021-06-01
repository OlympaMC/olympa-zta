package fr.olympa.zta.ranks;

import java.sql.SQLException;

import org.bukkit.Location;

import fr.olympa.api.spigot.ranking.AbstractSQLRank;

public class KillPlayerRanking extends AbstractSQLRank {
	
	public KillPlayerRanking(Location location) throws SQLException {
		super("kill_player", location, 10, false);
	}
	
	@Override
	public String getHologramName() {
		return "§e§lJoueurs tués";
	}
	
	@Override
	public String getMessageName() {
		return "des joueurs tués";
	}
	
	@Override
	protected String getColumn() {
		return "killed_players";
	}
	
}
