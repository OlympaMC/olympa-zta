package fr.olympa.zta.utils.map;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.CircleMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import fr.olympa.api.spigot.region.Region;
import fr.olympa.api.spigot.region.shapes.Cylinder;
import fr.olympa.api.spigot.region.tracking.ActionResult;
import fr.olympa.api.spigot.region.tracking.RegionEvent.EntryEvent;
import fr.olympa.api.spigot.region.tracking.RegionEvent.ExitEvent;
import fr.olympa.api.spigot.region.tracking.flags.Flag;
import fr.olympa.api.spigot.utils.CustomDayDuration;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.clans.plots.ClanPlot;
import fr.olympa.zta.loot.chests.LootChest;
import fr.olympa.zta.mobs.MobSpawning.SpawnType;

public class DynmapLink {

	private static DynmapLink instance;
	
	public boolean CHESTS_ENABLED = false;
	
	private DynmapAPI api;
	private MarkerSet areasMarkers;
	private MarkerSet chestsMarkers;
	private MarkerIcon chestIcon;
	private MarkerSet enderChestsMarkers;
	private MarkerIcon enderChestIcon;
	private MarkerSet plotsMarkers;
	private MarkerIcon plotIcon;

	private DynmapLink() {
		api = (DynmapAPI) Bukkit.getPluginManager().getPlugin("dynmap");
		if (api != null) {
			areasMarkers = api.getMarkerAPI().getMarkerSet("regions");
			if (areasMarkers == null) areasMarkers = api.getMarkerAPI().createMarkerSet("regions", "Radar", null, false);
			if (CHESTS_ENABLED) {
				chestsMarkers = api.getMarkerAPI().getMarkerSet("chests");
				if (chestsMarkers == null) chestsMarkers = api.getMarkerAPI().createMarkerSet("chests", "Coffres", null, false);
				chestsMarkers.setHideByDefault(true);
			}
			chestIcon = api.getMarkerAPI().getMarkerIcon("chest");
			enderChestsMarkers = api.getMarkerAPI().getMarkerSet("enderchests");
			if (enderChestsMarkers == null) enderChestsMarkers = api.getMarkerAPI().createMarkerSet("enderchests", "Coffres de l'End", null, false);
			enderChestIcon = api.getMarkerAPI().getMarkerIcon("portal");
			plotsMarkers = api.getMarkerAPI().getMarkerSet("plots");
			if (plotsMarkers == null) plotsMarkers = api.getMarkerAPI().createMarkerSet("plots", "Parcelles de Clans", null, false);
			plotIcon = api.getMarkerAPI().getMarkerIcon("house");
			new DynmapCommand().registerPreProcess().register();
		}
	}

	public void setPlayerVisiblity(Player p, boolean visibility) {
		OlympaPlayerZTA player = OlympaPlayerZTA.get(p);
		api.setPlayerVisiblity(p, visibility && !player.isHidden() && p.getWorld().getTime() < CustomDayDuration.NIGHT_TIME && p.getGameMode() != GameMode.SPECTATOR);
	}

	public void showMobArea(Region region, SpawnType spawn) {
		List<Location> points = region.getLocations();
		DynmapZoneConfig config = spawn.dynmap;
		AreaMarker area = areasMarkers.createAreaMarker("A" + spawn.name() + region.hashCode() + "A", config.name(), true, region.getWorld().getName(), points.stream().mapToDouble(Location::getBlockX).toArray(), points.stream().mapToDouble(Location::getBlockZ).toArray(), false);
		area.setFillStyle(0.3, config.color().asRGB());
		area.setDescription("<center><b><p style=\"color:#" + config.htmlColor() + ";\">" + config.name() + "</p></b><br>" + config.description() + "</center>");
	}
	
	public void showSafeArea(Region region, String id, String title) {
		id = "Z" + id + "Z";
		String description = "<center><b><p>" + title + "</p></b><br>Les zombies ne spawnent pas ici, vous êtes en sécurité.</center>";
		if (region instanceof Cylinder) {
			Cylinder cylinder = (Cylinder) region;
			CircleMarker marker = areasMarkers.createCircleMarker(id, title, true, region.getWorld().getName(), cylinder.getCenterX(), 0, cylinder.getCenterZ(), cylinder.getRadius(), cylinder.getRadius(), false);
			marker.setFillStyle(0.45, Color.AQUA.asRGB());
			marker.setDescription(description);
		}else {
			List<Location> points = region.getLocations();
			AreaMarker marker = areasMarkers.createAreaMarker(id, title, true, region.getWorld().getName(), points.stream().mapToDouble(Location::getBlockX).toArray(), points.stream().mapToDouble(Location::getBlockZ).toArray(), false);
			marker.setFillStyle(0.45, Color.AQUA.asRGB());
			marker.setDescription(description);
		}
	}
	
	public void showChest(LootChest chest) {
		if (!CHESTS_ENABLED) return;
		
		String id = "chest" + chest.getID();
		Marker existingMarker = chestsMarkers.findMarker(id);
		if (existingMarker != null) existingMarker.deleteMarker();
	
		Location location = chest.getLocation();
		chestsMarkers.createMarker(id, "Coffre " + chest.getLootType().getName(), location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), chestIcon, false);
	}

	public void hideChest(LootChest chest) {
		if (!CHESTS_ENABLED) return;
		
		chestsMarkers.findMarker("chest" + chest.getID()).deleteMarker();
	}

	public void showEnderChest(Location location) {
		String loc = SpigotUtils.convertLocationToString(location);
		enderChestsMarkers.createMarker(loc, "Enderchest", location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), enderChestIcon, false);
	}
	
	public void hideEnderChest(Location location) {
		String loc = SpigotUtils.convertLocationToString(location);
		Marker marker = enderChestsMarkers.findMarker(loc);
		if (marker != null) marker.deleteMarker();
	}
	
	public void showClanPlot(ClanPlot plot) {
		plotsMarkers.createMarker(Integer.toString(plot.getID()), "Parcelle de clan", plot.getSign().getWorld().getName(), plot.getSign().getX(), plot.getSign().getY(), plot.getSign().getZ(), plotIcon, false);
		
		//List<Location> points = plot.getTrackedRegion().getRegion().getLocations();
		//plotsMarkers.createAreaMarker(Integer.toString(plot.getID()), "Parcelle du clan" + plot.getID(), true, plot.getTrackedRegion().getRegion().getWorld().getName(), points.stream().mapToDouble(Location::getBlockX).toArray(), points.stream().mapToDouble(Location::getBlockZ).toArray(), true);
	}
	
	private int i = 0;
	
	public AreaMarker showDebug(int threadID, World world, int xFrom, int zFrom, int xTo, int zTo, int color) {
		AreaMarker areaMarker = enderChestsMarkers.createAreaMarker(threadID + " " + i++, "Thread" + threadID, true, world.getName(), new double[] { xFrom, xTo }, new double[] { zFrom, zTo }, false);
		areaMarker.setFillStyle(0.8, color);
		return areaMarker;
	}
	
	public static class DynmapHideFlag extends Flag {
		@Override
		public ActionResult enters(EntryEvent event) {
			DynmapLink.ifEnabled(link -> link.setPlayerVisiblity(event.getPlayer(), false));
			return super.enters(event);
		}

		@Override
		public ActionResult leaves(ExitEvent event) {
			DynmapLink.ifEnabled(link -> link.setPlayerVisiblity(event.getPlayer(), true));
			return super.leaves(event);
		}
	}
	
	public static void initialize() {
		try {
			if (Bukkit.getPluginManager().isPluginEnabled("dynmap")) instance = new DynmapLink();
		}catch (Exception ex) {
			ex.printStackTrace();
		}
		OlympaZTA.getInstance().sendMessage("Intégration Dynmap: " + (instance == null ? "§cdésactivée" : "§aactivée"));
	}
	
	public static DynmapLink getInstance() {
		return instance;
	}
	
	public static boolean isEnabled() {
		return instance != null;
	}
	
	public static void ifEnabled(Consumer<DynmapLink> consumer) {
		if (instance != null) consumer.accept(instance);
	}

}
