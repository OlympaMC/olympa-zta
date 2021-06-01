package fr.olympa.zta.ranks;

import java.sql.SQLException;

import org.bukkit.Location;

import fr.olympa.api.spigot.ranking.AbstractSQLRank;

public class KillZombieRanking extends AbstractSQLRank {
	
	public KillZombieRanking(Location location) throws SQLException {
		super("kill_zombie", location, 10, false);
	}
	
	@Override
	public String getHologramName() {
		return "§e§lZombies tués";
	}
	
	@Override
	public String getMessageName() {
		return "des zombies tués";
	}
	
	@Override
	protected String getColumn() {
		return "killed_zombies";
	}
	
}
