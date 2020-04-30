package fr.olympa.zta.plots.players;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.sql.OlympaStatement;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.plots.PlotsCommand;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

public class PlayerPlotsManager implements Listener {
	
	private static final String tableName = "`zta_player_plots`";

	private final OlympaStatement createPlot;
	private final NPC npc;

	private Map<Integer, PlayerPlot> plotsByID = new HashMap<>();
	private Map<PlayerPlotLocation, PlayerPlot> plotsByPosition = new HashMap<>();
	private World worldCrea;

	public PlayerPlotsManager(int npcID) throws SQLException {
		OlympaCore.getInstance().getDatabase().createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " (" +
				"  `id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT," +
				"  `x` INT NOT NULL," +
				"  `z` INT NOT NULL," +
				"  `owner` INT NOT NULL," +
				"  `level` INT NOT NULL DEFAULT 1," +
				"  PRIMARY KEY (`id`))");

		createPlot = new OlympaStatement("INSERT INTO " + tableName + " (`x`, `z`, `owner`) VALUES (?, ?, ?)", true);

		ResultSet resultSet = OlympaCore.getInstance().getDatabase().createStatement().executeQuery("SELECT * FROM " + tableName);
		while (resultSet.next()) {
			try {
				PlayerPlot plot = new PlayerPlot(resultSet.getInt("id"), resultSet.getInt("x"), resultSet.getInt("z"), resultSet.getLong("owner"));
				plot.setLevel(resultSet.getInt("level"));
				addPlot(plot);
			}catch (Exception e) {
				e.printStackTrace();
				OlympaZTA.getInstance().getLogger().severe("Une erreur est survenue lors du chargement d'un plot.");
			}
		}

		worldCrea = Bukkit.createWorld(new WorldCreator("plots").generator(new PlotChunkGenerator()).environment(Environment.NORMAL));
		worldCrea.setSpawnLocation(0, worldCrea.getHighestBlockYAt(0, 0), 0);
		worldCrea.setGameRule(GameRule.RANDOM_TICK_SPEED, 5);
		worldCrea.setGameRule(GameRule.DO_MOB_SPAWNING, false);
		worldCrea.setDifficulty(Difficulty.PEACEFUL);

		new PlotsCommand(this).register();

		npc = CitizensAPI.getNPCRegistry().getById(npcID);
	}

	private void addPlot(PlayerPlot plot) {
		plotsByID.put(plot.getID(), plot);
		plotsByPosition.put(plot.getLocation(), plot);
	}

	public PlayerPlot create(PlayerPlotLocation location, OlympaPlayer owner) throws SQLException, IOException {
		PreparedStatement statement = createPlot.getStatement();
		statement.setInt(1, location.getX());
		statement.setInt(2, location.getZ());
		statement.setLong(3, owner.getId());
		statement.executeUpdate();
		ResultSet resultSet = statement.getGeneratedKeys();
		resultSet.next();
		PlayerPlot plot = new PlayerPlot(resultSet.getInt(1), location, owner.getId());
		addPlot(plot);
		resultSet.close();
		return plot;
	}

	public PlayerPlot getPlot(PlayerPlotLocation point) {
		return plotsByPosition.get(point);
	}

	public PlayerPlot getPlot(int id) {
		return plotsByID.get(id);
	}

	public PlayerPlotLocation getAvailable() {
		int side = Math.max((int) Math.sqrt(plotsByPosition.size()), 1);
		int splitSide = (int) Math.ceil(side / 2D);
		System.out.println("side " + side + " split " + splitSide);
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

	public World getWorld() {
		return worldCrea;
	}

	private boolean blockEvent(Player p, Block block) {
		if (block.getWorld() != worldCrea) return true;

		PlayerPlotLocation location = PlayerPlotLocation.get(block.getLocation());
		if (location == null) return true; // road

		PlayerPlot plot = getPlot(location);
		if (plot == null) return true; // empty plot

		return plot.blockAction(p);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		e.setCancelled(blockEvent(e.getPlayer(), e.getBlock()));
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		e.setCancelled(blockEvent(e.getPlayer(), e.getBlock()));
	}

	@EventHandler
	public void onNPCClick(NPCRightClickEvent e) {
		if (e.getNPC() == npc) {
			new PlayerPlotGUI().create(e.getClicker());
		}
	}

}
