package fr.olympa.zta.utils;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.CircleMarker;
import org.dynmap.markers.MarkerSet;

import fr.olympa.api.region.Region;
import fr.olympa.api.region.shapes.Cylinder;
import fr.olympa.api.region.tracking.flags.Flag;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.mobs.MobSpawning.SpawnType;

public class DynmapLink {

	private static DynmapAPI api;
	private static MarkerSet areasMarkers;
	/*private static MarkerSet chestsMarkers;
	private static MarkerIcon chestIcon;*/

	public static void initialize() {
		try {
			api = (DynmapAPI) Bukkit.getPluginManager().getPlugin("dynmap");
			if (api != null) {
				areasMarkers = api.getMarkerAPI().getMarkerSet("regions");
				if (areasMarkers == null) areasMarkers = api.getMarkerAPI().createMarkerSet("regions", "Radar", null, false);
				/*chestsMarkers = api.getMarkerAPI().getMarkerSet("chests");
				if (chestsMarkers == null) chestsMarkers = api.getMarkerAPI().createMarkerSet("chests", "Coffres", null, false);
				chestIcon = api.getMarkerAPI().getMarkerIcon("chest");*/
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
		AreaMarker area = areasMarkers.createAreaMarker(spawn.name() + region.hashCode(), spawn.name, true, region.getWorld().getName(), points.stream().mapToDouble(Location::getBlockX).toArray(), points.stream().mapToDouble(Location::getBlockZ).toArray(), false);
		area.setFillStyle(0.3, spawn.color.asRGB());
		area.setDescription(spawn.description);
	}
	
	public static void showSafeArea(Region region, String id, String title) {
		if (api == null) return;
		
		if (region instanceof Cylinder) {
			Cylinder cylinder = (Cylinder) region;
			CircleMarker marker = areasMarkers.createCircleMarker(id, title, true, region.getWorld().getName(), cylinder.getCenterX(), 0, cylinder.getCenterZ(), cylinder.getRadius(), cylinder.getRadius(), false);
			marker.setFillStyle(0.45, Color.AQUA.asRGB());
		}else {
			List<Location> points = region.getLocations();
			AreaMarker marker = areasMarkers.createAreaMarker(id, title, true, region.getWorld().getName(), points.stream().mapToDouble(Location::getBlockX).toArray(), points.stream().mapToDouble(Location::getBlockZ).toArray(), false);
			marker.setFillStyle(0.45, Color.AQUA.asRGB());
		}
	}
	
	/*public static void showChest(LootChest chest) {
		if (api == null) return;
		
		String id = "chest" + chest.getID();
		Marker existingMarker = chestsMarkers.findMarker(id);
		if (existingMarker != null) existingMarker.deleteMarker();
	
		Location location = chest.getLocation();
		chestsMarkers.createMarker(id, "Coffre " + chest.getLootType().getName(), location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), chestIcon, false);
	}*/

	public static class DynmapHideFlag extends Flag {
		@Override
		public boolean enters(Player p) {
			setPlayerVisiblity(p, false);
			return super.enters(p);
		}

		@Override
		public boolean leaves(Player p) {
			setPlayerVisiblity(p, true);
			return super.leaves(p);
		}
	}

}
