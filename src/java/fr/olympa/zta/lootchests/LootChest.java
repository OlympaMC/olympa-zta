package fr.olympa.zta.lootchests;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.utils.AbstractRandomizedPicker;
import fr.olympa.zta.lootchests.creators.LootCreator;
import fr.olympa.zta.registry.Registrable;
import fr.olympa.zta.registry.ZTARegistry;
import net.minecraft.server.v1_13_R2.Block;
import net.minecraft.server.v1_13_R2.BlockPosition;

public class LootChest extends AbstractRandomizedPicker<LootCreator> implements Registrable, InventoryHolder {

	private final int id = super.random.nextInt();

	private Location location;
	private LootChestType type = null;
	private int minutesToWait = 8;
	private long nextOpen = 0;

	private Inventory inv;

	private BlockPosition nmsPosition;
	private Block nmsBlock;

	public LootChest(Location lc) {
		this.location = lc;

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
		updateChestState();
	}

	public void updateChestState() {
		((CraftWorld) location.getWorld()).getHandle().playBlockAction(nmsPosition, nmsBlock, 1, inv.getViewers().size());
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

}
