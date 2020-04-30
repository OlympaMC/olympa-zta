package fr.olympa.zta.plots.players;

import org.bukkit.entity.Player;

import fr.olympa.zta.OlympaPlayerZTA;

public class PlayerPlot {

	private final int id;
	private final PlayerPlotLocation loc;
	private final long owner;

	private int level = 1;

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

	public void setLevel(int level) {
		this.level = level;
	}

	public boolean blockAction(Player p) {
		OlympaPlayerZTA oplayer = OlympaPlayerZTA.get(p);
		return oplayer.getId() != owner;
	}

}
