package fr.olympa.zta.lootchests;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;

import fr.olympa.api.sql.OlympaStatement;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.lootchests.type.LootChestType;

public class LootChestsManager implements Listener {

	public static final NamespacedKey LOOTCHEST = new NamespacedKey(OlympaZTA.getInstance(), "loot_chest_id");

	private final String tableName = "`zta_lootchests`";
	private final OlympaStatement createStatement = new OlympaStatement("INSERT INTO " + tableName + " (`world`, `x`, `y`, `z`, `loot_type`) VALUES (?, ?, ?, ?, ?)", true);
	private final OlympaStatement removeStatement = new OlympaStatement("DELETE FROM " + tableName + " WHERE (`id` = ?)");
	private final OlympaStatement updateLootStatement = new OlympaStatement("UPDATE " + tableName + " SET `loot_type` = ? WHERE (`id` = ?)");

	public final Map<Integer, LootChest> chests = new HashMap<>();

	public LootChestsManager() throws SQLException {
		OlympaCore.getInstance().getDatabase().createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " (" +
				"  `id` int(11) NOT NULL AUTO_INCREMENT," +
				"  `world` varchar(45) NOT NULL," +
				"  `x` int(11) NOT NULL," +
				"  `y` int(11) NOT NULL," +
				"  `z` int(11) NOT NULL," +
				"  `loot_type` varchar(45) NOT NULL," +
				"  PRIMARY KEY (`id`))");

		Bukkit.getScheduler().runTaskAsynchronously(OlympaZTA.getInstance(), () -> {
			try {
				ResultSet resultSet = OlympaCore.getInstance().getDatabase().createStatement().executeQuery("SELECT * FROM " + tableName);
				while (resultSet.next()) {
					try {
						int id = resultSet.getInt("id");
						chests.put(id, new LootChest(id, new Location(Bukkit.getWorld(resultSet.getString("world")), resultSet.getInt("x"), resultSet.getInt("y"), resultSet.getInt("z")), LootChestType.valueOf(resultSet.getString("loot_type"))));
					}catch (Exception ex) {
						ex.printStackTrace();
						OlympaZTA.getInstance().getLogger().severe("Impossible de charger le coffre de loot " + resultSet.getInt("id"));
						continue;
					}
				}
				OlympaZTA.getInstance().getLogger().info(chests.size() + " coffres de loot chargés !");
			}catch (SQLException e) {
				e.printStackTrace();
			}
		});

		new LootChestCommand(this).register();
	}

	public LootChest getLootChest(Chest chest) {
		if (chest.getPersistentDataContainer().has(LOOTCHEST, PersistentDataType.INTEGER)) return chests.get(chest.getPersistentDataContainer().get(LOOTCHEST, PersistentDataType.INTEGER));
		return null;
	}

	public synchronized LootChest createLootChest(Location location, LootChestType type) throws SQLException {
		PreparedStatement statement = createStatement.getStatement();
		statement.setString(1, location.getWorld().getName());
		statement.setInt(2, location.getBlockX());
		statement.setInt(3, location.getBlockY());
		statement.setInt(4, location.getBlockZ());
		statement.setString(5, type.name());
		statement.executeUpdate();
		ResultSet generatedKeys = statement.getGeneratedKeys();
		generatedKeys.next();
		LootChest chest = new LootChest(generatedKeys.getInt(1), location, type);
		chests.put(chest.getID(), chest);
		return chest;
	}

	public synchronized void removeLootChest(int id) throws SQLException {
		if (chests.remove(id) == null) throw new IllegalArgumentException("No lootchest with id " + id);
		PreparedStatement statement = removeStatement.getStatement();
		statement.setInt(1, id);
		statement.executeUpdate();
	}

	public synchronized void updateLootType(LootChest chest) throws SQLException {
		PreparedStatement statement = updateLootStatement.getStatement();
		statement.setString(1, chest.getLootType().name());
		statement.setInt(2, chest.getID());
		statement.executeUpdate();
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (e.getHand() != EquipmentSlot.HAND) return;

		Block block = e.getClickedBlock();
		Player player = e.getPlayer();
		if (block.getType() == Material.CHEST) {
			LootChest chest = getLootChest((Chest) block.getState());
			if (chest == null) return;

			e.setCancelled(true);
			chest.click(player);
		}else if (block.getType() == Material.ENDER_CHEST) {
			e.setCancelled(true);
			player.openInventory(OlympaPlayerZTA.get(player).getEnderChest());
		}
	}

}
