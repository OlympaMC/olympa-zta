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
	private final OlympaStatement loadPlot = new OlympaStatement("SELECT `owner`, `level`, `chests` WHERE `id` = ?");

	private Map<OlympaPlayerZTA, List<PlayerPlot>> invitations = new HashMap<>();

	private Map<Integer, InternalPlotDatas> plotsByID = new HashMap<>();
	private Map<PlayerPlotLocation, InternalPlotDatas> plotsByPosition = new HashMap<>();
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
				ResultSet resultSet = OlympaCore.getInstance().getDatabase().createStatement().executeQuery("SELECT `id`, `x`, `z` FROM " + tableName);
				while (resultSet.next()) {
					int id = resultSet.getInt("id");
					PlayerPlotLocation location = new PlayerPlotLocation(resultSet.getInt("x"), resultSet.getInt("z"));
					InternalPlotDatas plot = new InternalPlotDatas(id, location);
					plotsByPosition.put(location, plot);
					plotsByID.put(id, plot);
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

	private void addPlot(InternalPlotDatas plot) {
		plotsByID.put(plot.id, plot);
		plotsByPosition.put(plot.loc, plot);
	}
	
	public PlayerPlot getPlot(int id, boolean load) throws SQLException {
		InternalPlotDatas plotDatas = plotsByID.get(id);
		if (plotDatas == null) throw new NullPointerException("Les données primaires avec l'ID " + id + " n'ont pas été chargées.");

		if (load && plotDatas.loadedPlot == null) {
			PreparedStatement statement = loadPlot.getStatement();
			statement.setInt(1, id);
			ResultSet resultSet = statement.executeQuery();
			if (!resultSet.first()) throw new NullPointerException("Le plot du joueur a été supprimé de la base de données.");
			plotDatas.loadedPlot = new PlayerPlot(id, plotDatas.loc, resultSet.getLong("owner"), resultSet.getInt("chests"), resultSet.getInt("level"));

			statement = getPlotPlayers.getStatement();
			statement.setInt(1, id);
			ResultSet playersResultSet = statement.executeQuery();
			while (playersResultSet.next()) {
				plotDatas.loadedPlot.getPlayers().add(playersResultSet.getLong("player_id"));
			}
		}

		return plotDatas.loadedPlot;
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
		addPlot(new InternalPlotDatas(plot));
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

		InternalPlotDatas plotDatas = plotsByPosition.get(location);
		return plotDatas == null ? null : plotDatas.loadedPlot;
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

	class InternalPlotDatas {
		final int id;
		final PlayerPlotLocation loc;
		PlayerPlot loadedPlot;

		InternalPlotDatas(int id, PlayerPlotLocation loc) {
			this.id = id;
			this.loc = loc;
		}

		InternalPlotDatas(PlayerPlot plot) {
			this.id = plot.getID();
			this.loc = plot.getLocation();
			loadedPlot = plot;
		}
	}

}
