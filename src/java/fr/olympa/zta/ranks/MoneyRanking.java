package fr.olympa.zta.ranks;

import java.sql.SQLException;

import org.bukkit.Location;

import fr.olympa.api.ranking.AbstractSQLRank;

public class MoneyRanking extends AbstractSQLRank {
	
	public MoneyRanking(Location location) throws SQLException {
		super("money", location, 10, false);
	}
	
	@Override
	public String getHologramName() {
		return "§e§lOmega";
	}
	
	@Override
	public String getMessageName() {
		return "des Omega";
	}
	
	@Override
	protected String getColumn() {
		return "money";
	}
	
}
