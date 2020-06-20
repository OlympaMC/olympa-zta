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
import fr.olympa.zta.clans.ClanZTA;
import fr.olympa.zta.plots.PlayerPlot;

public class OlympaPlayerZTA extends OlympaPlayerObject implements ClanPlayerInterface<ClanZTA> {

	public static final int MAX_SLOTS = 27;
	static final Map<String, String> COLUMNS = ImmutableMap.<String, String>builder()
			.put("ender_chest", "VARBINARY(8000) NULL")
			.put("money", "DOUBLE NULL DEFAULT 0")
			.put("clan", "INT NOT NULL DEFAULT -1")
			.put("plot", "INT NOT NULL DEFAULT -1")
			.put("killedZombies", "INT NOT NULL DEFAULT 0")
			.put("killedPlayers", "INT NOT NULL DEFAULT 0")
			.put("deaths", "INT NOT NULL DEFAULT 0")
			.build();

	private Inventory enderChest = Bukkit.createInventory(null, 9, "Enderchest de " + getName());
	private OlympaMoney money = new OlympaMoney(0);
	private ClanZTA clan = null;
	private PlayerPlot plot = null;
	public BukkitTask plotFind = null; // pas persistant
	/* Stats */
	public int killedZombies = 0;
	public int killedPlayers = 0;
	public int deaths = 0;

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
			int clanID = resultSet.getInt("clan");
			if (clanID != -1) clan = OlympaZTA.getInstance().clansManager.getClan(clanID);
			plot = OlympaZTA.getInstance().plotsManager.getPlot(resultSet.getInt("plot"), true);
			killedZombies = resultSet.getInt("killedZombies");
			killedPlayers = resultSet.getInt("killedPlayers");
			deaths = resultSet.getInt("deaths");
		}catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	public void saveDatas(PreparedStatement statement) throws SQLException {
		try {
			statement.setBytes(1, ItemUtils.serializeItemsArray(enderChest.getContents()));
			statement.setDouble(2, money.get());
			statement.setInt(3, clan == null ? -1 : clan.getID());
			statement.setInt(4, plot == null ? -1 : plot.getID());
			statement.setInt(5, killedZombies);
			statement.setInt(6, killedPlayers);
			statement.setInt(7, deaths);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static OlympaPlayerZTA get(Player p) {
		return AccountProvider.get(p.getUniqueId());
	}
	
}
