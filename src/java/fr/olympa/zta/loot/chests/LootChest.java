package fr.olympa.zta.loot.chests;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataType;

import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.loot.RandomizedInventory;
import fr.olympa.zta.loot.chests.type.LootChestType;
import fr.olympa.zta.loot.creators.LootCreator;
import fr.olympa.zta.utils.map.DynmapLink;
import net.minecraft.server.v1_16_R3.Block;
import net.minecraft.server.v1_16_R3.BlockPosition;

public class LootChest extends RandomizedInventory {

	private final int id;

	private Location location;
	private LootChestType type;
	private int waitMin = 6 * 60000, waitMax = 8 * 60000; // 60'000ticks = 1min
	private long nextOpen = 0;

	private Random random = new Random();

	private BlockPosition nmsPosition;
	private Block nmsBlock;

	public LootChest(int id, Location lc, LootChestType type) {
		super("Coffre " + type.getName(), InventoryType.CHEST);
		this.id = id;
		this.location = lc;

		this.nmsPosition = new BlockPosition(lc.getX(), lc.getY(), lc.getZ());

		setLootType(type, false);
	}

	public void click(Player p) {
		long time = System.currentTimeMillis();
		if (time > nextOpen) {
			OlympaPlayerZTA.get(p).openedChests.increment();
			nextOpen = time + Utils.getRandomAmount(random, waitMin, waitMax);
			fillInventory();
		}else Prefix.DEFAULT.sendMessage(p, "§oCe coffre a déjà été ouvert récemment...");
		
		super.create(p);
		updateChestState(inv.getViewers().size(), true);
	}

	@Override
	public boolean onClose(Player p) {
		updateChestState(inv.getViewers().size() - 1, false);
		return true;
	}

	public void updateChestState(int viewers, boolean open) {
		if (nmsBlock == null) nmsBlock = ((CraftBlock) location.getBlock()).getNMS().getBlock();
		Sound sound = null;
		if (viewers == 0) sound = Sound.BLOCK_CHEST_CLOSE;
		if (viewers == 1 && open) sound = Sound.BLOCK_CHEST_OPEN;
		if (sound != null) location.getWorld().playSound(location, sound, SoundCategory.BLOCKS, 1, 1);
		((CraftWorld) location.getWorld()).getHandle().playBlockAction(nmsPosition, nmsBlock, 1, viewers);
	}

	public void register(Chest chest) {
		chest.getPersistentDataContainer().set(LootChestsManager.LOOTCHEST, PersistentDataType.INTEGER, id);
		chest.getInventory().clear();
		chest.update();
	}

	public void unregister(Chest chest) {
		chest.getPersistentDataContainer().remove(LootChestsManager.LOOTCHEST);
		chest.update();
		DynmapLink.hideChest(this);
	}

	public void resetTimer() {
		this.nextOpen = 0;
	}

	public void setTimer(int minutesToWait) {
		this.waitMin = this.waitMax = 0;
		this.nextOpen = 0;
	}

	public LootChestType getLootType() {
		return type;
	}

	public void setLootType(LootChestType type, boolean update) {
		this.type = type;
		DynmapLink.showChest(this);
		clearInventory();
		inv = Bukkit.createInventory(this, 27, "Coffre " + type.getName());
		if (update) OlympaZTA.getInstance().lootChestsManager.columnLootType.updateAsync(this, type.getName(), null, null);
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
		return 3;
	}

	public List<LootCreator> getObjectList() {
		return type.getCreatorsSimple();
	}

	public List<LootCreator> getAlwaysObjectList() {
		return type.getCreatorsAlways();
	}

	@Override
	public Inventory getInventory() {
		return inv;
	}

}
