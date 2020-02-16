package fr.olympa.zta;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableMap;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.objects.OlympaMoney;
import fr.olympa.api.provider.OlympaPlayerObject;

public class OlympaPlayerZTA extends OlympaPlayerObject {

	public static final int MAX_SLOTS = 27;
	static final Map<String, String> COLUMNS = ImmutableMap.<String, String>builder().put("bank_slots", "TINYINT(1) UNSIGNED NULL DEFAULT 9").put("bank_content", "VARBINARY(10) NULL").put("ender_chest", "VARBINARY(10) NULL").put("money", "INT(2) NULL DEFAULT 0").build();

	private int bankSlots = 9;
	private ItemStack[] bankContent = new ItemStack[MAX_SLOTS];

	private ItemStack[] enderChest = new ItemStack[9];

	private OlympaMoney money = new OlympaMoney(0);

	public OlympaPlayerZTA(UUID uuid, String name, String ip) {
		super(uuid, name, ip);
		System.out.println("OlympaPlayerZTA.OlympaPlayerZTA()");
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
		System.out.println("OlympaPlayerZTA.getGameMoney()");
		return money;
	}

	public void loadDatas(ResultSet resultSet) throws SQLException {
		System.out.println("OlympaPlayerZTA.loadDatas()");
		try {
			bankSlots = resultSet.getInt("bank_slots");
			bankContent = ItemUtils.deserializeItemsArray(resultSet.getBytes("bank_content"));
			enderChest = ItemUtils.deserializeItemsArray(resultSet.getBytes("ender_chest"));
			money.give(resultSet.getDouble("money"));
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
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

}
