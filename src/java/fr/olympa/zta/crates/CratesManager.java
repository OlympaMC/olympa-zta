package fr.olympa.zta.crates;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class CratesManager implements Listener {
	
	private List<Crate> running = new ArrayList<>();
	
	public CratesManager() {
		new CratesCommand(this).register();
	}
	
	public void spawnCrate(Location location, CrateType type) {
		Crate crate = new Crate(location, type);
		running.add(crate);
		crate.startFalling();
	}
	
	public void unloadCrate(Crate crate) {
		running.remove(crate);
		crate.cancel();
	}
	
	public void unload() {
		running.forEach(Crate::cancel);
		running.clear();
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.TRAPPED_CHEST) {
			running.stream().filter(x -> e.getClickedBlock().equals(x.block)).findAny().ifPresent(crate -> {
				e.setCancelled(true);
				crate.click(e.getPlayer());
			});
		}
	}
	
}
