package fr.olympa.zta;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.clans.ClanPlayerInterface;
import fr.olympa.api.economy.OlympaMoney;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.provider.OlympaPlayerObject;
import fr.olympa.api.sql.SQLColumn;
import fr.olympa.api.utils.observable.ObservableInt;
import fr.olympa.zta.clans.ClanZTA;
import fr.olympa.zta.clans.plots.ClanPlayerDataZTA;
import fr.olympa.zta.plots.PlayerPlot;

public class OlympaPlayerZTA extends OlympaPlayerObject implements ClanPlayerInterface<ClanZTA, ClanPlayerDataZTA> {

	public static final int MAX_SLOTS = 27;

	private static final SQLColumn<OlympaPlayerZTA> COLUMN_ENDER_CHEST = new SQLColumn<>("ender_chest", "VARBINARY(8000) NULL", Types.VARBINARY);
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_MONEY = new SQLColumn<>("money", "DOUBLE NULL DEFAULT 100", Types.DOUBLE);
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_PLOT = new SQLColumn<>("plot", "INT NOT NULL DEFAULT -1", Types.INTEGER);
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_KILLED_ZOMBIES = new SQLColumn<>("killed_zombies", "INT NOT NULL DEFAULT 0", Types.INTEGER);
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_KILLED_PLAYERS = new SQLColumn<>("killed_players", "INT NOT NULL DEFAULT 0", Types.INTEGER);
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_DEATH = new SQLColumn<>("deaths", "INT NOT NULL DEFAULT 0", Types.INTEGER);
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_HEADSHOTS = new SQLColumn<>("headshots", "INT NOT NULL DEFAULT 0", Types.INTEGER);
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_OTHER_SHOTS = new SQLColumn<>("other_shots", "INT NOT NULL DEFAULT 0", Types.INTEGER);
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_OPENED_CHESTS = new SQLColumn<>("opened_chests", "INT NOT NULL DEFAULT 0", Types.INTEGER);
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_KIT_VIP_TIME = new SQLColumn<>("kit_vip_time", "BIGINT NULL", Types.BIGINT);
	
	static final List<SQLColumn<OlympaPlayerZTA>> COLUMNS = Arrays.asList(COLUMN_ENDER_CHEST, COLUMN_MONEY, COLUMN_PLOT, COLUMN_KILLED_ZOMBIES, COLUMN_KILLED_PLAYERS, COLUMN_DEATH, COLUMN_HEADSHOTS, COLUMN_OTHER_SHOTS, COLUMN_OPENED_CHESTS, COLUMN_KIT_VIP_TIME);
	
	private ClanZTA clan = null;
	private PlayerPlot plot = null;
	public BukkitTask plotFind = null; // pas persistant
	/* Données */
	private Inventory enderChest = Bukkit.createInventory(null, 9, "Enderchest de " + getName());
	private OlympaMoney money = new OlympaMoney(100);
	public ObservableInt killedZombies = new ObservableInt(0);
	public ObservableInt killedPlayers = new ObservableInt(0);
	public ObservableInt deaths = new ObservableInt(0);
	public ObservableInt headshots = new ObservableInt(0);
	public ObservableInt otherShots = new ObservableInt(0);
	public ObservableInt openedChests = new ObservableInt(0);
	public long kitVIPtime = 0;
	
	public OlympaPlayerZTA(UUID uuid, String name, String ip) {
		super(uuid, name, ip);
		money.observe("scoreboard_update", () -> OlympaZTA.getInstance().lineMoney.updateHolder(OlympaZTA.getInstance().scoreboards.getPlayerScoreboard(this)));
		//enderChest.observe("datas", () -> COLUMN_MONEY.updateValue(this, money.get())); TODO
		money.observe("datas", () -> COLUMN_MONEY.updateValue(this, money.get()));
		killedZombies.observe("datas", () -> COLUMN_KILLED_ZOMBIES.updateValue(this, killedZombies.get()));
		killedPlayers.observe("datas", () -> COLUMN_KILLED_PLAYERS.updateValue(this, killedPlayers.get()));
		deaths.observe("datas", () -> COLUMN_DEATH.updateValue(this, deaths.get()));
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
			kitVIPtime = resultSet.getLong("kit_vip_time");
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
			statement.setLong(i++, kitVIPtime);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static OlympaPlayerZTA get(Player p) {
		return AccountProvider.get(p.getUniqueId());
	}
	
}
