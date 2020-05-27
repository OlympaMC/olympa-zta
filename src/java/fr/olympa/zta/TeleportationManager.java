package fr.olympa.zta;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.spigot.SpigotUtils;

public class TeleportationManager implements Listener {

	public static final int TELEPORTATION_SECONDS = 3;
	public static final int TELEPORTATION_TICKS = TELEPORTATION_SECONDS * 20;

	private Map<Player, BukkitTask> teleportations = new HashMap<>();

	public void teleport(Player p, Location to, String message) {
		teleport(p, to, message, null);
	}

	public void teleport(Player p, Location to, String message, Runnable run) {
		BukkitTask removed = teleportations.remove(p);
		if (removed != null) {
			removed.cancel();
			Prefix.INFO.sendMessage(p, "La téléportation précédente a été annulée.");
		}
		teleportations.put(p, new BukkitRunnable() {
			@Override
			public void run() {
				teleportations.remove(p);
				p.teleport(to);
				p.sendMessage(message);
				if (run != null) run.run();
			}
		}.runTaskLater(OlympaZTA.getInstance(), TELEPORTATION_TICKS));
		Prefix.INFO.sendMessage(p, "Téléportation dans " + TELEPORTATION_SECONDS + " secondes...");
	}

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if (!SpigotUtils.isSameLocation(e.getFrom(), e.getTo())) {
			BukkitTask task = teleportations.remove(e.getPlayer());
			if (task != null) {
				task.cancel();
				Prefix.BAD.sendMessage(e.getPlayer(), "La téléportation a été annulée.");
			}
		}
	}

}
