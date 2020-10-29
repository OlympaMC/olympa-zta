package fr.olympa.zta.enderchest;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import fr.olympa.api.holograms.Hologram;
import fr.olympa.api.lines.CyclingLine;
import fr.olympa.api.sql.statement.OlympaStatement;
import fr.olympa.api.sql.statement.StatementType;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.utils.DynmapLink;

public class EnderChestManager implements Listener {
	
	private static final String TABLE_NAME = "`zta_enderchests`";
	
	private Map<Location, Hologram> enderchests = new HashMap<>();
	
	private OlympaStatement insertStatement = new OlympaStatement(StatementType.INSERT, TABLE_NAME, "world", "x", "y", "z");
	private OlympaStatement deleteStatement = new OlympaStatement(StatementType.DELETE, TABLE_NAME, "world", "x", "y", "z");
	
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
		enderchests.put(location, OlympaCore.getInstance().getHologramsManager().createHologram(location.add(0.5, 1, 0.5), false, new CyclingLine<>(CyclingLine.getAnim("EnderChest", "&l&b", "&l&3"), 2, 3 * 10)));
		DynmapLink.showEnderChest(location);
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
	
	public void removeEnderChest(Location location) {
		Hologram hologram = enderchests.remove(location);
		if (hologram == null) return;
		hologram.remove();
		DynmapLink.hideEnderChest(location);
		try {
			PreparedStatement statement = deleteStatement.getStatement();
			statement.setString(1, location.getWorld().getName());
			statement.setInt(2, location.getBlockX());
			statement.setInt(3, location.getBlockY());
			statement.setInt(4, location.getBlockZ());
			statement.executeUpdate();
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onBlockBreak(BlockBreakEvent e) {
		if (e.isCancelled()) return;
		if (e.getBlock().getType() != Material.ENDER_CHEST) return;
		addEnderChest(e.getBlock().getLocation(), true);
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onBlockPlace(BlockPlaceEvent e) {
		if (e.isCancelled()) return;
		if (e.getBlock().getType() != Material.ENDER_CHEST) return;
		removeEnderChest(e.getBlock().getLocation());
	}
	
}
