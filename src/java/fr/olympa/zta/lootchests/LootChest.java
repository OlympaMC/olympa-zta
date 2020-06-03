package fr.olympa.zta.lootchests;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.utils.AbstractRandomizedPicker;
import fr.olympa.zta.lootchests.creators.LootCreator;
import fr.olympa.zta.lootchests.creators.LootCreator.Loot;
import fr.olympa.zta.lootchests.type.LootChestType;
import fr.olympa.zta.utils.DynmapLink;
import net.minecraft.server.v1_15_R1.Block;
import net.minecraft.server.v1_15_R1.BlockPosition;

public class LootChest extends OlympaGUI implements AbstractRandomizedPicker<LootCreator> {

	private final int id;

	private Location location;
	private LootChestType type;
	private int minutesToWait = 8;
	private long nextOpen = 0;

	private Map<Integer, Loot> currentLoots = new HashMap<>();

	private Random random = new Random();

	private BlockPosition nmsPosition;
	private Block nmsBlock;

	public LootChest(int id, Location lc, LootChestType type) {
		super("Coffre " + type.getName(), InventoryType.CHEST);
		this.id = id;
		this.location = lc;

		this.nmsPosition = new BlockPosition(lc.getX(), lc.getY(), lc.getZ());
		this.nmsBlock = ((CraftBlock) lc.getBlock()).getNMS().getBlock();

		setLootType(type);
	}

	public void click(Player p) {
		long time = System.currentTimeMillis();
		if (time > nextOpen) {
			nextOpen = time + minutesToWait * 60000;
			clearInventory();
			for (LootCreator creator : pick(random)) {
				int slot;
				do {
					slot = random.nextInt(27);
				}while (inv.getItem(slot) != null);

				Loot loot = creator.create(p, random);
				currentLoots.put(slot, loot);
				inv.setItem(slot, loot.getItem());
			}
		}

		super.create(p);
		updateChestState(inv.getViewers().size());
	}

	public void clearInventory() {
		currentLoots.values().forEach(Loot::onRemove);
		currentLoots.clear();
		inv.clear();
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		Loot loot = currentLoots.remove(slot);
		if (loot == null) throw new RuntimeException("No loot at slot for chest " + getID());
		return loot.onTake(p, inv, slot);
	}

	@Override
	public boolean onClose(Player p) {
		updateChestState(inv.getViewers().size() - 1);
		return true;
	}

	public void updateChestState(int viewers) {
		((CraftWorld) location.getWorld()).getHandle().playBlockAction(nmsPosition, nmsBlock, 1, viewers);
	}

	public void register(Chest chest) {
		chest.getPersistentDataContainer().set(LootChestsManager.LOOTCHEST, PersistentDataType.INTEGER, id);
		chest.getInventory().clear();
	}

	public void resetTimer() {
		this.nextOpen = 0;
	}

	public void setTimer(int minutesToWait) {
		this.minutesToWait = minutesToWait;
		this.nextOpen = 0;
	}

	public LootChestType getLootType() {
		return type;
	}

	public void setLootType(LootChestType type) {
		this.type = type;
		DynmapLink.showChest(this);
		clearInventory();
		inv = Bukkit.createInventory(this, 27, "Coffre " + type.getName());
	}

	public int getID() {
		return id;
	}

	public Location getLocation() {
		return location;
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

	public Inventory getInventory() {
		return inv;
	}

}
