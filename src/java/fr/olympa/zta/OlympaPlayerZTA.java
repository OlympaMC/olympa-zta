package fr.olympa.zta;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
			.put("bank_slots", "TINYINT(1) UNSIGNED NULL DEFAULT 9")
			.put("bank_content", "VARBINARY(8000) NULL")
			.put("ender_chest", "VARBINARY(8000) NULL")
			.put("money", "DOUBLE NULL DEFAULT 0")
			.put("clan", "INT NULL DEFAULT NULL")
			.put("plot", "INT NOT NULL DEFAULT -1")
			.build();

	private int bankSlots = 9;
	private ItemStack[] bankContent = new ItemStack[MAX_SLOTS];

	private ItemStack[] enderChest = new ItemStack[9];

	private OlympaMoney money = new OlympaMoney(0);

	private ClanZTA clan = null;

	private PlayerPlot plot = null;
	public BukkitTask plotFind = null; // pas persistant

	public OlympaPlayerZTA(UUID uuid, String name, String ip) {
		super(uuid, name, ip);
		money.observe(() -> OlympaZTA.getInstance().lineMoney.updatePlayer(this));
	}

	public int getBankSlots() {
		return bankSlots;
	}

	public void incrementBankSlots() {
		bankSlots++;
	}

	public ItemStack[] getBankContent() {
		return bankContent;
	}

	public void setBankContent(ItemStack[] items) {
		this.bankContent = items;
	}

	public ItemStack[] getEnderChest() {
		return enderChest;
	}

	public void setEnderChest(ItemStack[] enderChest) {
		this.enderChest = enderChest;
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
			bankSlots = resultSet.getInt("bank_slots");
			bankContent = ItemUtils.deserializeItemsArray(resultSet.getBytes("bank_content"));
			enderChest = ItemUtils.deserializeItemsArray(resultSet.getBytes("ender_chest"));
			money.set(resultSet.getDouble("money"));
			clan = OlympaZTA.getInstance().clansManager.getClan(resultSet.getInt("clan"));
			plot = OlympaZTA.getInstance().plotsManager.getPlot(resultSet.getInt("plot"), true);
		}catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	public void saveDatas(PreparedStatement statement) throws SQLException {
		try {
			statement.setInt(1, bankSlots);
			statement.setBytes(2, ItemUtils.serializeItemsArray(bankContent));
			statement.setBytes(3, ItemUtils.serializeItemsArray(enderChest));
			statement.setDouble(4, money.get());
			if (clan == null) {
				statement.setNull(5, Types.INTEGER);
			}else {
				statement.setInt(5, clan.getID());
			}
			statement.setInt(6, plot == null ? -1 : plot.getID());
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static OlympaPlayerZTA get(Player p) {
		return AccountProvider.get(p.getUniqueId());
	}
	
}
