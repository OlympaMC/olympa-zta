package fr.olympa.zta.plots;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.api.common.observable.ObservableList;
import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.common.sql.statement.OlympaStatement;
import fr.olympa.api.spigot.region.tracking.flags.DamageFlag;
import fr.olympa.api.spigot.region.tracking.flags.FishFlag;
import fr.olympa.api.spigot.region.tracking.flags.GameModeFlag;
import fr.olympa.api.spigot.region.tracking.flags.ItemDurabilityFlag;
import fr.olympa.api.spigot.region.tracking.flags.ItemPickupFlag;
import fr.olympa.api.spigot.region.tracking.flags.PhysicsFlag;
import fr.olympa.api.spigot.region.tracking.flags.PlayerBlockInteractFlag;
import fr.olympa.api.spigot.region.tracking.flags.PlayerBlocksFlag;
import fr.olympa.api.spigot.region.tracking.flags.RedstoneFlag;
import fr.olympa.api.spigot.utils.Schematic;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.loot.creators.FoodCreator.Food;

public class PlayerPlotsManager {
	
	private static final String tableName = OlympaZTA.getInstance().getServerNameID() + "_player_plots";

	private final OlympaStatement createPlot = new OlympaStatement("INSERT INTO " + tableName + " (`x`, `z`, `owner`) VALUES (?, ?, ?)", true);
	private final OlympaStatement setPlotLevel = new OlympaStatement("UPDATE " + tableName + " SET `level` = ? WHERE `id` = ?");
	private final OlympaStatement setPlotChests = new OlympaStatement("UPDATE " + tableName + " SET `chests` = ? WHERE `id` = ?");
	private final OlympaStatement getPlotPlayers = new OlympaStatement("SELECT `player_id` FROM " + AccountProviderAPI.getter().getPluginPlayerTable().getName() + " WHERE `plot` = ?");
	private final OlympaStatement removeOfflinePlayerPlot = new OlympaStatement("UPDATE " + AccountProviderAPI.getter().getPluginPlayerTable().getName() + " SET `plot` = '-1' WHERE `player_id` = ?");
	private final OlympaStatement loadPlot = new OlympaStatement("SELECT `owner`, `level`, `chests` FROM " + tableName + " WHERE `id` = ?");

	protected Map<OlympaPlayerZTA, ObservableList<PlayerPlot>> invitations = new HashMap<>();

	protected Map<Integer, InternalPlotDatas> plotsByID = new HashMap<>();
	protected Map<PlayerPlotLocation, InternalPlotDatas> plotsByPosition = new HashMap<>();
	
	protected Map<Player, Long> messages = new HashMap<>();
	
	private World worldCrea;
	private Schematic schematic;

	public PlayerPlotsManager(File schematicFile) throws SQLException {
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
		worldCrea.setGameRule(GameRule.RANDOM_TICK_SPEED, 4);
		worldCrea.setGameRule(GameRule.DO_MOB_SPAWNING, false);
		worldCrea.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
		worldCrea.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		worldCrea.setFullTime(10000);
		worldCrea.setDifficulty(Difficulty.PEACEFUL);

		OlympaCore.getInstance().getRegionManager().getWorldRegion(worldCrea).registerFlags(
				new ItemDurabilityFlag(true),
				new DamageFlag(true),
				new GameModeFlag(GameMode.SURVIVAL),
				new PhysicsFlag(false),
				new RedstoneFlag(true),
				new PlayerBlocksFlag(true) {
			@Override
			public <T extends Event & Cancellable> void blockEvent(T event, Player p, Block block) {
				event.setCancelled(PlayerPlotsManager.this.blockEvent(p, event, block));
			}
			@Override
			public <T extends Event & Cancellable> void entityEvent(T event, Player p, Entity entity) {
				event.setCancelled(PlayerPlotsManager.this.entityAction(p, entity));
			}
		}, new PlayerBlockInteractFlag(true) {
			@Override
			public void interactEvent(PlayerInteractEvent event) {
				PlayerPlot plot = getPlot(event.getClickedBlock().getLocation());
				event.setCancelled(plot == null || plot.onInteract(event));
			}
		}, new FishFlag(false) {
			@Override
			public void fishEvent(PlayerFishEvent event) {
				if (event.getState() == State.CAUGHT_FISH && event.getCaught() instanceof Item item) {
					Food food = ThreadLocalRandom.current().nextDouble() < 0.4 ? Food.COOKED_SALMON : Food.COOKED_COD;
					item.setItemStack(food.getOriginalItem());
				}
				super.fishEvent(event);
			}
		}, new ItemPickupFlag(false) {
			@Override
			public void itemPickupEvent(EntityPickupItemEvent e) {
				PlayerPlot plot = getPlot(e.getEntity().getLocation());
				e.setCancelled(plot != null && plot.entityAction((Player) e.getEntity()));
			}
		});

		new PlotsCommand(this).register();
		
		try {
			schematic = Schematic.load(schematicFile);
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
		if (id == -1) return null;
		InternalPlotDatas plotDatas = plotsByID.get(id);
		if (plotDatas == null) throw new NullPointerException("Les donn??es primaires de plot avec l'ID " + id + " n'ont pas ??t?? trouv??es.");

		if (load && plotDatas.loadedPlot == null) {
			try (PreparedStatement statement = loadPlot.createStatement()) {
				statement.setInt(1, id);
				ResultSet resultSet = loadPlot.executeQuery(statement);
				if (!resultSet.first()) throw new NullPointerException("Le plot du joueur a ??t?? supprim?? de la base de donn??es.");
				plotDatas.loadedPlot = new PlayerPlot(id, plotDatas.loc, resultSet.getLong("owner"), resultSet.getInt("chests"), resultSet.getInt("level"));
			}
			
			try (PreparedStatement statement = getPlotPlayers.createStatement()) {
				statement.setInt(1, id);
				ResultSet playersResultSet = getPlotPlayers.executeQuery(statement);
				while (playersResultSet.next()) {
					plotDatas.loadedPlot.getPlayers().add(playersResultSet.getLong("player_id"));
				}
			}
		}

		return plotDatas.loadedPlot;
	}

	public ObservableList<PlayerPlot> getInvitations(OlympaPlayerZTA player) {
		ObservableList<PlayerPlot> invit = invitations.get(player);
		return invit == null ? ObservableList.EMPTY_LIST : invit;
	}

	public void invite(OlympaPlayerZTA player, PlayerPlot plot) {
		ObservableList<PlayerPlot> invit = invitations.get(player);
		if (invit == null) {
			invit = new ObservableList<>(new ArrayList<>());
			invitations.put(player, invit);
		}
		invit.add(plot);
	}

	public void clearInvitations(OlympaPlayerZTA player) {
		invitations.remove(player);
	}

	public PlayerPlot create(PlayerPlotLocation location, OlympaPlayerZTA owner) throws SQLException {
		try (PreparedStatement statement = createPlot.createStatement()) {
			statement.setInt(1, location.getX());
			statement.setInt(2, location.getZ());
			statement.setLong(3, owner.getId());
			createPlot.executeUpdate(statement);
			ResultSet resultSet = statement.getGeneratedKeys();
			resultSet.next();
			PlayerPlot plot = new PlayerPlot(resultSet.getInt(1), location, owner.getId());
			plot.setLevel(1, true);
			addPlot(new InternalPlotDatas(plot));
			owner.setPlot(plot);
			resultSet.close();
			return plot;
		}
	}

	void updateLevel(PlayerPlot plot, int newLevel) throws SQLException {
		try (PreparedStatement statement = setPlotLevel.createStatement()) {
			statement.setInt(1, newLevel);
			statement.setInt(2, plot.getID());
			setPlotLevel.executeUpdate(statement);
		}
	}

	void updateChests(PlayerPlot plot, int chests) throws SQLException {
		try (PreparedStatement statement = setPlotChests.createStatement()) {
			statement.setInt(1, chests);
			statement.setInt(2, plot.getID());
			setPlotChests.executeUpdate(statement);
		}
	}

	void removePlayerPlot(OlympaPlayerInformations informations) throws SQLException {
		try (PreparedStatement statement = removeOfflinePlayerPlot.createStatement()) {
			statement.setLong(1, informations.getId());
			removeOfflinePlayerPlot.executeUpdate(statement);
		}
	}

	public PlayerPlotLocation getAvailable() {
		PlayerPlotLocation loc = new PlayerPlotLocation(0, 0);
		if (plotsByPosition.isEmpty()) return loc;
		int side = Math.max((int) Math.sqrt(plotsByPosition.size()), 1);
		int splitSide = (int) Math.ceil(side / 2D);
		for (int x = -splitSide; x <= splitSide; x++) {
			for (int z = -splitSide; z <= splitSide; z++) {
				loc.setX(x);
				loc.setZ(z);
				if (!plotsByPosition.containsKey(loc)) return loc;
			}
		}
		return null;
	}
	
	public void sendDenyMessage(Player p) {
		long time = System.currentTimeMillis();
		Long last = messages.get(p);
		if (last == null || last.longValue() < time) {
			Prefix.DEFAULT_BAD.sendMessage(p, "Tu n'as pas le droit de construire ici !");
			messages.put(p, time + 2000);
		}
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

	protected PlayerPlot getPlot(Location loc) {
		if (loc.getWorld() != worldCrea) return null;

		PlayerPlotLocation location = PlayerPlotLocation.get(loc);
		if (location == null) return null; // road

		InternalPlotDatas plotDatas = plotsByPosition.get(location);
		return plotDatas == null ? null : plotDatas.loadedPlot;
	}

	private boolean blockEvent(Player p, Event e, Block block) {
		PlayerPlot plot = getPlot(block.getLocation());
		if (plot == null) {
			sendDenyMessage(p);
			return true;
		}
		return plot.blockAction(p, e, block);
	}

	private boolean entityAction(Player p, Entity entity) {
		PlayerPlot plot = getPlot(entity.getLocation());
		if (plot == null) return true;
		return plot.entityAction(p);
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
		
		boolean isLoaded() {
			return loadedPlot != null;
		}
	}

}
