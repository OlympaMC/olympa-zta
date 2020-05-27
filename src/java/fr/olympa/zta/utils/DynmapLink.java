package fr.olympa.zta.utils;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerDescription;
import org.dynmap.markers.MarkerSet;

import fr.olympa.api.region.Region;
import fr.olympa.api.region.shapes.Cylinder;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.mobs.MobSpawning.SpawnType;

public class DynmapLink {

	private static DynmapAPI api;
	private static MarkerSet markers;

	public static void initialize() {
		try {
			api = (DynmapAPI) Bukkit.getPluginManager().getPlugin("dynmap");
			if (api != null) {
				markers = api.getMarkerAPI().getMarkerSet("regions");
				if (markers == null) markers = api.getMarkerAPI().createMarkerSet("regions", "Radar", null, false);
			}
		}catch (Exception ex) {
			api = null;
		}
		OlympaZTA.getInstance().sendMessage("Dynmap integration " + (api == null ? "disabled." : "enabled."));
	}

	public static void setPlayerVisiblity(Player p, boolean visibility) {
		if (api == null) return;
		api.setPlayerVisiblity(p, visibility);
	}

	public static void showMobArea(Region region, SpawnType spawn) {
		if (api == null) return;
		List<Location> points = region.getLocations();
		AreaMarker area = markers.createAreaMarker(spawn.name() + region.hashCode(), spawn.name, true, region.getWorld().getName(), points.stream().mapToDouble(Location::getBlockX).toArray(), points.stream().mapToDouble(Location::getBlockZ).toArray(), false);
		area.setFillStyle(0.4, spawn.color.asRGB());
		area.setDescription(spawn.description);
		OlympaZTA.getInstance().getLogger().info("Région affichée sur dynmap : " + area.getMarkerID());
	}
	
	public static void showSafeArea(Region region, String id, String title) {
		if (api == null) return;
		
		MarkerDescription marker;
		if (region instanceof Cylinder) {
			Cylinder cylinder = (Cylinder) region;
			marker = markers.createCircleMarker(id, title, true, region.getWorld().getName(), cylinder.getCenterX(), 0, cylinder.getCenterZ(), cylinder.getRadius(), cylinder.getRadius(), false);
		}else {
			List<Location> points = region.getLocations();
			marker = markers.createAreaMarker(id, title, true, region.getWorld().getName(), points.stream().mapToDouble(Location::getBlockX).toArray(), points.stream().mapToDouble(Location::getBlockZ).toArray(), false);
		}
		OlympaZTA.getInstance().getLogger().info("Région affichée sur dynmap : " + marker.getMarkerID());
	}
	
}
