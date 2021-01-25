package fr.olympa.zta;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.clans.ClanPlayerInterface;
import fr.olympa.api.economy.OlympaMoney;
import fr.olympa.api.enderchest.EnderChestPlayerInterface;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.provider.OlympaPlayerObject;
import fr.olympa.api.sql.SQLColumn;
import fr.olympa.api.utils.observable.ObservableInt;
import fr.olympa.api.utils.observable.ObservableLong;
import fr.olympa.api.utils.observable.ObservableValue;
import fr.olympa.zta.clans.ClanZTA;
import fr.olympa.zta.clans.plots.ClanPlayerDataZTA;
import fr.olympa.zta.plots.PlayerPlot;

public class OlympaPlayerZTA extends OlympaPlayerObject implements ClanPlayerInterface<ClanZTA, ClanPlayerDataZTA>, EnderChestPlayerInterface {

	public static final int MAX_SLOTS = 27;

	private static final SQLColumn<OlympaPlayerZTA> COLUMN_ENDER_CHEST = new SQLColumn<OlympaPlayerZTA>("ender_chest", "VARBINARY(8000) NULL", Types.VARBINARY).setUpdatable();
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_MONEY = new SQLColumn<OlympaPlayerZTA>("money", "DOUBLE NULL DEFAULT 100", Types.DOUBLE).setUpdatable();
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_PLOT = new SQLColumn<OlympaPlayerZTA>("plot", "INT NOT NULL DEFAULT -1", Types.INTEGER).setUpdatable();
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_KILLED_ZOMBIES = new SQLColumn<OlympaPlayerZTA>("killed_zombies", "INT NOT NULL DEFAULT 0", Types.INTEGER).setUpdatable();
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_KILLED_PLAYERS = new SQLColumn<OlympaPlayerZTA>("killed_players", "INT NOT NULL DEFAULT 0", Types.INTEGER).setUpdatable();
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_DEATH = new SQLColumn<OlympaPlayerZTA>("deaths", "INT NOT NULL DEFAULT 0", Types.INTEGER).setUpdatable();
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_HEADSHOTS = new SQLColumn<OlympaPlayerZTA>("headshots", "INT NOT NULL DEFAULT 0", Types.INTEGER).setUpdatable();
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_OTHER_SHOTS = new SQLColumn<OlympaPlayerZTA>("other_shots", "INT NOT NULL DEFAULT 0", Types.INTEGER).setUpdatable();
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_OPENED_CHESTS = new SQLColumn<OlympaPlayerZTA>("opened_chests", "INT NOT NULL DEFAULT 0", Types.INTEGER).setUpdatable();
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_KIT_VIP_TIME = new SQLColumn<OlympaPlayerZTA>("kit_vip_time", "BIGINT NULL DEFAULT 0", Types.BIGINT).setUpdatable();
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_BACK_VIP_TIME = new SQLColumn<OlympaPlayerZTA>("back_vip_time", "BIGINT NULL DEFAULT 0", Types.BIGINT).setUpdatable();
	
	static final List<SQLColumn<OlympaPlayerZTA>> COLUMNS = Arrays.asList(COLUMN_ENDER_CHEST, COLUMN_MONEY, COLUMN_PLOT, COLUMN_KILLED_ZOMBIES, COLUMN_KILLED_PLAYERS, COLUMN_DEATH, COLUMN_HEADSHOTS, COLUMN_OTHER_SHOTS, COLUMN_OPENED_CHESTS, COLUMN_KIT_VIP_TIME, COLUMN_BACK_VIP_TIME);
	
	private ClanZTA clan = null;
	public BukkitTask plotFind = null; // pas persistant
	/* Données */
	private ItemStack[] enderChestContents = new ItemStack[0];
	private OlympaMoney money = new OlympaMoney(100);
	private ObservableValue<PlayerPlot> plot = new ObservableValue<PlayerPlot>(null);
	public ObservableInt killedZombies = new ObservableInt(0);
	public ObservableInt killedPlayers = new ObservableInt(0);
	public ObservableInt deaths = new ObservableInt(0);
	public ObservableInt headshots = new ObservableInt(0);
	public ObservableInt otherShots = new ObservableInt(0);
	public ObservableInt openedChests = new ObservableInt(0);
	public ObservableLong kitVIPTime = new ObservableLong(0);
	public ObservableLong backVIPTime = new ObservableLong(0);
	
	public OlympaPlayerZTA(UUID uuid, String name, String ip) {
		super(uuid, name, ip);
		money.observe("scoreboard_update", () -> OlympaZTA.getInstance().lineMoney.updateHolder(OlympaZTA.getInstance().scoreboards.getPlayerScoreboard(this)));
		money.observe("datas", () -> COLUMN_MONEY.updateAsync(this, money.get(), null, null));
		plot.observe("datas", () -> COLUMN_PLOT.updateAsync(this, plot.mapOr(PlayerPlot::getID, -1), null, null));
		killedZombies.observe("datas", () -> COLUMN_KILLED_ZOMBIES.updateAsync(this, killedZombies.get(), null, null));
		killedPlayers.observe("datas", () -> COLUMN_KILLED_PLAYERS.updateAsync(this, killedPlayers.get(), null, null));
		deaths.observe("datas", () -> COLUMN_DEATH.updateAsync(this, deaths.get(), null, null));
		headshots.observe("datas", () -> COLUMN_HEADSHOTS.updateAsync(this, headshots.get(), null, null));
		otherShots.observe("datas", () -> COLUMN_OTHER_SHOTS.updateAsync(this, otherShots.get(), null, null));
		openedChests.observe("datas", () -> COLUMN_OPENED_CHESTS.updateAsync(this, openedChests.get(), null, null));
		kitVIPTime.observe("datas", () -> COLUMN_KIT_VIP_TIME.updateAsync(this, kitVIPTime.get(), null, null));
		backVIPTime.observe("datas", () -> COLUMN_BACK_VIP_TIME.updateAsync(this, backVIPTime.get(), null, null));
	}

	@Override
	public ItemStack[] getEnderChestContents() {
		return enderChestContents;
	}

	@Override
	public void setEnderChestContents(ItemStack[] contents) {
		this.enderChestContents = contents;
		try {
			COLUMN_ENDER_CHEST.updateAsync(this, ItemUtils.serializeItemsArray(enderChestContents), null, null);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public int getEnderChestRows() {
		return ZTAPermissions.ENDERCHEST_MORE_SPACE.hasPermission(this) ? 3 : 2;
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
		return plot.get();
	}
	
	public void setPlot(PlayerPlot plot) {
		this.plot.set(plot);
	}

	@Override
	public void loadDatas(ResultSet resultSet) throws SQLException {
		try {
			enderChestContents = ItemUtils.deserializeItemsArray(resultSet.getBytes("ender_chest"));
			money.set(resultSet.getDouble("money"));
			plot.set(OlympaZTA.getInstance().plotsManager.getPlot(resultSet.getInt("plot"), true));
			killedZombies.set(resultSet.getInt("killed_zombies"));
			killedPlayers.set(resultSet.getInt("killed_players"));
			deaths.set(resultSet.getInt("deaths"));
			headshots.set(resultSet.getInt("headshots"));
			otherShots.set(resultSet.getInt("other_shots"));
			openedChests.set(resultSet.getInt("opened_chests"));
			kitVIPTime.set(resultSet.getLong("kit_vip_time"));
			backVIPTime.set(resultSet.getLong("back_vip_time"));
		}catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	public static OlympaPlayerZTA get(Player p) {
		return AccountProvider.get(p.getUniqueId());
	}
	
}
