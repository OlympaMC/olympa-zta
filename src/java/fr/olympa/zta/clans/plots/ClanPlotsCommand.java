package fr.olympa.zta.clans.plots;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.spigot.command.ComplexCommand;
import fr.olympa.api.spigot.editor.RegionEditor;
import fr.olympa.api.spigot.editor.TextEditor;
import fr.olympa.api.spigot.editor.WaitBlockClick;
import fr.olympa.api.spigot.editor.WaitClick;
import fr.olympa.api.spigot.editor.parsers.NumberParser;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import fr.olympa.zta.clans.ClanZTA;

public class ClanPlotsCommand extends ComplexCommand {
	
	private ClanPlotsManager manager;
	
	private ClanPlotPaginator paginatorAll = new ClanPlotPaginator(10, "Parcelles de clans", "all", () -> new ArrayList<>(manager.getPlots().values()));
	private ClanPlotPaginator paginatorRent = new ClanPlotPaginator(10, "Parcelles de clans louées", "rent", () -> manager.getPlots().values().stream().filter(x -> x.getClan() != null).collect(Collectors.toList()));
	private ClanPlotPaginator paginatorFree = new ClanPlotPaginator(10, "Parcelles de clans vides", "free", () -> manager.getPlots().values().stream().filter(x -> x.getClan() == null).collect(Collectors.toList()));
	
	public ClanPlotsCommand(ClanPlotsManager manager) {
		super(OlympaZTA.getInstance(), "clanplots", "Permet de gérer les parcelles de clan.", ZTAPermissions.CLAN_PLOTS_COMMAND);
		this.manager = manager;
		
		super.addArgumentParser("PLOT", (sender, arg) -> manager.getPlots().keySet().stream().map(x -> x.toString()).collect(Collectors.toList()), x -> manager.getPlots().get(Integer.parseInt(x)), x -> String.format("Le plot %s n'existe pas.", x));
	}
	
	@Cmd (player = true, permissionName = "CLAN_PLOTS_MANAGE_COMMAND")
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
		sendHoverAndCommand(Prefix.INFO, "Point d'apparition: §6" + SpigotUtils.convertLocationToHumanString(plot.getSpawn()), "Clique pour te téléporter.", "/clanplots teleport " + plot.getID());
		sendInfo("Pancarte informative: §6%s", SpigotUtils.convertBlockLocationToString(plot.getSign()));
		sendInfo("Prix: §6%s", plot.getPriceFormatted());
		if (plot.getClan() != null) {
			sendInfo("Louée au clan: §6%s", plot.getClan().getName() + " §e(" + plot.getClan().getID() + ")");
			sendInfo("Expire le §6%s", plot.getExpirationDate());
		}else sendInfo("Actuellement §6non louée");
	}
	
	@Cmd (permissionName = "CLAN_PLOTS_MANAGE_COMMAND")
	public void updateSigns(CommandContext cmd) {
		int i = 0;
		for (ClanPlot plot : manager.getPlots().values()) {
			plot.updateSign();
			i++;
		}
		sendSuccess("%d panneaux ont été mis à jour.", i);
	}
	
	@Cmd (min = 1, args = "PLOT", permissionName = "CLAN_PLOTS_MANAGE_COMMAND")
	public void eject(CommandContext cmd) {
		ClanPlot plot = cmd.getArgument(0);
		ClanZTA clan = plot.getClan();
		if (clan == null) {
			sendError("La parcelle #%d n'est louée par aucun clan...", plot.getID());
			return;
		}
		if (cmd.getArgumentsLength() == 1 || !"confirm".equals(cmd.getArgument(1))) {
			sendSuccess("§eÊtes-vous sûr de vouloir éjecter le clan %s de la parcelle #%d ? Utilisez /clanplots eject %d confirm.", clan.getName(), plot.getID(), plot.getID());
		}else {
			plot.setClan(null, true);
			clan.broadcast("Un opérateur vous a retiré votre parcelle.");
			sendSuccess("La parcelle #%d a été retirée au clan %s.", plot.getID(), clan.getName());
		}
	}
	
	@Cmd (min = 1, args = "PLOT", permissionName = "CLAN_PLOTS_MANAGE_COMMAND")
	public void emptyChests(CommandContext cmd) {
		ClanPlot plot = cmd.getArgument(0);
		if (cmd.getArgumentsLength() == 1 || !"confirm".equals(cmd.getArgument(1))) {
			sendSuccess("§eÊtes-vous sûr de vouloir vider les coffres de la parcelle #%d ? Utilisez /clanplots emptyChests %d confirm. CETTE ACTION EST IRREVERSIBLE.", plot.getID(), plot.getID());
		}else {
			emptyChests(plot);
		}
	}
	
	@Cmd (permissionName = "CLAN_PLOTS_MANAGE_COMMAND")
	public void emptyAllChests(CommandContext cmd) {
		if (cmd.getArgumentsLength() == 0 || !"confirm".equals(cmd.getArgument(0))) {
			sendSuccess("§eÊtes-vous sûr de vouloir vider les coffres de toutes les parcelles ? Utilisez /clanplots emptyAllChests confirm. CETTE ACTION EST IRREVERSIBLE.");
		}else {
			manager.getPlots().values().forEach(this::emptyChests);
		}
	}
	
	private void emptyChests(ClanPlot plot) {
		int clear = 0;
		Iterator<Block> blockList = plot.getTrackedRegion().getRegion().blockList();
		for (; blockList.hasNext();) {
			Block block = blockList.next();
			if (ClanPlot.CONTAINER_MATERIALS.contains(block.getType())) {
				Container container = (Container) block.getState();
				Inventory inventory = container.getInventory();
				if (!inventory.isEmpty()) {
					inventory.clear();
					clear++;
				}
				//container.update();
			}
		}
		sendSuccess("La parcelle #%d a été vidée de %d inventaires.", plot.getID(), clear);
	}
	
	@Cmd (hide = true, permissionName = "CLAN_PLOTS_MANAGE_COMMAND")
	public void updateDBRegions(CommandContext cmd) {
		int i = 0;
		for (ClanPlot plot : manager.getPlots().values()) {
			try {
				plot.setRegion(plot.getTrackedRegion().getRegion());
				i++;
			}catch (SQLException | IOException e) {
				e.printStackTrace();
				sendError(e);
			}
			i++;
		}
		sendSuccess("%d régions ont été mises à jour.", i);
	}
	
	@Cmd (player = true, args = "PLOT", min = 1, syntax = "<plot ID>")
	public void teleport(CommandContext cmd) {
		ClanPlot plot = cmd.getArgument(0);
		player.teleport(plot.getSpawn());
		sendSuccess("Tu as été téléporté au spawn du plot %d.", plot.getID());
	}
	
	@Cmd (player = true, args = "PLOT", min = 1, permissionName = "CLAN_PLOTS_MANAGE_COMMAND")
	public void editRegion(CommandContext cmd) {
		ClanPlot plot = cmd.getArgument(0);
		Player p = getPlayer();
		Prefix.INFO.sendMessage(p, "Sélectionne la région de la parcelle.");
		new RegionEditor(p, (region) -> {
			if (region == null) {
				Prefix.DEFAULT_BAD.sendMessage(p, "La région sélectionnée n'est pas correcte.");
				return;
			}
			try {
				plot.setRegion(region);
				Prefix.DEFAULT_GOOD.sendMessage(p, "La région du plot %d a été modifiée.", plot.getID());
			}catch (SQLException | IOException e) {
				e.printStackTrace();
				sendError(e);
			}
		}).edit(plot.getTrackedRegion().getRegion()).enterOrLeave();
	}
	
	@Cmd (args = { "PLOT", "INTEGER" }, min = 2, syntax = "<plot> <prix>", permissionName = "CLAN_PLOTS_MANAGE_COMMAND")
	public void setPrice(CommandContext cmd) {
		ClanPlot plot = cmd.getArgument(0);
		plot.setPrice(cmd.getArgument(1), true);
		sendSuccess("Modification du prix de la parcelle %d effectuée.", plot.getID());
	}
	
	@Cmd (player = true, args = { "PLOT" }, min = 1, syntax = "<plot>", permissionName = "CLAN_PLOTS_MANAGE_COMMAND")
	public void setSpawnpoint(CommandContext cmd) {
		ClanPlot plot = cmd.getArgument(0);
		Player p = player;
		new WaitClick(p, ItemUtils.item(Material.DIAMOND, "§bValider le point de spawn"), () -> {
			plot.setSpawn(p.getLocation(), true);
			Prefix.DEFAULT_GOOD.sendMessage(p, "Tu as modifié le spawn de la parcelle %d.", plot.getID());
		}).enterOrLeave();
		sendSuccess("Modification du prix de la parcelle %d effectuée.", plot.getID());
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
