package fr.olympa.zta.plots.players;

import java.sql.SQLException;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;

public class PlayerPlot {

	public static int[] questsRequiredPerLevel = { 3, 10, 25, 60, 140, 300, 750, 1500 };
	private static int[] sizePerLevel = { 10, 14, 18, 22, 26, 31, 36, 42 };
	private static int[] heightPerLevel = { PlotChunkGenerator.WORLD_LEVEL + 4, 40, 76, 112, 148, 184, 220, 256 };

	private final int id;
	private final PlayerPlotLocation loc;
	private final long owner;

	private int level = -1;

	private int min = -1;
	private int max = -1;

	PlayerPlot(int id, int x, int z, long owner) {
		this(id, new PlayerPlotLocation(x, z), owner);
	}

	PlayerPlot(int id, PlayerPlotLocation location, long owner) {
		this.id = id;
		this.loc = location;
		this.owner = owner;
	}

	public int getID() {
		return id;
	}

	public PlayerPlotLocation getLocation() {
		return loc;
	}

	public long getOwner() {
		return owner;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level, boolean update) {
		this.level = level;

		int up = PlotChunkGenerator.PLOT_WIDTH / 2;
		int down = up - 1 + PlotChunkGenerator.PLOT_WIDTH % 2;

		int maxDistance = sizePerLevel[level - 1];
		int addUp = maxDistance / 2 - 1;
		int addDown = addUp;
		addUp += maxDistance % 2;

		int oldMin = min;
		int oldMax = max;
		max = up + addUp;
		min = down - addDown;

		if (update) {

			for (int x = min; x <= max; x++) {
				for (int z = min; z <= max; z++) {
					if (oldMin != -1 && oldMax != -1 && x > oldMin && x < oldMax && z > oldMin && z < oldMax) continue;
					Block block = new Location(OlympaZTA.getInstance().plotsManager.getWorld(), loc.getWorldX() + x, PlotChunkGenerator.WORLD_LEVEL, loc.getWorldZ() + z).getBlock();
					if (block.getType() == Material.GRASS_BLOCK) {
						block.getRelative(0, 1, 0).setType(Material.AIR); // pour supprimer l'herbe
					}else block.setType(Material.GRASS_BLOCK);
				}
			}

			if (level != questsRequiredPerLevel.length) {
				int borderMin = min - 1;
				int borderMax = max + 1;
				boolean yellow = true;

				int x = borderMin;
				int z = borderMin;
				for (; x < borderMax; x++) {
					setBlock(x, z, (yellow = !yellow) ? Material.YELLOW_CONCRETE : Material.BLACK_CONCRETE);
				}
				for (; z < borderMax; z++) {
					setBlock(x, z, (yellow = !yellow) ? Material.YELLOW_CONCRETE : Material.BLACK_CONCRETE);
				}
				for (; x > borderMin; x--) {
					setBlock(x, z, (yellow = !yellow) ? Material.YELLOW_CONCRETE : Material.BLACK_CONCRETE);
				}
				for (; z > borderMin; z--) {
					setBlock(x, z, (yellow = !yellow) ? Material.YELLOW_CONCRETE : Material.BLACK_CONCRETE);
				}
			}

			if (level != 1) {
				try {
					OlympaZTA.getInstance().plotsManager.updateLevel(this, level);
				}catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void setBlock(int innerX, int innerZ, Material type) {
		new Location(OlympaZTA.getInstance().plotsManager.getWorld(), loc.getWorldX() + innerX, PlotChunkGenerator.WORLD_LEVEL, loc.getWorldZ() + innerZ).getBlock().setType(type);
	}

	public boolean blockAction(Player p, Block block, boolean place) {
		OlympaPlayerZTA oplayer = OlympaPlayerZTA.get(p);
		Location location = block.getLocation();
		if (level < questsRequiredPerLevel.length) {
			int x = location.getBlockX() - loc.getWorldX();
			int z = location.getBlockZ() - loc.getWorldZ();
			if (x < min || x > max || z < min || z > max | location.getBlockY() > heightPerLevel[level - 1]) return true;
		}
		return oplayer.getId() != owner;
	}

}
