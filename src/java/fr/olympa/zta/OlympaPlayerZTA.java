package fr.olympa.zta;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.ImmutableMap;

import fr.olympa.api.clans.ClanPlayerInterface;
import fr.olympa.api.economy.OlympaMoney;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.provider.OlympaPlayerObject;
import fr.olympa.api.utils.observable.ObservableInt;
import fr.olympa.zta.clans.ClanZTA;
import fr.olympa.zta.clans.plots.ClanPlayerDataZTA;
import fr.olympa.zta.plots.PlayerPlot;

public class OlympaPlayerZTA extends OlympaPlayerObject implements ClanPlayerInterface<ClanZTA, ClanPlayerDataZTA> {

	public static final int MAX_SLOTS = 27;
	static final Map<String, String> COLUMNS = ImmutableMap.<String, String>builder()
			.put("ender_chest", "VARBINARY(8000) NULL")
			.put("money", "DOUBLE NULL DEFAULT 0")
			.put("plot", "INT NOT NULL DEFAULT -1")
			.put("killed_zombies", "INT NOT NULL DEFAULT 0")
			.put("killed_players", "INT NOT NULL DEFAULT 0")
			.put("deaths", "INT NOT NULL DEFAULT 0")
			.put("headshots", "INT NOT NULL DEFAULT 0")
			.put("other_shots", "INT NOT NULL DEFAULT 0")
			.put("opened_chests", "INT NOT NULL DEFAULT 0")
			.build();

	private Inventory enderChest = Bukkit.createInventory(null, 9, "Enderchest de " + getName());
	private OlympaMoney money = new OlympaMoney(0);
	private ClanZTA clan = null;
	private PlayerPlot plot = null;
	public BukkitTask plotFind = null; // pas persistant
	/* Stats */
	public ObservableInt killedZombies = new ObservableInt(0);
	public ObservableInt killedPlayers = new ObservableInt(0);
	public ObservableInt deaths = new ObservableInt(0);
	public ObservableInt headshots = new ObservableInt(0);
	public ObservableInt otherShots = new ObservableInt(0);
	public ObservableInt openedChests = new ObservableInt(0);

	public OlympaPlayerZTA(UUID uuid, String name, String ip) {
		super(uuid, name, ip);
		money.observe("scoreboard_update", () -> OlympaZTA.getInstance().lineMoney.updateHolder(OlympaZTA.getInstance().scoreboards.getPlayerScoreboard(this)));
	}

	public Inventory getEnderChest() {
		return enderChest;
	}

	@Override
	public OlympaMoney getGameMoney() {
		return money;
	}

	public ClanZTA getClan() {
		return clan;
	}

	public void setClan(ClanZTA clan) {
		this.clan = clan;
	}

	public PlayerPlot getPlot() {
		return plot;
	}

	public void setPlot(PlayerPlot plot) {
		this.plot = plot;
	}

	public void loadDatas(ResultSet resultSet) throws SQLException {
		try {
			enderChest.setContents(ItemUtils.deserializeItemsArray(resultSet.getBytes("ender_chest")));
			money.set(resultSet.getDouble("money"));
			plot = OlympaZTA.getInstance().plotsManager.getPlot(resultSet.getInt("plot"), true);
			killedZombies.set(resultSet.getInt("killed_zombies"));
			killedPlayers.set(resultSet.getInt("killed_players"));
			deaths.set(resultSet.getInt("deaths"));
			headshots.set(resultSet.getInt("headshots"));
			otherShots.set(resultSet.getInt("other_shots"));
			openedChests.set(resultSet.getInt("opened_chests"));
		}catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	public void saveDatas(PreparedStatement statement) throws SQLException {
		try {
			int i = 1;
			statement.setBytes(i++, ItemUtils.serializeItemsArray(enderChest.getContents()));
			statement.setDouble(i++, money.get());
			statement.setInt(i++, plot == null ? -1 : plot.getID());
			statement.setInt(i++, killedZombies.get());
			statement.setInt(i++, killedPlayers.get());
			statement.setInt(i++, deaths.get());
			statement.setInt(i++, headshots.get());
			statement.setInt(i++, otherShots.get());
			statement.setInt(i++, openedChests.get());
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static OlympaPlayerZTA get(Player p) {
		return AccountProvider.get(p.getUniqueId());
	}
	
}
