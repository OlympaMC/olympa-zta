package fr.olympa.zta;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableMap;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.objects.OlympaMoney;
import fr.olympa.api.objects.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.provider.OlympaPlayerObject;
import fr.olympa.api.sql.OlympaStatement;
import fr.olympa.zta.clans.Clan;
import fr.olympa.zta.registry.ZTARegistry;

public class OlympaPlayerZTA extends OlympaPlayerObject {

	public static final int MAX_SLOTS = 27;
	static final Map<String, String> COLUMNS = ImmutableMap.<String, String>builder().put("bank_slots", "TINYINT(1) UNSIGNED NULL DEFAULT 9").put("bank_content", "VARBINARY(8000) NULL").put("ender_chest", "VARBINARY(8000) NULL").put("money", "DOUBLE NULL DEFAULT 0").put("clan", "INT NULL DEFAULT NULL").build();

	private int bankSlots = 9;
	private ItemStack[] bankContent = new ItemStack[MAX_SLOTS];

	private ItemStack[] enderChest = new ItemStack[9];

	private OlympaMoney money = new OlympaMoney(0);

	private Clan clan = null;

	public OlympaPlayerZTA(UUID uuid, String name, String ip) {
		super(uuid, name, ip);
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

	public OlympaMoney getGameMoney() {
		return money;
	}

	public Clan getClan() {
		return clan;
	}

	public void setClan(Clan clan) {
		this.clan = clan;
	}

	public void loadDatas(ResultSet resultSet) throws SQLException {
		try {
			bankSlots = resultSet.getInt("bank_slots");
			bankContent = ItemUtils.deserializeItemsArray(resultSet.getBytes("bank_content"));
			enderChest = ItemUtils.deserializeItemsArray(resultSet.getBytes("ender_chest"));
			money.set(resultSet.getDouble("money"));
			clan = ZTARegistry.getObject(resultSet.getInt("clan"));
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
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static OlympaStatement removeClan = new OlympaStatement("UPDATE `zta_players` SET `clan` = NULL WHERE (`player_id` = ?)");
	public static void removePlayerClan(OlympaPlayerInformations pinfo) {
		try {
			PreparedStatement statement = removeClan.getStatement();
			statement.setLong(1, pinfo.getID());
			statement.executeUpdate();
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static OlympaStatement getPlayersByClan = new OlympaStatement("SELECT `player_id` FROM `zta_players` WHERE (`clan` = ?)");
	public static List<OlympaPlayerInformations> getPlayersByClan(Clan clan) {
		try {
			List<OlympaPlayerInformations> players = new ArrayList<>();
			PreparedStatement statement = getPlayersByClan.getStatement();
			statement.setLong(1, clan.getID());
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				players.add(AccountProvider.getPlayerInformations(resultSet.getLong("player_id")));
			}
			return players;
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
