package fr.olympa.zta.clans.plots;

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
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;

public class ClanPlotsCommand extends ComplexCommand {
	
	private ClanPlotsManager manager;
	
	public ClanPlotsCommand(ClanPlotsManager manager) {
		super(OlympaZTA.getInstance(), "clanplots", "Permet de gérer les parcelles de clan.", ZTAPermissions.CLAN_PLOTS_MANAGE_COMMAND);
		this.manager = manager;
		
		super.addArgumentParser("PLOT", sender -> manager.getPlots().keySet().stream().map(x -> x.toString()).collect(Collectors.toList()), x -> manager.getPlots().get(Integer.parseInt(x)));
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
	public void updateSign(CommandContext cmd) {
		ClanPlot plot = cmd.getArgument(0);
		plot.updateSign();
		sendSuccess("Le panneau a été mis à jour.");
	}
	
}
