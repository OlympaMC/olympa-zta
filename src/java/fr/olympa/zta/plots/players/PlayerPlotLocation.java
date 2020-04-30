package fr.olympa.zta.plots.players;

import org.bukkit.Location;

import fr.olympa.zta.OlympaZTA;

public class PlayerPlotLocation {

	private int x, z;
	
	public PlayerPlotLocation(int x, int z) {
		System.out.println(x + " " + z);
		this.x = x;
		this.z = z;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}

	@Override
	public int hashCode() {
		long bits = x;
		bits ^= z * 31;
		return (((int) bits) ^ ((int) (bits >> 32)));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PlayerPlotLocation) {
			PlayerPlotLocation other = (PlayerPlotLocation) obj;
			return other.x == x && other.z == z;
		}
		return false;
	}
	
	public Location toLocation() {
		return new Location(OlympaZTA.getInstance().plotsManager.getWorld(), x * PlotChunkGenerator.PLOT_CHUNK_SIZE * 16, PlotChunkGenerator.WORLD_LEVEL, z * PlotChunkGenerator.PLOT_CHUNK_SIZE * 16);
	}
	
	public static PlayerPlotLocation get(Location loc) {
		int x = loc.getBlockX() >> 4;
		int z = loc.getBlockZ() >> 4;
		if (x % PlotChunkGenerator.PLOT_CHUNK_SIZE == PlotChunkGenerator.PLOT_CHUNK_SIZE - 1) {
			if (loc.getBlockX() % 16 >= 16 - PlotChunkGenerator.ROAD_WIDTH) return null;
		}
		if (z % PlotChunkGenerator.PLOT_CHUNK_SIZE == PlotChunkGenerator.PLOT_CHUNK_SIZE - 1) {
			if (loc.getBlockZ() % 16 >= 16 - PlotChunkGenerator.ROAD_WIDTH) return null;
		}
		int plotX = (int) Math.floor((double) x / (double) PlotChunkGenerator.PLOT_CHUNK_SIZE);
		int plotZ = (int) Math.floor((double) z / (double) PlotChunkGenerator.PLOT_CHUNK_SIZE);
		return new PlayerPlotLocation(plotX, plotZ);
	}

}
