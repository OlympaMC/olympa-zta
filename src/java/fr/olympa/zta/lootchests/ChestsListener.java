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
		if (!block.hasMetadata("lootchest")) return;

		LootChest chest = (LootChest) ZTARegistry.getObject(block.getMetadata("lootchest").get(0).asInt());
		chest.click(e.getPlayer());
	}
	
}
