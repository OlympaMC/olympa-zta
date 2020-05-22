package fr.olympa.zta;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.utils.Prefix;

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
				p.teleport(to);
				p.sendMessage(message);
				if (run != null) run.run();
			}
		}.runTaskLater(OlympaZTA.getInstance(), TELEPORTATION_TICKS));
		Prefix.INFO.sendMessage(p, "Téléportation dans " + TELEPORTATION_SECONDS + " secondes...");
	}

}
