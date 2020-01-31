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
import fr.olympa.api.provider.OlympaPlayerObject;

public class OlympaPlayerZTA extends OlympaPlayerObject {

	static final Map<String, String> COLUMNS = ImmutableMap.<String, String>builder().put("bank_slots", "TINYINT(1) UNSIGNED NULL DEFAULT 9").put("bank_content", "VARBINARY NULL").build();

	private int bankSlots = 9;
	private ItemStack[] bankContent = new ItemStack[9];

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

	public void loadDatas(ResultSet resultSet) throws SQLException {
		bankSlots = resultSet.getInt("bank_slots");
		try {
			bankContent = ItemUtils.deserializeItemsArray(resultSet.getBytes("bank_content"));
		}catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	public void saveDatas(PreparedStatement statement) throws SQLException {
		statement.setInt(1, bankSlots);
		try {
			statement.setBytes(2, ItemUtils.serializeItemsArray(bankContent));
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

}
