package fr.olympa.zta.ranks;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;

import fr.olympa.api.spigot.economy.OlympaMoney;
import fr.olympa.api.spigot.ranking.AbstractRank;
import fr.olympa.api.common.sql.statement.OlympaStatement;

public class ClanMoneyRanking extends AbstractRank {
	
	public ClanMoneyRanking(Location location) throws SQLException {
		super("money_clan", location, 10, false);
	}
	
	@Override
	public String getHologramName() {
		return "§e§lArgent du Clan";
	}
	
	@Override
	public String getMessageName() {
		return "de l'argent du clan";
	}
	
	@Override
	protected void fillUpScores(ScoreEntry[] scores) throws SQLException {
		OlympaStatement topStatement =
				new OlympaStatement("SELECT name, money"
						+ " FROM zta_clans"
						+ " ORDER BY money DESC LIMIT " + getMaxSlots());
		try (PreparedStatement statement = topStatement.createStatement()) {
			ResultSet resultSet = topStatement.executeQuery(statement);
			int i = 0;
			while (resultSet.next()) {
				double value = resultSet.getDouble("money");
				if (value != 0) scores[i].fill(resultSet.getString("name"), value);
				i++;
			}
			resultSet.close();
		}
	}
	
	@Override
	protected String formatScore(double score) {
		return OlympaMoney.format(score);
	}
	
}
