package fr.olympa.zta.lootchests;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;

public class ChestsListener implements Listener{
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e){
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (e.getHand() != EquipmentSlot.HAND) return;
		
		Block block = e.getClickedBlock();
		if (block.getType() != Material.CHEST) return;

		LootChest chest = LootChest.getLootChest((Chest) block.getState());
		if (chest == null) return;

		e.setCancelled(true);
		chest.click(e.getPlayer());
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		Inventory inv = e.getInventory();
		if (!(inv.getHolder() instanceof LootChest)) return;

		LootChest chest = (LootChest) inv.getHolder();
		chest.updateChestState();
	}

}
