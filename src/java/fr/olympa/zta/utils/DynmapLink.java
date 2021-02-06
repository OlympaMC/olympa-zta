package fr.olympa.zta.utils;

import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.CircleMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import fr.olympa.api.region.Region;
import fr.olympa.api.region.shapes.Cylinder;
import fr.olympa.api.region.tracking.ActionResult;
import fr.olympa.api.region.tracking.TrackedRegion;
import fr.olympa.api.region.tracking.flags.Flag;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.clans.plots.ClanPlot;
import fr.olympa.zta.loot.chests.LootChest;
import fr.olympa.zta.mobs.MobSpawning.SpawnType;

public class DynmapLink {

	private static DynmapAPI api;
	private static MarkerSet areasMarkers;
	private static MarkerSet chestsMarkers;
	private static MarkerIcon chestIcon;
	private static MarkerSet enderChestsMarkers;
	private static MarkerIcon enderChestIcon;
	private static MarkerSet plotsMarkers;
	private static MarkerIcon plotIcon;

	public static void initialize() {
		try {
			api = (DynmapAPI) Bukkit.getPluginManager().getPlugin("dynmap");
			if (api != null) {
				areasMarkers = api.getMarkerAPI().getMarkerSet("regions");
				if (areasMarkers == null) areasMarkers = api.getMarkerAPI().createMarkerSet("regions", "Radar", null, false);
				chestsMarkers = api.getMarkerAPI().getMarkerSet("chests");
				if (chestsMarkers == null) chestsMarkers = api.getMarkerAPI().createMarkerSet("chests", "Coffres", null, false);
				chestsMarkers.setHideByDefault(true);
				chestIcon = api.getMarkerAPI().getMarkerIcon("chest");
				enderChestsMarkers = api.getMarkerAPI().getMarkerSet("enderchests");
				if (enderChestsMarkers == null) enderChestsMarkers = api.getMarkerAPI().createMarkerSet("enderchests", "Coffres de l'End", null, false);
				enderChestIcon = api.getMarkerAPI().getMarkerIcon("portal");
				plotsMarkers = api.getMarkerAPI().getMarkerSet("plots");
				if (plotsMarkers == null) plotsMarkers = api.getMarkerAPI().createMarkerSet("plots", "Parcelles de Clans", null, false);
				plotIcon = api.getMarkerAPI().getMarkerIcon("house");
				new DynmapCommand().register();
			}
		}catch (Exception ex) {
			ex.printStackTrace();
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
		area.setDescription("<center><b>" + spawn.name + "</b></center><br><br>" + spawn.description);
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
	
	public static void showChest(LootChest chest) {
		if (api == null) return;
		
		String id = "chest" + chest.getID();
		Marker existingMarker = chestsMarkers.findMarker(id);
		if (existingMarker != null) existingMarker.deleteMarker();
	
		Location location = chest.getLocation();
		chestsMarkers.createMarker(id, "Coffre " + chest.getLootType().getName(), location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), chestIcon, false);
	}

	public static void hideChest(LootChest chest) {
		if (api == null) return;
		
		chestsMarkers.findMarker("chest" + chest.getID()).deleteMarker();
	}

	public static void showEnderChest(Location location) {
		if (api == null) return;
		
		String loc = SpigotUtils.convertLocationToString(location);
		enderChestsMarkers.createMarker(loc, "Enderchest", location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), enderChestIcon, false);
	}
	
	public static void hideEnderChest(Location location) {
		if (api == null) return;
		
		String loc = SpigotUtils.convertLocationToString(location);
		Marker marker = enderChestsMarkers.findMarker(loc);
		if (marker != null) marker.deleteMarker();
	}
	
	public static void showClanPlot(ClanPlot plot) {
		if (api == null) return;
		
		plotsMarkers.createMarker(Integer.toString(plot.getID()), "Parcelle de clan", plot.getSign().getWorld().getName(), plot.getSign().getX(), plot.getSign().getY(), plot.getSign().getZ(), plotIcon, false);
		
		//List<Location> points = plot.getTrackedRegion().getRegion().getLocations();
		//plotsMarkers.createAreaMarker(Integer.toString(plot.getID()), "Parcelle du clan" + plot.getID(), true, plot.getTrackedRegion().getRegion().getWorld().getName(), points.stream().mapToDouble(Location::getBlockX).toArray(), points.stream().mapToDouble(Location::getBlockZ).toArray(), true);
	}
	
	private static int i = 0;
	
	public static AreaMarker showDebug(int threadID, World world, int xFrom, int zFrom, int xTo, int zTo, int color) {
		if (api == null) return null;
		
		AreaMarker areaMarker = enderChestsMarkers.createAreaMarker(threadID + " " + i++, "Thread" + threadID, true, world.getName(), new double[] { xFrom, xTo }, new double[] { zFrom, zTo }, false);
		areaMarker.setFillStyle(0.8, color);
		return areaMarker;
	}
	
	public static class DynmapHideFlag extends Flag {
		@Override
		public ActionResult enters(Player p, Set<TrackedRegion> to) {
			setPlayerVisiblity(p, false);
			return super.enters(p, to);
		}

		@Override
		public ActionResult leaves(Player p, Set<TrackedRegion> to) {
			setPlayerVisiblity(p, true);
			return super.leaves(p, to);
		}
	}

}
