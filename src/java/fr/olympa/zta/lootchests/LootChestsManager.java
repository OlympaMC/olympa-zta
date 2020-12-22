package fr.olympa.zta.lootchests;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.persistence.PersistentDataType;

import fr.olympa.api.region.tracking.TrackedRegion;
import fr.olympa.api.sql.statement.OlympaStatement;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.lootchests.type.LootChestType;
import fr.olympa.zta.mobs.MobSpawning.SpawnType.SpawningFlag;

public class LootChestsManager implements Listener {

	public static final NamespacedKey LOOTCHEST = new NamespacedKey(OlympaZTA.getInstance(), "loot_chest_id");

	private final String tableName = "`zta_lootchests`";
	private final OlympaStatement createStatement = new OlympaStatement("INSERT INTO " + tableName + " (`world`, `x`, `y`, `z`, `loot_type`) VALUES (?, ?, ?, ?, ?)", true);
	private final OlympaStatement removeStatement = new OlympaStatement("DELETE FROM " + tableName + " WHERE (`id` = ?)");
	private final OlympaStatement updateLootStatement = new OlympaStatement("UPDATE " + tableName + " SET `loot_type` = ? WHERE (`id` = ?)");

	public final Map<Integer, LootChest> chests = new HashMap<>();
	private Random random = new Random();

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
				OlympaZTA.getInstance().sendMessage("§e" + chests.size() + "§7 coffres de loot chargés !");
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
			if (otherType == Type.LEFT) return (Chest) otherBlock.getState();
			return chest;
		}else {
			chestBlockData.setType(Type.SINGLE);
			chest.getBlock().setBlockData(chestBlockData);
			return chest;
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

	public LootChestType pickRandomChestType(Location location) {
		for (TrackedRegion region : OlympaCore.getInstance().getRegionManager().getApplicableRegions(location)) {
			SpawningFlag flag = region.getFlag(SpawningFlag.class);
			if (flag == null) continue;
			return flag.type.getLootChests().pick(random).get(0).getType();
		}
		return null;
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
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onBlockBreak(BlockBreakEvent e) {
		if (e.isCancelled()) return;
		if (e.getBlock().getType() != Material.CHEST) return;
		Chest chest = (Chest) e.getBlock().getState();
		if (chest.getPersistentDataContainer().has(LOOTCHEST, PersistentDataType.INTEGER)) {
			Integer id = chest.getPersistentDataContainer().get(LOOTCHEST, PersistentDataType.INTEGER);
			try {
				removeLootChest(id);
				Prefix.INFO.sendMessage(e.getPlayer(), "Tu as supprimé le coffre de loot %d.", id);
			}catch (Exception ex) {
				ex.printStackTrace();
				Prefix.ERROR.sendMessage(e.getPlayer(), "Une erreur est survenue lors de la suppresion du coffre de loot %d.", id);
			}
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onBlockPlace(BlockPlaceEvent e) {
		if (e.isCancelled()) return;
		if (e.getBlock().getWorld() != OlympaZTA.getInstance().mobSpawning.world) return;
		if (e.getBlock().getType() != Material.CHEST) return;
		Chest chest = (Chest) e.getBlock().getState();
		if (getLootChest(chest) != null) return;
		try {
			Location loc = chest.getLocation();
			LootChestType type = pickRandomChestType(loc);
			LootChest lootchest = createLootChest(loc, type);
			Prefix.DEFAULT_GOOD.sendMessage(e.getPlayer(), "Le coffre de loot a été créé ! ID: " + lootchest.getID() + ", type: " + type.getName());
		}catch (SQLException ex) {
			ex.printStackTrace();
			Prefix.DEFAULT_BAD.sendMessage(e.getPlayer(), "Une erreur est survenue lors de la création du coffre.");
		}
	}

}
