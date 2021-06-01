package fr.olympa.zta.clans.plots;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

import fr.olympa.api.spigot.region.Region;
import fr.olympa.api.common.sql.SQLColumn;
import fr.olympa.api.common.sql.SQLTable;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.clans.ClansManagerZTA;
import fr.olympa.zta.utils.map.DynmapLink;

public class ClanPlotsManager implements Listener {

	public static final NamespacedKey SIGN_KEY = new NamespacedKey(OlympaZTA.getInstance(), "plotID");
	/*private static final String tableName = "`zta_clan_plots`";
	
	private static final OlympaStatement createPlot = new OlympaStatement("INSERT INTO " + tableName + " (`region`, `price`, `sign`, `spawn`) VALUES (?, ?, ?, ?)", true);
	public static final OlympaStatement updatePlotClan = new OlympaStatement("UPDATE " + tableName + " SET `clan` = ? WHERE (`id` = ?)");
	public static final OlympaStatement updatePlotNextPayment = new OlympaStatement("UPDATE " + tableName + " SET `next_payment` = ? WHERE (`id` = ?)");*/
	
	private SQLTable<ClanPlot> table;
	
	public SQLColumn<ClanPlot> columnID = new SQLColumn<ClanPlot>("id", "INT(11) UNSIGNED NOT NULL AUTO_INCREMENT", Types.INTEGER).setPrimaryKey(ClanPlot::getID);
	public SQLColumn<ClanPlot> columnRegion = new SQLColumn<ClanPlot>("region", "VARBINARY(8000) NOT NULL", Types.VARBINARY);
	public SQLColumn<ClanPlot> columnClan = new SQLColumn<ClanPlot>("clan", "INT NULL DEFAULT -1", Types.INTEGER).setUpdatable();
	public SQLColumn<ClanPlot> columnSign = new SQLColumn<ClanPlot>("sign", "VARCHAR(100) NOT NULL", Types.VARCHAR);
	public SQLColumn<ClanPlot> columnSpawn = new SQLColumn<ClanPlot>("spawn", "VARCHAR(100) NOT NULL", Types.VARCHAR);
	public SQLColumn<ClanPlot> columnPrice = new SQLColumn<ClanPlot>("price", "INT NOT NULL", Types.INTEGER);
	public SQLColumn<ClanPlot> columnNextPayment = new SQLColumn<ClanPlot>("next_payment", "BIGINT NOT NULL DEFAULT -1", Types.BIGINT).setUpdatable();

	private Map<Integer, ClanPlot> plots = new HashMap<>();

	public ClanPlotsManager(ClansManagerZTA clans) throws SQLException {
		table = new SQLTable<>("zta_clan_plots",
				Arrays.asList(columnID, columnRegion, columnClan, columnSign, columnSpawn, columnPrice, columnNextPayment),
				resultSet -> {
					try {
						ClanPlot plot = new ClanPlot(this, resultSet.getInt("id"), SpigotUtils.deserialize(resultSet.getBytes("region")), resultSet.getInt("price"), SpigotUtils.convertStringToLocation(resultSet.getString("sign")), SpigotUtils.convertStringToLocation(resultSet.getString("spawn")));
						DynmapLink.showClanPlot(plot);
						int clanID = resultSet.getInt("clan");
						if (clanID != -1) plot.setClan(clans.getClan(clanID), false);
						plot.setNextPayment(resultSet.getLong("next_payment"), false, true);
						return plot;
					}catch (Exception ex) {
						OlympaZTA.getInstance().getLogger().severe("Une erreur est survenue lors du chargement d'une parcelle.");
						ex.printStackTrace();
						return null;
					}
				});
		table.createOrAlter();

		plots = table.selectAll(null).stream().filter(Objects::nonNull).collect(Collectors.toMap(ClanPlot::getID, Function.identity()));
		
		new ClanPlotsCommand(this).register();
	}

	public ClanPlot create(Region region, int price, Block sign, Location spawn) throws SQLException, IOException {
		Location signLocation = sign.getLocation();

		ResultSet resultSet = table.insert(SpigotUtils.serialize(region), SpigotUtils.convertLocationToString(signLocation), SpigotUtils.convertLocationToString(spawn), price);
		resultSet.next();

		ClanPlot plot = new ClanPlot(this, resultSet.getInt(1), region, price, signLocation, spawn);
		DynmapLink.showClanPlot(plot);
		plot.updateSign();
		plots.put(plot.getID(), plot);
		resultSet.close();

		return plot;
	}

	public ClanPlot getPlot(Location location) {
		for (ClanPlot plot : plots.values()) {
			if (plot.getTrackedRegion().getRegion().isIn(location)) return plot;
		}
		return null;
	}
	
	public Map<Integer, ClanPlot> getPlots() {
		return plots;
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onInteract(PlayerInteractEvent e) {
		Block clickedBlock = e.getClickedBlock();
		if (clickedBlock == null) return;

		if (clickedBlock.getType().name().contains("_SIGN")) {
			Sign sign = (Sign) clickedBlock.getState();
			if (sign.getPersistentDataContainer().has(SIGN_KEY, PersistentDataType.INTEGER)) plots.get(sign.getPersistentDataContainer().get(SIGN_KEY, PersistentDataType.INTEGER)).signClick(e.getPlayer());
		}
	}

}
