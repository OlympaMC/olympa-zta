package fr.olympa.zta.plots;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.spigot.Schematic;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;

public class PlayerPlot {

	public static int[] questsRequiredPerLevel = { 3, 10, 25, 60, 140, 300, 750, 1500 };
	private static int[] sizePerLevel = { 10, 14, 18, 22, 26, 31, 36, 42 };
	private static int[] heightPerLevel = { PlotChunkGenerator.WORLD_LEVEL + 4, 40, 76, 112, 148, 184, 220, 256 };
	private static int[] chestsPerLevel = { 1, 2, 3, 4, 5, 6, 7, 8 };

	private final int id;
	private final PlayerPlotLocation loc;
	private final long owner;
	private final List<Long> players = new ArrayList<>();

	private int level = -1;
	private int chests = 0;

	private int min = -1;
	private int max = -1;

	PlayerPlot(int id, PlayerPlotLocation location, long owner, int chests, int level) {
		this(id, location, owner);
		this.chests = chests;
		setLevel(level, false);
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

	public List<Long> getPlayers() {
		return players;
	}

	public int getLevel() {
		return level;
	}

	public Location getSpawnLocation() {
		return loc.toLocation().add(17, 2, level == 1 ? 10 : 20 - sizePerLevel[level - 1] / 2);
	}

	public void addPlayer(OlympaPlayerZTA player) {
		players.add(player.getId());
		player.setPlot(this);
		OlympaZTA.getInstance().plotsManager.clearInvitations(player);
	}

	public void kick(OlympaPlayerInformations player) {
		if (!players.remove(player.getId())) return;
		OlympaPlayerZTA oplayer = (OlympaPlayerZTA) AccountProvider.cache.get(player.getUUID());
		if (oplayer == null) {
			try {
				OlympaZTA.getInstance().plotsManager.removePlayerPlot(player);
			}catch (SQLException e) {
				e.printStackTrace();
			}
		}else oplayer.setPlot(null);
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
					if (oldMin != -1 && oldMax != -1 && x >= oldMin && x <= oldMax && z >= oldMin && z <= oldMax) continue;
					Block block = new Location(OlympaZTA.getInstance().plotsManager.getWorld(), loc.getWorldX() + x, PlotChunkGenerator.WORLD_LEVEL, loc.getWorldZ() + z).getBlock();
					if (block.getType() != Material.GRASS_BLOCK) block.setType(Material.GRASS_BLOCK);
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

			if (level == 1) {
				Schematic schematic = OlympaZTA.getInstance().plotsManager.getFirstBuildSchematic();
				Random random = new Random();
				int x = random.nextInt(sizePerLevel[level - 1] - schematic.width);
				int z = random.nextInt(sizePerLevel[level - 1] - schematic.length);
				schematic.paste(loc.toLocation().add(min + x, 1, min + z), true);

				Block sign = loc.toLocation().add(16, 1, 11).getBlock();
				sign.setType(Material.OAK_SIGN);

				org.bukkit.block.data.type.Sign signData = (org.bukkit.block.data.type.Sign) sign.getBlockData();
				signData.setRotation(BlockFace.NORTH_NORTH_EAST);
				sign.setBlockData(signData);

				Sign signState = (Sign) sign.getState();
				signState.setLine(1, "Parcelle de");
				signState.setLine(2, AccountProvider.getPlayerInformations(owner).getName());
				signState.update();
			}else {
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
		if (oplayer.getPlot() != this) return true;
		
		boolean chest = false;
		if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
			chest = true;
			if (place && chests == chestsPerLevel[level - 1]) return true;
		}

		Location location = block.getLocation();
		if (level < questsRequiredPerLevel.length) {
			int x = location.getBlockX() - loc.getWorldX();
			int z = location.getBlockZ() - loc.getWorldZ();
			if (x < min || x > max || z < min || z > max | location.getBlockY() > heightPerLevel[level - 1]) return true;
		}

		if (chest) {
			if (place) {
				chests++;
			}else chests--;
			try {
				OlympaZTA.getInstance().plotsManager.updateChests(this, chests);
			}catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public boolean onInteract(PlayerInteractEvent e) {
		OlympaPlayerZTA oplayer = OlympaPlayerZTA.get(e.getPlayer());
		if (oplayer.getPlot() != this) return true;
		
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.getClickedBlock().getType() == Material.CHEST) {
				return owner != oplayer.getId();
			}
		}
		return false;
	}

}