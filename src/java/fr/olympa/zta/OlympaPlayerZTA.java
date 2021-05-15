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
import fr.olympa.api.trades.TradeBag;
import fr.olympa.api.trades.TradePlayerInterface;
import fr.olympa.api.utils.observable.ObservableBoolean;
import fr.olympa.api.utils.observable.ObservableInt;
import fr.olympa.api.utils.observable.ObservableLong;
import fr.olympa.api.utils.observable.ObservableValue;
import fr.olympa.zta.clans.ClanZTA;
import fr.olympa.zta.clans.plots.ClanPlayerDataZTA;
import fr.olympa.zta.plots.PlayerPlot;
import fr.olympa.zta.settings.ClanBoardSetting;

public class OlympaPlayerZTA extends OlympaPlayerObject implements ClanPlayerInterface<ClanZTA, ClanPlayerDataZTA>, EnderChestPlayerInterface, TradePlayerInterface {

	public static final int MAX_SLOTS = 27;

	/* Datas */
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_ENDER_CHEST = new SQLColumn<OlympaPlayerZTA>("ender_chest", "VARBINARY(60000) NULL", Types.VARBINARY).setUpdatable();
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_MONEY = new SQLColumn<OlympaPlayerZTA>("money", "DOUBLE NULL DEFAULT 100", Types.DOUBLE).setUpdatable();
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_PLOT = new SQLColumn<OlympaPlayerZTA>("plot", "INT NOT NULL DEFAULT -1", Types.INTEGER).setUpdatable();
	/* Stats */
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_KILLED_ZOMBIES = new SQLColumn<OlympaPlayerZTA>("killed_zombies", "INT NOT NULL DEFAULT 0", Types.INTEGER).setUpdatable();
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_KILLED_PLAYERS = new SQLColumn<OlympaPlayerZTA>("killed_players", "INT NOT NULL DEFAULT 0", Types.INTEGER).setUpdatable();
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_DEATH = new SQLColumn<OlympaPlayerZTA>("deaths", "INT NOT NULL DEFAULT 0", Types.INTEGER).setUpdatable();
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_HEADSHOTS = new SQLColumn<OlympaPlayerZTA>("headshots", "INT NOT NULL DEFAULT 0", Types.INTEGER).setUpdatable();
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_OTHER_SHOTS = new SQLColumn<OlympaPlayerZTA>("other_shots", "INT NOT NULL DEFAULT 0", Types.INTEGER).setUpdatable();
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_OPENED_CHESTS = new SQLColumn<OlympaPlayerZTA>("opened_chests", "INT NOT NULL DEFAULT 0", Types.INTEGER).setUpdatable();
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_PLAY_TIME = new SQLColumn<OlympaPlayerZTA>("play_time", "BIGINT NOT NULL DEFAULT 0", Types.BIGINT).setUpdatable();
	/* Times */
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_KIT_VIP_TIME = new SQLColumn<OlympaPlayerZTA>("kit_vip_time", "BIGINT NULL DEFAULT 0", Types.BIGINT).setUpdatable();
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_BACK_VIP_TIME = new SQLColumn<OlympaPlayerZTA>("back_vip_time", "BIGINT NULL DEFAULT 0", Types.BIGINT).setUpdatable();
	/* Parameters */
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_PARAMETER_AMBIENT = new SQLColumn<OlympaPlayerZTA>("param_ambient", "BOOLEAN NOT NULL DEFAULT 1", Types.BOOLEAN).setUpdatable();
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_PARAMETER_BLOOD = new SQLColumn<OlympaPlayerZTA>("param_blood", "BOOLEAN NOT NULL DEFAULT 1", Types.BOOLEAN).setUpdatable();
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_PARAMETER_QUESTS_BOARD = new SQLColumn<OlympaPlayerZTA>("param_quests_board", "BOOLEAN NOT NULL DEFAULT 1", Types.BOOLEAN).setUpdatable();
	private static final SQLColumn<OlympaPlayerZTA> COLUMN_PARAMETER_CLAN_BOARD = new SQLColumn<OlympaPlayerZTA>("param_clan_board", "TINYINT(3) NOT NULL DEFAULT 0", Types.TINYINT).setUpdatable();
	
	static final List<SQLColumn<OlympaPlayerZTA>> COLUMNS = Arrays.asList(
			COLUMN_ENDER_CHEST, COLUMN_MONEY, COLUMN_PLOT,
			COLUMN_KILLED_ZOMBIES, COLUMN_KILLED_PLAYERS, COLUMN_DEATH, COLUMN_HEADSHOTS, COLUMN_OTHER_SHOTS, COLUMN_OPENED_CHESTS, COLUMN_PLAY_TIME,
			COLUMN_KIT_VIP_TIME, COLUMN_BACK_VIP_TIME,
			COLUMN_PARAMETER_AMBIENT, COLUMN_PARAMETER_BLOOD, COLUMN_PARAMETER_QUESTS_BOARD, COLUMN_PARAMETER_CLAN_BOARD);
	
	private ClanZTA clan = null;
	public BukkitTask plotFind = null; // pas persistant
	public long joinTime;
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
	public ObservableLong playTime = new ObservableLong(0);
	public ObservableLong kitVIPTime = new ObservableLong(0);
	public ObservableLong backVIPTime = new ObservableLong(0);
	
	public ObservableBoolean parameterAmbient = new ObservableBoolean(true);
	public ObservableBoolean parameterBlood = new ObservableBoolean(true);
	public ObservableBoolean parameterQuestsBoard = new ObservableBoolean(true);
	public ObservableValue<ClanBoardSetting> parameterClanBoard = new ObservableValue<ClanBoardSetting>(ClanBoardSetting.ONLINE_FIVE);
	
	public OlympaPlayerZTA(UUID uuid, String name, String ip) {
		super(uuid, name, ip);
	}
	
	@Override
	public void loaded() {
		super.loaded();
		money.observe("scoreboard_update", () -> OlympaZTA.getInstance().lineMoney.updateHolder(OlympaZTA.getInstance().scoreboards.getPlayerScoreboard(this)));
		parameterClanBoard.observe("scoreboard_manage", () -> OlympaZTA.getInstance().clansManager.updateBoardParameter(this, parameterClanBoard.get()));
		parameterQuestsBoard.observe("scoreboard_manage", () -> OlympaZTA.getInstance().beautyQuestsLink.updateBoardParameter(this, parameterQuestsBoard.get()));
		
		money.observe("datas", () -> COLUMN_MONEY.updateAsync(this, money.get(), null, null));
		plot.observe("datas", () -> COLUMN_PLOT.updateAsync(this, plot.mapOr(PlayerPlot::getID, -1), null, null));
		killedZombies.observe("datas", () -> COLUMN_KILLED_ZOMBIES.updateAsync(this, killedZombies.get(), null, null));
		killedPlayers.observe("datas", () -> COLUMN_KILLED_PLAYERS.updateAsync(this, killedPlayers.get(), null, null));
		deaths.observe("datas", () -> COLUMN_DEATH.updateAsync(this, deaths.get(), null, null));
		headshots.observe("datas", () -> COLUMN_HEADSHOTS.updateAsync(this, headshots.get(), null, null));
		otherShots.observe("datas", () -> COLUMN_OTHER_SHOTS.updateAsync(this, otherShots.get(), null, null));
		openedChests.observe("datas", () -> COLUMN_OPENED_CHESTS.updateAsync(this, openedChests.get(), null, null));
		playTime.observe("datas", () -> COLUMN_PLAY_TIME.updateAsync(this, playTime.get(), null, null));
		kitVIPTime.observe("datas", () -> COLUMN_KIT_VIP_TIME.updateAsync(this, kitVIPTime.get(), null, null));
		backVIPTime.observe("datas", () -> COLUMN_BACK_VIP_TIME.updateAsync(this, backVIPTime.get(), null, null));
		parameterAmbient.observe("datas", () -> COLUMN_PARAMETER_AMBIENT.updateAsync(this, parameterAmbient.get(), null, null));
		parameterBlood.observe("datas", () -> COLUMN_PARAMETER_BLOOD.updateAsync(this, parameterBlood.get(), null, null));
		parameterQuestsBoard.observe("datas", () -> COLUMN_PARAMETER_QUESTS_BOARD.updateAsync(this, parameterQuestsBoard.get(), null, null));
		parameterClanBoard.observe("datas", () -> COLUMN_PARAMETER_CLAN_BOARD.updateAsync(this, parameterClanBoard.get().ordinal(), null, null));
		
		if (OlympaZTA.getInstance().rankingMoney != null) money.observe("ranking", () -> OlympaZTA.getInstance().rankingMoney.handleNewScore(getName(), null, money.get()));
		if (OlympaZTA.getInstance().rankingKillZombie != null) killedZombies.observe("ranking", () -> OlympaZTA.getInstance().rankingKillZombie.handleNewScore(getName(), null, killedZombies.get()));
		if (OlympaZTA.getInstance().rankingKillPlayer != null) killedPlayers.observe("ranking", () -> OlympaZTA.getInstance().rankingKillPlayer.handleNewScore(getName(), null, killedPlayers.get()));
		if (OlympaZTA.getInstance().rankingLootChest != null) openedChests.observe("ranking", () -> OlympaZTA.getInstance().rankingLootChest.handleNewScore(getName(), null, openedChests.get()));
		
		joinTime = System.currentTimeMillis();
	}
	
	@Override
	public void unloaded() {
		super.unloaded();
		if (joinTime != 0) playTime.add(System.currentTimeMillis() - joinTime);
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
			killedZombies.set(resultSet.getInt("killed_zombies"));
			killedPlayers.set(resultSet.getInt("killed_players"));
			deaths.set(resultSet.getInt("deaths"));
			headshots.set(resultSet.getInt("headshots"));
			otherShots.set(resultSet.getInt("other_shots"));
			openedChests.set(resultSet.getInt("opened_chests"));
			kitVIPTime.set(resultSet.getLong("kit_vip_time"));
			backVIPTime.set(resultSet.getLong("back_vip_time"));
			parameterAmbient.set(resultSet.getBoolean("param_ambient"));
			parameterQuestsBoard.set(resultSet.getBoolean("param_quests_board"));
			parameterClanBoard.set(ClanBoardSetting.values()[resultSet.getInt("param_clan_board")]);
			
			plot.set(OlympaZTA.getInstance().plotsManager.getPlot(resultSet.getInt("plot"), true));
		}catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	public static OlympaPlayerZTA get(Player p) {
		return AccountProvider.get(p.getUniqueId());
	}
	
	@Override
	public TradeBag<? extends TradePlayerInterface> getTradeBag() {
		return null;// TODO
	}
	
}
