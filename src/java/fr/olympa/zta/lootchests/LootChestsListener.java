package fr.olympa.zta.lootchests;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import fr.olympa.zta.OlympaPlayerZTA;

public class LootChestsListener implements Listener{
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e){
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (e.getHand() != EquipmentSlot.HAND) return;
		
		Block block = e.getClickedBlock();
		Player player = e.getPlayer();
		if (block.getType() == Material.CHEST) {
			LootChest chest = LootChest.getLootChest((Chest) block.getState());
			if (chest == null) return;

			e.setCancelled(true);
			chest.click(player);
		}else if (block.getType() == Material.ENDER_CHEST) {
			e.setCancelled(true);
			player.openInventory(OlympaPlayerZTA.get(player).getEnderChest());
		}
	}

}
