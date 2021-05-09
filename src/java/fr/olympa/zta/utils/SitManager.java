package fr.olympa.zta.utils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.Stairs.Shape;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spigotmc.event.entity.EntityDismountEvent;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaZTA;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class SitManager implements Listener {
	
	private BiMap<Player, Location> sitting = HashBiMap.create();
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		Player p = e.getPlayer();
		if (p.isInsideVehicle()) return;
		if (p.isSneaking()) return;
		if (e.getClickedBlock().getType().name().endsWith("_STAIRS")) {
			if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) {
				p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cTu ne peux t'asseoir en étant en créatif."));
				return;
			}
			if (sitting.containsValue(e.getClickedBlock().getLocation())) {
				Prefix.BAD.sendMessage(p, "Cette chaise est déjà occupée.");
			}else {
				Stairs stairs = (Stairs) e.getClickedBlock().getBlockData();
				if (stairs.getShape() != Shape.STRAIGHT || stairs.getHalf() == Half.TOP) return;
				BlockFace facing = stairs.getFacing();
				double xMod = facing.getModX() * -0.12;
				double zMod = facing.getModZ() * -0.12;
				Location location = e.getClickedBlock().getLocation().add(0.5 + xMod, 0.3, 0.5 + zMod);
				location.setYaw(facing.getModX() * 90f);
				ArmorStand stand = p.getWorld().spawn(location, ArmorStand.class, x -> {
					x.setPersistent(false);
					x.setInvisible(true);
					x.setMarker(true);
					x.setSmall(true);
					x.setGravity(false);
				});
				stand.addPassenger(p);
				sitting.put(p, e.getClickedBlock().getLocation());
				Bukkit.getScheduler().runTaskLaterAsynchronously(OlympaZTA.getInstance(), () -> p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§aTu es maintenant assis !")), 3);
			}
		}
	}
	
	@EventHandler
	public void onLeaveVehicle(EntityDismountEvent e) {
		Location block = sitting.remove(e.getEntity());
		if (block != null) {
			Bukkit.getScheduler().runTask(OlympaZTA.getInstance(), () -> {
				e.getDismounted().remove();
				e.getEntity().teleport(block.add(0, 1, 0).setDirection(e.getEntity().getLocation().getDirection()));
			});
		}
	}
	
}
