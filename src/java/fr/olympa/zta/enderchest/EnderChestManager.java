package fr.olympa.zta.enderchest;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import fr.olympa.api.lines.CyclingLine;
import fr.olympa.api.sql.statement.OlympaStatement;
import fr.olympa.api.sql.statement.StatementType;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.utils.DynmapLink;

public class EnderChestManager {
	
	private static final String TABLE_NAME = "`zta_enderchests`";
	
	private List<Location> enderchests = new ArrayList<>();
	
	private OlympaStatement insertStatement = new OlympaStatement(StatementType.INSERT, TABLE_NAME, "world", "x", "y", "z");
	
	public EnderChestManager() throws SQLException {
		Statement statement = OlympaCore.getInstance().getDatabase().createStatement();
		statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
				+ "  `world` VARCHAR(45) NOT NULL,"
				+ "  `x` INT NOT NULL,"
				+ "  `y` INT NOT NULL,"
				+ "  `z` INT NOT NULL);");
		
		ResultSet resultSet = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
		while (resultSet.next()) {
			addEnderChest(new Location(Bukkit.getWorld(resultSet.getString("world")), resultSet.getInt("x"), resultSet.getInt("y"), resultSet.getInt("z")), false);
		}
		resultSet.close();
		statement.close();
		
		OlympaZTA.getInstance().sendMessage("§6%d §eEnderChests chargés", enderchests.size());
	}
	
	public void addEnderChest(Location location, boolean database) {
		enderchests.add(location);
		DynmapLink.showEnderChest(location);
		OlympaZTA.getInstance().getTask().runTask(() -> OlympaCore.getInstance().getHologramsManager().createHologram(location.add(0.5, 1, 0.5), false, new CyclingLine<>(CyclingLine.getAnim("EnderChest", ChatColor.AQUA, ChatColor.DARK_AQUA), 2, 3 * 20)));
		if (database) {
			try {
				PreparedStatement statement = insertStatement.getStatement();
				statement.setString(1, location.getWorld().getName());
				statement.setInt(2, location.getBlockX());
				statement.setInt(3, location.getBlockY());
				statement.setInt(4, location.getBlockZ());
				statement.executeUpdate();
			}catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
}
