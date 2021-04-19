package fr.olympa.zta.ranks;

import java.sql.SQLException;

import org.bukkit.Location;

import fr.olympa.api.ranking.AbstractSQLRank;

public class LootChestRanking extends AbstractSQLRank {
	
	public LootChestRanking(Location location) throws SQLException {
		super("loot_chests", location, 10, false);
	}
	
	@Override
	public String getHologramName() {
		return "§e§lCoffres ouverts";
	}
	
	@Override
	public String getMessageName() {
		return "des coffres ouverts";
	}
	
	@Override
	protected String getColumn() {
		return "opened_chests";
	}
	
}
