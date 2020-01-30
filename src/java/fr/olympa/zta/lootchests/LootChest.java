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

import fr.olympa.api.utils.AbstractRandomizedPicker;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.lootchests.creators.LootCreator;
import fr.olympa.zta.registry.Registrable;
import fr.olympa.zta.registry.ZTARegistry;
import net.minecraft.server.v1_15_R1.Block;
import net.minecraft.server.v1_15_R1.BlockPosition;

public class LootChest extends AbstractRandomizedPicker<LootCreator> implements Registrable, InventoryHolder {

	private final int id;

	private Location location;
	private LootChestType type = null;
	private int minutesToWait = 8;
	private long nextOpen = 0;

	private Inventory inv;

	private BlockPosition nmsPosition;
	private Block nmsBlock;

	public LootChest(Location lc, int id) {
		this.location = lc;
		this.id = id;

		this.nmsPosition = new BlockPosition(lc.getX(), lc.getY(), lc.getZ());
		this.nmsBlock = ((CraftBlock) lc.getBlock()).getNMS().getBlock();
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

	public void updateChestState(int viewers) {
		((CraftWorld) location.getWorld()).getHandle().playBlockAction(nmsPosition, nmsBlock, 1, viewers);
	}

	public void resetTimer() {
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
			return (LootChest) ZTARegistry.getObject(Integer.parseInt(item.getItemMeta().getDisplayName()));
		}catch (NumberFormatException ex) {
			return null;
		}
	}

	public Inventory getInventory() {
		return inv;
	}

	private static PreparedStatement createStatement;
	private static PreparedStatement updateStatement;

	public void createDatas() throws SQLException {
		if (createStatement == null || createStatement.isClosed()) createStatement = OlympaCore.getInstance().getDatabase().prepareStatement("INSERT INTO `chests` (`id`, `world`, `x`, `y`, `z`, `loot_type`) VALUES (?, ?, ?, ?, ?, ?)");
		createStatement.setInt(1, getID());
		createStatement.setString(2, location.getWorld().getName());
		createStatement.setInt(3, location.getBlockX());
		createStatement.setInt(4, location.getBlockY());
		createStatement.setInt(5, location.getBlockZ());
		createStatement.setString(6, LootChestType.chestTypes.inverse().get(type));
		createStatement.executeUpdate();
	}

	public synchronized void updateDatas() throws SQLException {
		if (updateStatement == null || updateStatement.isClosed()) updateStatement = OlympaCore.getInstance().getDatabase().prepareStatement("UPDATE `chests` SET "
				+ "`world` = ?, "
				+ "`x` = ?, "
				+ "`y` = ?, "
				+ "`z` = ?, "
				+ "`loot_type` = ?, "
				+ "WHERE (`id` = ?)");
		updateStatement.setString(1, location.getWorld().getName());
		updateStatement.setInt(2, location.getBlockX());
		updateStatement.setInt(3, location.getBlockY());
		updateStatement.setInt(4, location.getBlockZ());
		updateStatement.setString(5, LootChestType.chestTypes.inverse().get(type));
		updateStatement.executeUpdate();
	}

	public static final String CREATE_TABLE_STATEMENT = "CREATE TABLE IF NOT EXISTS `zta`.`chests` (" +
			"  `id` INT NOT NULL," +
			"  `world` VARCHAR(45) NULL," +
			"  `x` INT NOT NULL," +
			"  `y` INT NOT NULL," +
			"  `z` INT NOT NULL," +
			"  `loot_type` VARCHAR(45) NOT NULL," +
			"  PRIMARY KEY (`id`))";

	public static LootChest deserializeGun(ResultSet set, int id, Class<?> clazz) {
		try {
			LootChest chest = new LootChest(new Location(Bukkit.getWorld(set.getString("world")), set.getInt("x"), set.getInt("y"), set.getInt("z")), id);
			chest.setLootType(LootChestType.chestTypes.get(set.getString("loot_type")));
			return chest;
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
