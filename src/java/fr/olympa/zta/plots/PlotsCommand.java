package fr.olympa.zta.plots;

import java.sql.SQLException;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;

public class PlotsCommand extends ComplexCommand {

	private PlayerPlotsManager manager;

	public PlotsCommand(PlayerPlotsManager manager) {
		super(OlympaZTA.getInstance(), "plot", "Permet de gérer les parcelles", ZTAPermissions.PLOTS_MANAGE_COMMAND, "plots");
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
			manager.create(PlayerPlotLocation.get(player.getLocation()), getOlympaPlayer());
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Cmd (player = true)
	public void gui(CommandContext cmd) {
		new PlayerPlotGUI(getOlympaPlayer()).create(player);
	}

}
