package fr.olympa.zta.plots;

import java.sql.SQLException;

import org.bukkit.entity.Player;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.editor.RegionEditor;
import fr.olympa.api.editor.TextEditor;
import fr.olympa.api.editor.parsers.NumberParser;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import fr.olympa.zta.plots.clans.ClanPlot;
import fr.olympa.zta.plots.players.PlayerPlotGUI;
import fr.olympa.zta.plots.players.PlayerPlotLocation;
import fr.olympa.zta.plots.players.PlayerPlotsManager;

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

	@Cmd (player = true)
	public void createClanPlot(CommandContext cmd) {
		Player p = player;
		new RegionEditor(p, (region) -> {
			if (region == null) {
				Prefix.DEFAULT_BAD.sendMessage(p, "La région sélectionnée n'est pas correcte.");
				return;
			}
			new TextEditor<>(p, (price) -> {
				ClanPlot plot = OlympaZTA.getInstance().clanPlotsManager.create(region, price);
				Prefix.DEFAULT_GOOD.sendMessage(p, "Tu as créé la parcelle de clan, ID " + plot.getID());
			}, () -> Prefix.DEFAULT_BAD.sendMessage(p, "Tu as annulé la création d'une parcelle de clan."), false, new NumberParser<>(Integer.class, true, true)).enterOrLeave();
		}).enterOrLeave();
	}

}
