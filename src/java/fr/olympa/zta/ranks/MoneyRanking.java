package fr.olympa.zta.ranks;

import java.sql.SQLException;

import org.bukkit.Location;

import fr.olympa.api.economy.OlympaMoney;
import fr.olympa.api.ranking.AbstractSQLRank;

public class MoneyRanking extends AbstractSQLRank {
	
	public MoneyRanking(Location location) throws SQLException {
		super("money", location, 10, false);
	}
	
	@Override
	public String getHologramName() {
		return "§e§lArgent personnel";
	}
	
	@Override
	public String getMessageName() {
		return "des Omega";
	}
	
	@Override
	protected String getColumn() {
		return "money";
	}
	
	@Override
	protected String formatScore(double score) {
		return OlympaMoney.format(score);
	}
	
}