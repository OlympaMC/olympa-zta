package fr.olympa.zta.tyrolienne;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;

import fr.olympa.api.region.shapes.Cuboid;
import fr.olympa.api.region.tracking.ActionResult;
import fr.olympa.api.region.tracking.TrackedRegion;
import fr.olympa.api.region.tracking.flags.Flag;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Tyrolienne implements Listener {
	
	private final double speed = 0.69;
	
	private final Location from;
	private final Location to;
	
	private final Vector direction;
	
	private Map<Player, TyrolienneRunner> players = new HashMap<>();
	
	public Tyrolienne(Location from, Location to) {
		this.from = from;
		this.to = to;
		
		direction = to.toVector().subtract(from.toVector());
		
		OlympaCore.getInstance().getRegionManager().registerRegion(new Cuboid(from.clone().subtract(0, 2, 0), to), "tyrolienne" + hashCode(), EventPriority.NORMAL, new Flag() {
			@Override
			public ActionResult enters(Player p, Set<TrackedRegion> to) {
				if (!players.containsKey(p)) start(p);
				return ActionResult.ALLOW;
			}
		});
	}
	
	@EventHandler
	public void onDismount(EntityDismountEvent e) {
		TyrolienneRunner runner = players.get(e.getEntity());
		if (runner != null) runner.stop(true);
	}
	
	public void start(Player p) {
		Vector step;
		Location point;
		Location ploc = p.getLocation();
		float angle = Math.abs(ploc.getDirection().angle(direction));
		if (angle < Math.PI / 6) {
			step = direction.clone().normalize().multiply(speed);
			point = to;
		}else if (angle > 5 * Math.PI / 6) {
			step = direction.clone().normalize().multiply(-speed);
			point = from;
		}else return;
		ploc.add(ploc.getDirection());
		if (ploc.distanceSquared(point) < 20) return;
		
		double a = direction.getX();
		double b = direction.getY();
		double c = direction.getZ();
		double x1 = from.getX();
		double y1 = from.getY();
		double z1 = from.getZ();
		double t = -(a * (x1 - ploc.getX()) + b * (y1 - ploc.getY()) + c * (z1 - ploc.getZ())) / (a * a + b * b + c * c);
		Location loc = new Location(p.getWorld(), x1 + a * t + 0.5, y1 + b * t, z1 + c * t + 0.5);
		
		players.put(p, new TyrolienneRunner(p, loc.subtract(0, 3, 0).setDirection(step), step, point));
	}
	
	public void unload() {
		new ArrayList<>(players.values()).forEach(x -> x.stop(false));
	}
	
	private class TyrolienneRunner {
		private Player p;
		private Vector step;
		
		private ArmorStand stand;
		private ArmorStand headBottom, headTop;
		private BukkitTask task;
		
		private boolean wasAllowFlight;
		
		private int i = 0;
		
		public TyrolienneRunner(Player p, Location standLocation, Vector step, Location point) {
			this.p = p;
			this.step = step;
			
			wasAllowFlight = p.getAllowFlight();
			p.setAllowFlight(true);
			
			stand = p.getWorld().spawn(standLocation, ArmorStand.class, x -> {
				/*x.setMarker(true); Ca empêche la vélocité
				x.setGravity(false);*/
				x.setInvisible(true);
				x.setPersistent(false);
			});
			stand.addPassenger(p);
			
			headBottom = p.getWorld().spawn(standLocation, ArmorStand.class, x -> {
				x.setInvisible(true);
				x.setPersistent(false);
				//x.setMarker(true); Fait foirer les passengers vu qu'il y a pas de boîte de collision
				x.setSmall(true);
			});
			stand.addPassenger(headBottom);
			
			headTop = p.getWorld().spawn(standLocation, ArmorStand.class, x -> {
				x.setInvisible(true);
				x.setPersistent(false);
				x.setMarker(true);
				x.setSmall(true);
				x.getEquipment().setHelmet(new ItemStack(Material.DISPENSER));
			});
			headBottom.addPassenger(headTop);
			
			task = Bukkit.getScheduler().runTaskTimer(OlympaZTA.getInstance(), () -> {
				if (p.getLocation().distanceSquared(point) < 10) {
					stop(false);
				}else {
					Vector istep;
					i++;
					if (i == 1) {
						istep = step.clone().multiply(0.5);
					}else if (i == 2) {
						istep = step.clone().multiply(0.59);
						p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§6➤ §eAppuyez sur §6§lshift §epour descendre !"));
					}else if (i == 3) {
						istep = step.clone().multiply(0.68);
					}else if (i == 4) {
						istep = step.clone().multiply(0.77);
					}else if (i == 5) {
						istep = step.clone().multiply(0.86);
					}else if (i == 6) {
						istep = step.clone().multiply(0.92);
					}else if (i == 7) {
						istep = step.clone().multiply(0.97);
					}else {
						istep = step;
					}
					stand.setVelocity(istep);
				}
			}, 1L, 2L);
		}
		
		public void stop(boolean fromDismount) {
			task.cancel();
			players.remove(p);
			if (fromDismount) {
				Bukkit.getScheduler().runTask(OlympaZTA.getInstance(), this::end);
			}else end();
			stand.remove();
			headBottom.remove();
			headTop.remove();
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§6➤ §eBonne visite !"));
		}
		
		private void end() {
			p.teleport(p.getLocation());
			p.setVelocity(step.multiply(0.4));
			p.setAllowFlight(wasAllowFlight);
		}
	}
	
}
