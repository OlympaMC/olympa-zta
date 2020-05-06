package fr.olympa.zta.plots.players;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.api.objects.OlympaPlayerInformations;
import fr.olympa.api.sql.OlympaStatement;
import fr.olympa.api.utils.Schematic;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.plots.PlotsCommand;

public class PlayerPlotsManager implements Listener {
	
	private static final String tableName = "`zta_player_plots`";

	private final OlympaStatement createPlot = new OlympaStatement("INSERT INTO " + tableName + " (`x`, `z`, `owner`) VALUES (?, ?, ?)", true);
	private final OlympaStatement setPlotLevel = new OlympaStatement("UPDATE " + tableName + " SET `level` = ? WHERE `id` = ?");
	private final OlympaStatement setPlotChests = new OlympaStatement("UPDATE " + tableName + " SET `chests` = ? WHERE `id` = ?");
	private final OlympaStatement getPlotPlayers = new OlympaStatement("SELECT `player_id` FROM " + tableName + " WHERE `plot` = ?");
	private final OlympaStatement removeOfflinePlayerPlot = new OlympaStatement("UPDATE " + tableName + " SET `plot` = NULL WHERE `player_id` = ?");

	private Map<OlympaPlayerZTA, List<PlayerPlot>> invitations = new HashMap<>();

	private Map<Integer, PlayerPlot> plotsByID = new HashMap<>();
	private Map<PlayerPlotLocation, PlayerPlot> plotsByPosition = new HashMap<>();
	private World worldCrea;
	private Schematic schematic;

	public PlayerPlotsManager(File file) throws SQLException {
		OlympaCore.getInstance().getDatabase().createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " (" +
				"  `id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT," +
				"  `x` INT NOT NULL," +
				"  `z` INT NOT NULL," +
				"  `owner` INT NOT NULL," +
				"  `level` SMALLINT NOT NULL DEFAULT 1," +
				"  `chests` SMALLINT NOT NULL DEFAULT 1,"
				+
				"  PRIMARY KEY (`id`))");

		worldCrea = Bukkit.createWorld(new WorldCreator("plots").generator(new PlotChunkGenerator()).generateStructures(false).environment(Environment.NORMAL));
		worldCrea.setSpawnLocation(0, worldCrea.getHighestBlockYAt(0, 0), 0);
		worldCrea.setGameRule(GameRule.RANDOM_TICK_SPEED, 5);
		worldCrea.setGameRule(GameRule.DO_MOB_SPAWNING, false);
		worldCrea.setDifficulty(Difficulty.PEACEFUL);

		new PlotsCommand(this).register();
		
		try {
			schematic = Schematic.load(file);
		}catch (Exception e) {
			e.printStackTrace();
		}

		OlympaZTA.getInstance().getTask().runTaskAsynchronously(() -> {
			try {
				ResultSet resultSet = OlympaCore.getInstance().getDatabase().createStatement().executeQuery("SELECT * FROM " + tableName);
				while (resultSet.next()) {
					try {
						PlayerPlot plot = new PlayerPlot(resultSet.getInt("id"), resultSet.getInt("x"), resultSet.getInt("z"), resultSet.getLong("owner"), resultSet.getInt("level"), resultSet.getInt("chests"));
						addPlot(plot);
						PreparedStatement statement = getPlotPlayers.getStatement();
						statement.setInt(1, plot.getID());
						ResultSet playersResultSet = statement.executeQuery();
						while (playersResultSet.next()) {
							plot.getPlayers().add(playersResultSet.getLong("player_id"));
						}
					}catch (Exception e) {
						e.printStackTrace();
						OlympaZTA.getInstance().getLogger().severe("Une erreur est survenue lors du chargement d'un plot.");
					}
				}
			}catch (SQLException ex) {
				ex.printStackTrace();
			}
		});
	}

	public World getWorld() {
		return worldCrea;
	}

	public Schematic getFirstBuildSchematic() {
		return schematic;
	}

	private void addPlot(PlayerPlot plot) {
		plotsByID.put(plot.getID(), plot);
		plotsByPosition.put(plot.getLocation(), plot);
	}

	public PlayerPlot getPlot(PlayerPlotLocation point) {
		return plotsByPosition.get(point);
	}

	public PlayerPlot getPlot(int id) {
		return plotsByID.get(id);
	}

	public List<PlayerPlot> getInvitations(OlympaPlayerZTA player) {
		List<PlayerPlot> invit = invitations.get(player);
		if (invit == null) invit = Collections.EMPTY_LIST;
		return invit;
	}

	public void invite(OlympaPlayerZTA player, PlayerPlot plot) {
		List<PlayerPlot> invit = invitations.get(player);
		if (invit == null) {
			invit = new ArrayList<>();
			invitations.put(player, invit);
		}
		invit.add(plot);
	}

	public void clearInvitations(OlympaPlayerZTA player) {
		invitations.remove(player);
	}

	public PlayerPlot create(PlayerPlotLocation location, OlympaPlayerZTA owner) throws SQLException {
		PreparedStatement statement = createPlot.getStatement();
		statement.setInt(1, location.getX());
		statement.setInt(2, location.getZ());
		statement.setLong(3, owner.getId());
		statement.executeUpdate();
		ResultSet resultSet = statement.getGeneratedKeys();
		resultSet.next();
		PlayerPlot plot = new PlayerPlot(resultSet.getInt(1), location, owner.getId());
		plot.setLevel(1, true);
		addPlot(plot);
		owner.setPlot(plot);
		resultSet.close();
		return plot;
	}

	void updateLevel(PlayerPlot plot, int newLevel) throws SQLException {
		PreparedStatement statement = setPlotLevel.getStatement();
		statement.setInt(1, newLevel);
		statement.setInt(2, plot.getID());
		statement.executeUpdate();
	}

	void updateChests(PlayerPlot plot, int chests) throws SQLException {
		PreparedStatement statement = setPlotChests.getStatement();
		statement.setInt(1, chests);
		statement.setInt(2, plot.getID());
		statement.executeUpdate();
	}

	void removePlayerPlot(OlympaPlayerInformations informations) throws SQLException {
		PreparedStatement statement = removeOfflinePlayerPlot.getStatement();
		statement.setLong(1, informations.getId());
		statement.executeUpdate();
	}

	public PlayerPlotLocation getAvailable() {
		int side = Math.max((int) Math.sqrt(plotsByPosition.size()), 1);
		int splitSide = (int) Math.ceil(side / 2D);
		PlayerPlotLocation loc = new PlayerPlotLocation(0, 0);
		for (int x = -splitSide; x <= splitSide; x++) {
			for (int z = -splitSide; z <= splitSide; z++) {
				loc.setX(x);
				loc.setZ(z);
				if (!plotsByPosition.containsKey(loc)) return loc;
			}
		}
		return null;
	}

	public void initSearch(OlympaPlayerZTA player) {
		player.plotFind = new BukkitRunnable() {
			@Override
			public void run() {
				PlayerPlotLocation available = getAvailable();
				Bukkit.getScheduler().runTask(OlympaZTA.getInstance(), () -> {
					try {
						create(available, player);
					}catch (SQLException e) {
						e.printStackTrace();
					}finally {
						player.plotFind = null;
					}
				});
			}
		}.runTaskAsynchronously(OlympaZTA.getInstance());
	}

	private PlayerPlot getPlot(Location loc) {
		if (loc.getWorld() != worldCrea) return null;

		PlayerPlotLocation location = PlayerPlotLocation.get(loc);
		if (location == null) return null; // road

		return getPlot(location);
	}

	private boolean blockEvent(Player p, Block block, boolean place) {
		PlayerPlot plot = getPlot(block.getLocation());
		if (plot == null) return true;
		return plot.blockAction(p, block, place);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		e.setCancelled(blockEvent(e.getPlayer(), e.getBlock(), false));
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		e.setCancelled(blockEvent(e.getPlayer(), e.getBlock(), true));
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.getClickedBlock() == null) return;
		PlayerPlot plot = getPlot(e.getClickedBlock().getLocation());
		if (plot != null) e.setCancelled(plot.onInteract(e));
	}

}
