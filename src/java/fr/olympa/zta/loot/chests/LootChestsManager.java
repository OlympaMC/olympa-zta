package fr.olympa.zta.loot.chests;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

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
import fr.olympa.api.sql.SQLColumn;
import fr.olympa.api.sql.SQLTable;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.loot.chests.type.LootChestType;
import fr.olympa.zta.mobs.MobSpawning.SpawnType.SpawningFlag;

public class LootChestsManager implements Listener {

	public static final NamespacedKey LOOTCHEST = new NamespacedKey(OlympaZTA.getInstance(), "loot_chest_id");
	
	private SQLTable<LootChest> table;
	
	public SQLColumn<LootChest> columnID = new SQLColumn<LootChest>("id", "int(11) NOT NULL AUTO_INCREMENT", Types.INTEGER).setPrimaryKey(LootChest::getID);
	public SQLColumn<LootChest> columnWorld = new SQLColumn<LootChest>("world", "varchar(45) NOT NULL", Types.VARCHAR);
	public SQLColumn<LootChest> columnX = new SQLColumn<LootChest>("x", "int(11) NOT NULL", Types.INTEGER);
	public SQLColumn<LootChest> columnY = new SQLColumn<LootChest>("y", "int(11) NOT NULL", Types.INTEGER);
	public SQLColumn<LootChest> columnZ = new SQLColumn<LootChest>("z", "int(11) NOT NULL", Types.INTEGER);
	public SQLColumn<LootChest> columnLootType = new SQLColumn<LootChest>("loot_type", "varchar(45) NOT NULL", Types.VARCHAR).setUpdatable();

	public final Map<Integer, LootChest> chests = new HashMap<>();
	private Random random = new Random();

	public LootChestsManager() throws SQLException {
		table = new SQLTable<>("zta_lootchests",
				Arrays.asList(columnID, columnWorld, columnX, columnY, columnZ, columnLootType),
				resultSet -> new LootChest(resultSet.getInt("id"), new Location(Bukkit.getWorld(resultSet.getString("world")), resultSet.getInt("x"), resultSet.getInt("y"), resultSet.getInt("z")), LootChestType.valueOf(resultSet.getString("loot_type").toUpperCase())));
		table.createOrAlter();

		Bukkit.getScheduler().runTaskAsynchronously(OlympaZTA.getInstance(), () -> {
			try {
				chests.putAll(table.selectAll().stream().collect(Collectors.toMap(LootChest::getID, x -> x)));
				OlympaZTA.getInstance().sendMessage("§e%d§7 coffres de loot chargés !", chests.size());
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

		ResultSet generatedKeys = table.insert(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), type.name());
		generatedKeys.next();
		LootChest chest = new LootChest(generatedKeys.getInt(1), location, type);
		chest.register(chestState);
		chests.put(chest.getID(), chest);
		return chest;
	}

	public synchronized void removeLootChest(int id) throws SQLException {
		LootChest chest = chests.remove(id);
		if (chest == null) throw new IllegalArgumentException("No lootchest with id " + id);
		table.delete(chest);
		chest.unregister((Chest) chest.getLocation().getBlock().getState());
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
				if (chests.containsKey(id)) {
					removeLootChest(id);
					Prefix.INFO.sendMessage(e.getPlayer(), "Tu as supprimé le coffre de loot %d.", id);
				}else {
					Prefix.DEFAULT_BAD.sendMessage(e.getPlayer(), "Le coffre portait l'ID %d qui n'existe plus dans la base de donnée.", id);
				}
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
