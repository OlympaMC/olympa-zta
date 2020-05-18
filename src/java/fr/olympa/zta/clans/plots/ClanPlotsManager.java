package fr.olympa.zta.clans.plots;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

import fr.olympa.api.region.Region;
import fr.olympa.api.sql.OlympaStatement;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.clans.ClansManagerZTA;

public class ClanPlotsManager implements Listener {

	private static final String tableName = "`zta_clan_plots`";
	private static final NamespacedKey signKey = new NamespacedKey(OlympaZTA.getInstance(), "plotID");

	private static final OlympaStatement createPlot = new OlympaStatement("INSERT INTO " + tableName + " (`region`, `price`, `sign`, `spawn`) VALUES (?, ?, ?, ?)", true);
	public static final OlympaStatement updatePlotClan = new OlympaStatement("UPDATE " + tableName + " SET `clan` = ? WHERE (`id` = ?)");
	public static final OlympaStatement updatePlotNextPayment = new OlympaStatement("UPDATE " + tableName + " SET `next_payment` = ? WHERE (`id` = ?)");

	private Map<Integer, ClanPlot> plots = new HashMap<>();

	public ClanPlotsManager(ClansManagerZTA clans) throws SQLException {
		OlympaCore.getInstance().getDatabase().createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " (" +
				"  `id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT," +
				"  `region` VARBINARY(8000) NOT NULL," +
				"  `clan` INT NULL DEFAULT -1," +
				"  `sign` VARCHAR(100) NOT NULL," +
				"  `spawn` VARCHAR(100) NOT NULL," +
				"  `price` INT NOT NULL," +
				"  `next_payment` BIGINT NOT NULL DEFAULT 0," +
				"  PRIMARY KEY (`id`))");

		ResultSet resultSet = OlympaCore.getInstance().getDatabase().createStatement().executeQuery("SELECT * FROM " + tableName);
		while (resultSet.next()) {
			try {
				ClanPlot plot = new ClanPlot(resultSet.getInt("id"), SpigotUtils.deserialize(resultSet.getBytes("region")), resultSet.getInt("price"), SpigotUtils.convertStringToLocation(resultSet.getString("sign")), SpigotUtils.convertStringToLocation(resultSet.getString("spawn")));
				plots.put(plot.getID(), plot);
				int clanID = resultSet.getInt("clan");
				if (clanID != -1) plot.setClan(clans.getClan(clanID), false);
				plot.setNextPayment(resultSet.getLong("next_payment"), false);
			}catch (Exception ex) {
				OlympaZTA.getInstance().getLogger().severe("Une erreur est survenue lors du chargement d'une parcelle.");
				ex.printStackTrace();
				continue;
			}
		}
	}

	public ClanPlot create(Region region, int price, Block sign, Location spawn) throws SQLException, IOException {
		Location signLocation = sign.getLocation();

		PreparedStatement statement = createPlot.getStatement();
		statement.setBytes(1, SpigotUtils.serialize(region));
		statement.setInt(2, price);
		statement.setString(3, SpigotUtils.convertLocationToString(signLocation));
		statement.setString(4, SpigotUtils.convertLocationToString(spawn));
		statement.executeUpdate();
		ResultSet resultSet = statement.getGeneratedKeys();
		resultSet.next();

		ClanPlot plot = new ClanPlot(resultSet.getInt(1), region, price, signLocation, spawn);
		plot.setClan(null, true); // va initialiser le panneau
		plots.put(plot.getID(), plot);
		resultSet.close();

		Sign signState = (Sign) sign.getState();
		signState.getPersistentDataContainer().set(signKey, PersistentDataType.INTEGER, plot.getID());

		return plot;
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Block clickedBlock = e.getClickedBlock();
		if (clickedBlock == null) return;

		try {
			Sign sign = (Sign) clickedBlock.getState();
			if (sign.getPersistentDataContainer().has(signKey, PersistentDataType.INTEGER)) plots.get(sign.getPersistentDataContainer().get(signKey, PersistentDataType.INTEGER)).signClick(e.getPlayer());
		}catch (ClassCastException ex) { // pas un panneau
			Location loc = clickedBlock.getLocation();
			for (ClanPlot plot : plots.values()) {
				if (plot.getRegion().isIn(loc)) {
					e.setCancelled(plot.onInteract(e.getPlayer()));
					break;
				}
			}
		}
	}

}
