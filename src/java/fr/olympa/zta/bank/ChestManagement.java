package fr.olympa.zta.bank;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.core.spigot.OlympaCore;

public class ChestManagement {

	public static void init() throws SQLException {
		OlympaCore.getInstance().getDatabase().createStatement().executeUpdate(
				"CREATE TABLE IF NOT EXISTS `player_bank` (" +
						"  `player_id` BIGINT NOT NULL," +
						"  `slots` TINYINT(1) UNSIGNED NULL DEFAULT 9," +
						"  `content` JSON NULL," +
						"  PRIMARY KEY (`player_id`))");
	}

	private static PreparedStatement createBank;
	private static PreparedStatement getBank;
	private static PreparedStatement updateSize;
	private static PreparedStatement updateDatas;

	public static ResultSet createBank(OlympaPlayer player) throws SQLException {
		if (createBank == null || createBank.isClosed()) createBank = OlympaCore.getInstance().getDatabase().prepareStatement("INSERT INTO `player_bank` (`player_id`) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
		createBank.setLong(1, player.getId());
		createBank.executeUpdate();
		ResultSet result = createBank.getGeneratedKeys();
		if (!result.next()) throw new RuntimeException("The plugin has not been able to create a player account.");
		return result;
	}

	public static void updateSize(OlympaPlayer player, int newSize) throws SQLException {
		if (updateSize == null || updateSize.isClosed()) updateSize = OlympaCore.getInstance().getDatabase().prepareStatement("UPDATE `player_bank` SET `rows` = ? WHERE (`player_id` = ?)");
		updateDatas.setInt(1, newSize);
		updateSize.setLong(2, player.getId());
		updateSize.executeUpdate();
	}

	public static ChestGUI getBankGUI(OlympaPlayer player) throws SQLException, IOException {
		if (getBank == null || getBank.isClosed()) getBank = OlympaCore.getInstance().getDatabase().prepareStatement("SELECT * FROM `player_bank` WHERE (`player_id` = ?)");
		getBank.setLong(1, player.getId());
		ResultSet result = getBank.executeQuery();
		if (!result.next()) result = createBank(player);
		try {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(result.getString("content")));
			BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
			ItemStack[] items = new ItemStack[dataInput.readInt()];

			// Read the serialized inventory
			for (int i = 0; i < items.length; i++) {
				items[i] = (ItemStack) dataInput.readObject();
			}

			dataInput.close();
			return new ChestGUI(player, result.getInt("slots"), items);
		}catch (ClassNotFoundException e) {
			throw new IOException("Unable to decode class type.", e);
		}
	}

	public static void saveItems(OlympaPlayer player, ItemStack[] items) throws IOException, SQLException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

		dataOutput.writeInt(items.length);

		for (int i = 0; i < items.length; i++) {
			dataOutput.writeObject(items[i]);
		}

		dataOutput.close();
		String inventory = Base64Coder.encodeLines(outputStream.toByteArray());

		if (updateDatas == null || updateDatas.isClosed()) updateDatas = OlympaCore.getInstance().getDatabase().prepareStatement("");
		updateDatas.setLong(1, player.getId());
		updateDatas.setString(2, inventory);
		updateDatas.executeUpdate();
	}

}
