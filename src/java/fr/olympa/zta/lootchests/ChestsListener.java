package fr.olympa.zta.lootchests;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import fr.olympa.zta.registry.ZTARegistry;

public class ChestsListener implements Listener{
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e){
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (e.getHand() != EquipmentSlot.HAND) return;
		
		Block block = e.getClickedBlock();

		LootChest chest = (LootChest) ZTARegistry.getObject(block);
		if (chest == null) return;

		chest.click(e.getPlayer());
		e.setCancelled(true);
	}
	
}
