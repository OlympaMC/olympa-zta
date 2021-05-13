package fr.olympa.zta.clans.plots;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.editor.RegionEditor;
import fr.olympa.api.editor.TextEditor;
import fr.olympa.api.editor.WaitBlockClick;
import fr.olympa.api.editor.WaitClick;
import fr.olympa.api.editor.parsers.NumberParser;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;

public class ClanPlotsCommand extends ComplexCommand {
	
	private ClanPlotsManager manager;
	
	private ClanPlotPaginator paginatorAll = new ClanPlotPaginator(10, "Parcelles de clans", "all", () -> new ArrayList<>(manager.getPlots().values()));
	private ClanPlotPaginator paginatorRent = new ClanPlotPaginator(10, "Parcelles de clans louées", "rent", () -> manager.getPlots().values().stream().filter(x -> x.getClan() != null).collect(Collectors.toList()));
	private ClanPlotPaginator paginatorFree = new ClanPlotPaginator(10, "Parcelles de clans vides", "free", () -> manager.getPlots().values().stream().filter(x -> x.getClan() == null).collect(Collectors.toList()));
	
	public ClanPlotsCommand(ClanPlotsManager manager) {
		super(OlympaZTA.getInstance(), "clanplots", "Permet de gérer les parcelles de clan.", ZTAPermissions.CLAN_PLOTS_MANAGE_COMMAND);
		this.manager = manager;
		
		super.addArgumentParser("PLOT", sender -> manager.getPlots().keySet().stream().map(x -> x.toString()).collect(Collectors.toList()), x -> manager.getPlots().get(Integer.parseInt(x)), x -> String.format("Le plot %s n'existe pas.", x));
	}
	
	@Cmd (player = true)
	public void createPlot(CommandContext cmd) {
		Player p = player;
		Runnable cancel = /*() -> Prefix.DEFAULT_BAD.sendMessage(p, "Tu as annulé la création d'une parcelle de clan.")*/ null;
		Prefix.INFO.sendMessage(p, "Sélectionne la région de la parcelle.");
		new RegionEditor(p, (region) -> {
			if (region == null) {
				Prefix.DEFAULT_BAD.sendMessage(p, "La région sélectionnée n'est pas correcte.");
				return;
			}
			Prefix.INFO.sendMessage(p, "Entre le prix de la parcelle.");
			new TextEditor<>(p, (price) -> {
				Prefix.INFO.sendMessage(p, "Clique sur la pancarte utilisée pour louer la parcelle.");
				new WaitBlockClick(p, (block) -> {
					Prefix.INFO.sendMessage(p, "Déplace-toi à l'endroit où les joueurs spawneront.");
					new WaitClick(p, ItemUtils.item(Material.DIAMOND, "§bValider le point de spawn"), () -> {
						try {
							ClanPlot plot = OlympaZTA.getInstance().clanPlotsManager.create(region, price, block, p.getLocation());
							Prefix.DEFAULT_GOOD.sendMessage(p, "Tu as créé la parcelle de clan, ID " + plot.getID());
						}catch (Exception e) {
							e.printStackTrace();
							Prefix.ERROR.sendMessage(p, "Une erreur est survenue lors de la création de la parcelle.");
						}
					}).enterOrLeave();
				}, ItemUtils.item(Material.STICK, "§aSélectionner le bloc"), (block) -> block.getType().name().endsWith("_SIGN")).enterOrLeave();
			}, cancel, false, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).enterOrLeave();
		}).enterOrLeave();
	}
	
	@Cmd (args = "PLOT", min = 1, syntax = "<plot ID>")
	public void info(CommandContext cmd) {
		ClanPlot plot = cmd.getArgument(0);
		sendSuccess("Parcelle de clan %d:", plot.getID());
		sendInfo("Point d'apparition: §6%s", SpigotUtils.convertLocationToHumanString(plot.getSpawn()));
		sendInfo("Pancarte informative: §6%s", SpigotUtils.convertBlockLocationToString(plot.getSign()));
		sendInfo("Prix: §6%s", plot.getPriceFormatted());
		if (plot.getClan() != null) {
			sendInfo("Louée au clan: §6%s", plot.getClan().getName() + " §e(" + plot.getClan().getID() + ")");
			sendInfo("Expire le §6%s", plot.getExpirationDate());
		}else sendInfo("Actuellement §6non louée");
	}
	
	@Cmd
	public void updateSigns(CommandContext cmd) {
		int i = 0;
		for (ClanPlot plot : manager.getPlots().values()) {
			plot.updateSign();
			i++;
		}
		sendSuccess("%d panneaux ont été mis à jour.", i);
	}
	
	@Cmd (player = true, args = "PLOT", min = 1, syntax = "<plot ID>")
	public void teleport(CommandContext cmd) {
		ClanPlot plot = cmd.getArgument(0);
		player.teleport(plot.getSpawn());
		sendSuccess("Tu as été téléporté au spawn du plot %d.", plot.getID());
	}
	
	@Cmd (player = true, args = "PLOT", min = 1)
	public void editRegion(CommandContext cmd) {
		ClanPlot plot = cmd.getArgument(0);
		Player p = getPlayer();
		Prefix.INFO.sendMessage(p, "Sélectionne la région de la parcelle.");
		new RegionEditor(p, (region) -> {
			if (region == null) {
				Prefix.DEFAULT_BAD.sendMessage(p, "La région sélectionnée n'est pas correcte.");
				return;
			}
			plot.getTrackedRegion().updateRegion(region);
			Prefix.DEFAULT_GOOD.sendMessage(p, "La région du plot %d a été modifiée.", plot.getID());
		}).enterOrLeave();
	}
	
	@Cmd (args = { "all|rent|free", "INTEGER" })
	public void list(CommandContext cmd) {
		ClanPlotPaginator paginator;
		switch (cmd.getArgument(0, "all")) {
		case "all":
			paginator = paginatorAll;
			break;
		case "rent":
			paginator = paginatorRent;
			break;
		case "free":
			paginator = paginatorFree;
			break;
		default:
			paginator = null;
			break;
		}
		if (paginator == null) {
			sendIncorrectSyntax();
			return;
		}
		sendComponents(paginator.getPage(cmd.getArgument(1, 1)));
	}
	
}
