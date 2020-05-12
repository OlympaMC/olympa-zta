package fr.olympa.zta.plots.clans;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.Listener;

import fr.olympa.api.region.Region;
import fr.olympa.core.spigot.OlympaCore;

public class ClanPlotsManager implements Listener {

	private static final String tableName = "`zta_clan_plots`";

	private Map<Integer, ClanPlot> plots = new HashMap<>();

	public ClanPlotsManager() throws SQLException {
		OlympaCore.getInstance().getDatabase().createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " (" +
				"  `id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT," +
				"  `region` VARBINARY(8000) NOT NULL," +
				"  `clan` INT NULL DEFAULT -1," +
				"  `price` INT NOT NULL," +
				"  PRIMARY KEY (`id`))");


	}

	public ClanPlot create(Region region, int price) {

	}

}
