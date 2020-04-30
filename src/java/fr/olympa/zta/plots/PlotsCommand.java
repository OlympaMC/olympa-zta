package fr.olympa.zta.plots;

import java.io.IOException;
import java.sql.SQLException;

import org.bukkit.Material;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import fr.olympa.zta.plots.players.PlayerPlot;
import fr.olympa.zta.plots.players.PlayerPlotLocation;
import fr.olympa.zta.plots.players.PlayerPlotsManager;

public class PlotsCommand extends ComplexCommand {

	private PlayerPlotsManager manager;

	public PlotsCommand(PlayerPlotsManager manager) {
		super(OlympaZTA.getInstance(), "plot", "Permet de gérer les parcelles", ZTAPermissions.PLOTS_PLAYERS_COMMAND, "plots");
		this.manager = manager;
	}

	@Cmd (player = true)
	public void teleport(CommandContext cmd) {
		player.teleport(manager.getWorld().getSpawnLocation());
	}

	@Cmd (player = true)
	public void findAvailable(CommandContext cmd) {
		player.teleport(manager.getAvailable().toLocation().add(0, 2, 0));
	}

	@Cmd (player = true)
	public void take(CommandContext cmd) {
		try {
			PlayerPlot plot = manager.create(PlayerPlotLocation.get(player.getLocation()), getOlympaPlayer());
			plot.getLocation().toLocation().add(0, 5, 0).getBlock().setType(Material.REDSTONE_BLOCK);
		}catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}

}
