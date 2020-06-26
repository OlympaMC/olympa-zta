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
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
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
		chest = getLeftChest(chest);
		if (chest.getPersistentDataContainer().has(LOOTCHEST, PersistentDataType.INTEGER)) return chests.get(chest.getPersistentDataContainer().get(LOOTCHEST, PersistentDataType.INTEGER));
		return null;
	}

	Chest getLeftChest(Chest chest) {
		while (true) {
			InventoryHolder holder = chest.getInventory().getHolder();
			if (holder instanceof DoubleChest) return (Chest) ((DoubleChest) holder).getLeftSide();
			
			org.bukkit.block.data.type.Chest chestBlockData = (org.bukkit.block.data.type.Chest) chest.getBlock().getBlockData();
			Type type = chestBlockData.getType();
			if (type == Type.SINGLE) return chest;
			
			BlockFace face = chestBlockData.getFacing();
			BlockFace relative;
			if (face == BlockFace.EAST) {
				relative = type == Type.LEFT ? BlockFace.SOUTH : BlockFace.NORTH;
			}else if (face == BlockFace.SOUTH) {
				relative = type == Type.LEFT ? BlockFace.WEST : BlockFace.EAST;
			}else if (face == BlockFace.WEST) {
				relative = type == Type.LEFT ? BlockFace.NORTH : BlockFace.SOUTH;
			}else {
				relative = type == Type.LEFT ? BlockFace.EAST : BlockFace.WEST;
			}
			Block otherBlock = chest.getBlock().getRelative(relative);
			if (otherBlock.getType() == Material.CHEST) {
				org.bukkit.block.data.type.Chest otherData = (org.bukkit.block.data.type.Chest) otherBlock.getBlockData();
				otherData.setFacing(face);
				Type otherType = type == Type.LEFT ? Type.RIGHT : Type.LEFT;
				otherData.setType(otherType);
				otherBlock.setBlockData(otherData);
				chest = (Chest) otherBlock.getState();
				continue;
			}else {
				chestBlockData.setType(Type.SINGLE);
				chest.getBlock().setBlockData(chestBlockData);
				return chest;
			}
		}
	}

	public synchronized LootChest createLootChest(Location location, LootChestType type) throws SQLException {
		Chest chestState = getLeftChest((Chest) location.getBlock().getState());
		location = chestState.getLocation();

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
		chest.register(chestState);
		chests.put(chest.getID(), chest);
		return chest;
	}

	public synchronized void removeLootChest(int id) throws SQLException {
		LootChest chest = chests.remove(id);
		if (chest == null) throw new IllegalArgumentException("No lootchest with id " + id);
		PreparedStatement statement = removeStatement.getStatement();
		statement.setInt(1, id);
		statement.executeUpdate();
		chest.unregister((Chest) chest.getLocation().getBlock().getState());
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
