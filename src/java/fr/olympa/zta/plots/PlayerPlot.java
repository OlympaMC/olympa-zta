package fr.olympa.zta.plots;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.spigot.Schematic;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.clans.plots.ClanPlot;

public class PlayerPlot {

	//public static int[] questsRequiredPerLevel = { 3, 10, 25, 60, 140, 300, 750, 1500 };
	public static int[] moneyRequiredPerLevel = { 3000, 1000, 2500, 6000, 14000, 30000, 75000, 150000 };
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
		OlympaPlayerZTA oplayer = AccountProvider.get(player.getUUID());
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
					if (block.getType() != Material.GRASS_BLOCK && block.getType() != Material.WATER) block.setType(Material.GRASS_BLOCK);
				}
			}

			if (level != moneyRequiredPerLevel.length) {
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

	public boolean entityAction(Player p, Entity entity) {
		return OlympaPlayerZTA.get(p).getPlot() != this;
	}

	public boolean blockAction(Player p, Event e, Block block) {
		OlympaPlayerZTA oplayer = OlympaPlayerZTA.get(p);
		if (oplayer.getPlot() != this) {
			OlympaZTA.getInstance().plotsManager.sendDenyMessage(p);
			return true;
		}
		
		boolean chest = false;
		if (ClanPlot.CONTAINER_MATERIALS.contains(block.getType())) {
			chest = true;
			if (e instanceof BlockPlaceEvent && chests >= chestsPerLevel[level - 1]) {
				Prefix.DEFAULT_BAD.sendMessage(p, "Tu as atteint la limite des %d coffres pour une parcelle de niveau %d.", chestsPerLevel[level - 1], level);
				return true;
			}
		}

		Location location = block.getLocation();
		if (level < moneyRequiredPerLevel.length) {
			int x = location.getBlockX() - loc.getWorldX();
			int z = location.getBlockZ() - loc.getWorldZ();
			if (x < min || x > max || z < min || z > max) {
				Prefix.DEFAULT_BAD.sendMessage(p, "Tu as atteint la limite d'une parcelle de niveau %d.", level);
				return true;
			}
			if (location.getBlockY() > heightPerLevel[level - 1]) {
				Prefix.DEFAULT_BAD.sendMessage(p, "Tu as atteint la limite de hauteur pour une parcelle de niveau %d.", level);
				return true;
			}
		}

		if (chest) {
			if (e instanceof BlockPlaceEvent) {
				chests++;
			}else {
				if (block.getType() == Material.CHEST && owner != oplayer.getId()) {
					Prefix.DEFAULT_BAD.sendMessage(p, "Tu ne peux pas détruire le coffre du propriétaire.");
				}else chests--;
			}
			try {
				OlympaZTA.getInstance().plotsManager.updateChests(this, chests);
			}catch (SQLException ex) {
				ex.printStackTrace();
			}
		}else if (e instanceof BlockBreakEvent) {
			// pourquoi j'ai commencé ça ? à voir
		}
		return false;
	}

	public boolean onInteract(PlayerInteractEvent e) {
		OlympaPlayerZTA oplayer = OlympaPlayerZTA.get(e.getPlayer());
		if (oplayer.getPlot() != this) return true;
		
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.getClickedBlock().getType() == Material.CHEST) {
				if (owner != oplayer.getId()) {
					Prefix.DEFAULT_BAD.sendMessage(e.getPlayer(), "Tu ne peux pas ouvrir le coffre du propriétaire. Utilise les coffres piégés pour les invités.");
					return true;
				}
				ItemStack[] inventory = ((Chest) e.getClickedBlock().getState()).getInventory().getContents();
				OlympaZTA.getInstance().getTask().runTaskAsynchronously(() -> {
					try {
						int items = OlympaZTA.getInstance().gunRegistry.loadFromItems(inventory);
						if (items != 0) OlympaZTA.getInstance().sendMessage("%d items chargés depuis un coffre du plot %d de %s.", items, id, oplayer.getName());
					}catch (SQLException ex) {
						ex.printStackTrace();
					}
				});
			}
		}
		return false;
	}

}
