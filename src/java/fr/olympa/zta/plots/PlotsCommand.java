package fr.olympa.zta.plots;

import java.sql.SQLException;
import java.util.stream.Collectors;

import fr.olympa.api.common.command.complex.ArgumentParser;
import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.spigot.command.ComplexCommand;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import fr.olympa.zta.plots.PlayerPlotsManager.InternalPlotDatas;

public class PlotsCommand extends ComplexCommand {

	private PlayerPlotsManager manager;

	public PlotsCommand(PlayerPlotsManager manager) {
		super(OlympaZTA.getInstance(), "plot", "Permet de gérer les parcelles.", ZTAPermissions.PLOTS_MANAGE_COMMAND, "plots");
		this.manager = manager;
		super.addArgumentParser("PLOT", new ArgumentParser<>((sender, arg) -> manager.plotsByID.values().stream().filter(InternalPlotDatas::isLoaded).map(x -> Integer.toString(x.id)).collect(Collectors.toList()), arg -> {
			try {
				int id = Integer.parseInt(arg);
				return manager.plotsByID.values().stream().filter(t -> t.isLoaded() && t.id == id).findAny().orElse(null);
			}catch (NumberFormatException ex) {
				return null;
			}
		}, null));
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
	
	@Cmd
	public void globalInfos(CommandContext cmd) {
		sendInfo("Invitations en mémoire: %d", manager.invitations.size());
		sendInfo("Données internes de plots en mémoire: %d (= %d)", manager.plotsByID.size(), manager.plotsByPosition.size());
		sendInfo("Données chargées de plots: %d", manager.plotsByID.values().stream().filter(InternalPlotDatas::isLoaded).count());
	}
	
	@Cmd (args = "PLOT")
	public void info(CommandContext cmd) {
		PlayerPlot plot;
		if (cmd.getArgumentsLength() == 0) {
			if (isConsole()) {
				sendIncorrectSyntax();
				return;
			}
			InternalPlotDatas plotDatas = manager.plotsByPosition.get(PlayerPlotLocation.get(player.getLocation()));
			if (plotDatas == null) {
				sendError("Aucun plot n'a été trouvé à cette position.");
				return;
			}
			if (!plotDatas.isLoaded()) {
				sendError("Le plot #%d à cette position n'est pas chargé.", plotDatas.id);
				return;
			}
			plot = plotDatas.loadedPlot;
		}else plot = cmd.getArgument(0);
		
		sendInfo("Plot #%d, position: %s.", plot.getID(), plot.getLocation().toString());
		sendInfo("Appartient à %s (#%d). %d joueurs.", AccountProviderAPI.getter().getPlayerInformations(plot.getOwner()).getName(), plot.getOwner(), plot.getPlayers().size());
		sendInfo("Niveau: %d/%d.", plot.getLevel(), PlayerPlot.moneyRequiredPerLevel.length);
		sendInfo("Coffres à ce niveau: %d/%d.", plot.getChests(), PlayerPlot.chestsPerLevel[plot.getLevel() - 1]);
	}
	
	@Cmd (args = { "PLOT", "INTEGER" }, min = 2)
	public void setChests(CommandContext cmd) {
		PlayerPlot plot = cmd.getArgument(0);
		int chests = cmd.getArgument(1);
		if (chests < 0) {
			sendError("Le nombre de coffres doit être positif.");
			return;
		}
		plot.setChests(chests);
		sendSuccess("Le nombre de coffres de la parcelle #%d a été modifié.", plot.getID());
	}

}
