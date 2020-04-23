package fr.olympa.zta.lootchests;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.sql.OlympaStatement;
import fr.olympa.api.utils.AbstractRandomizedPicker;
import fr.olympa.zta.lootchests.creators.LootCreator;
import fr.olympa.zta.registry.ItemStackable;
import fr.olympa.zta.registry.Registrable;
import fr.olympa.zta.registry.ZTARegistry;
import net.minecraft.server.v1_15_R1.Block;
import net.minecraft.server.v1_15_R1.BlockPosition;

public class LootChest extends AbstractRandomizedPicker<LootCreator> implements Registrable, InventoryHolder {

	public static final String TABLE_NAME = "`zta_lootchests`";

	private final int id;

	private Location location;
	private LootChestType type;
	private int minutesToWait = 8;
	private long nextOpen = 0;

	private Inventory inv;

	private BlockPosition nmsPosition;
	private Block nmsBlock;

	public LootChest(Location lc, int id, LootChestType type) {
		this.location = lc;
		this.id = id;

		this.nmsPosition = new BlockPosition(lc.getX(), lc.getY(), lc.getZ());
		this.nmsBlock = ((CraftBlock) lc.getBlock()).getNMS().getBlock();

		setLootType(type);
	}

	public void click(Player p) {
		long time = System.currentTimeMillis();
		if (time > nextOpen) {
			nextOpen = time + minutesToWait * 60000;
			inv.clear();
			for (LootCreator creator : pick()) {
				int slot;
				do {
					slot = random.nextInt(27);
				}while (inv.getItem(slot) != null);
				inv.setItem(slot, creator.create(p, super.random));
			}
		}

		p.openInventory(inv);
		updateChestState(inv.getViewers().size());
	}

	public void clearInventory() {
		for (ItemStack is : inv.getContents()) {
			if (is != null) {
				ItemStackable stackable = ZTARegistry.getItemStackable(is);
				if (stackable != null) ZTARegistry.removeObject(stackable);
			}
		}
		inv.clear();
	}

	public void updateChestState(int viewers) {
		((CraftWorld) location.getWorld()).getHandle().playBlockAction(nmsPosition, nmsBlock, 1, viewers);
	}

	public void resetTimer() {
		this.nextOpen = 0;
	}

	public void setTimer(int minutesToWait) {
		this.minutesToWait = minutesToWait;
		this.nextOpen = 0;
	}

	public void setLootType(LootChestType type) {
		this.type = type;
		inv = Bukkit.createInventory(this, 27, "Coffre " + type.getName());
	}

	public int getID() {
		return id;
	}

	public int getMinItems() {
		return 1;
	}

	public int getMaxItems() {
		return 4;
	}

	public List<LootCreator> getObjectList() {
		return type.getCreatorsSimple();
	}

	public List<LootCreator> getAlwaysObjectList() {
		return type.getCreatorsAlways();
	}

	public static LootChest getLootChest(Chest chest) {
		ItemStack item = chest.getInventory().getItem(0);
		if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return null;
		try {
			return ZTARegistry.getObject(Integer.parseInt(item.getItemMeta().getDisplayName()));
		}catch (NumberFormatException ex) {
			return null;
		}
	}

	public Inventory getInventory() {
		return inv;
	}

	private static OlympaStatement createStatement = new OlympaStatement("INSERT INTO " + TABLE_NAME + " (`id`, `world`, `x`, `y`, `z`, `loot_type`) VALUES (?, ?, ?, ?, ?, ?)");
	private static OlympaStatement updateStatement = new OlympaStatement("UPDATE " + TABLE_NAME + " SET "
			+ "`world` = ?, "
			+ "`x` = ?, "
			+ "`y` = ?, "
			+ "`z` = ?, "
			+ "`loot_type` = ? "
			+ "WHERE (`id` = ?)");

	public void createDatas() throws SQLException {
		PreparedStatement statement = createStatement.getStatement();
		statement.setInt(1, getID());
		statement.setString(2, location.getWorld().getName());
		statement.setInt(3, location.getBlockX());
		statement.setInt(4, location.getBlockY());
		statement.setInt(5, location.getBlockZ());
		statement.setString(6, LootChestType.chestTypes.inverse().get(type));
		statement.executeUpdate();
	}

	public synchronized void updateDatas() throws SQLException {
		PreparedStatement statement = updateStatement.getStatement();
		statement.setString(1, location.getWorld().getName());
		statement.setInt(2, location.getBlockX());
		statement.setInt(3, location.getBlockY());
		statement.setInt(4, location.getBlockZ());
		statement.setString(5, LootChestType.chestTypes.inverse().get(type));
		statement.setInt(6, getID());
		statement.executeUpdate();
	}

	public static final String CREATE_TABLE_STATEMENT = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
			"  `id` INT NOT NULL," +
			"  `world` VARCHAR(45) NULL," +
			"  `x` INT NOT NULL," +
			"  `y` INT NOT NULL," +
			"  `z` INT NOT NULL," +
			"  `loot_type` VARCHAR(45) NOT NULL," +
			"  PRIMARY KEY (`id`))";

	public static LootChest deserializeLootChest(ResultSet set, int id, Class<?> clazz) throws Exception {
		LootChest chest = new LootChest(new Location(Bukkit.getWorld(set.getString("world")), set.getInt("x"), set.getInt("y"), set.getInt("z")), id, LootChestType.chestTypes.get(set.getString("loot_type")));
		return chest;
	}

}
