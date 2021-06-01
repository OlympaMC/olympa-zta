package fr.olympa.zta.clans.plots;

import java.util.List;
import java.util.function.Supplier;

import fr.olympa.api.common.command.Paginator;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ClanPlotPaginator extends Paginator<ClanPlot> {
	
	private String parameter;
	private Supplier<List<ClanPlot>> plotsSupplier;
	
	public ClanPlotPaginator(int pageSize, String title, String parameter, Supplier<List<ClanPlot>> plotsSupplier) {
		super(pageSize, title);
		this.parameter = parameter;
		this.plotsSupplier = plotsSupplier;
	}
	
	@Override
	protected List<ClanPlot> getObjects() {
		return plotsSupplier.get();
	}
	
	@Override
	protected BaseComponent getObjectDescription(ClanPlot object) {
		TextComponent commandCompo = new TextComponent(TextComponent.fromLegacyText("§7➤ Parcelle §6#" + object.getID() + "§7, à §6" + object.getPriceFormatted() + "§7, " + (object.getClan() == null ? "§6non louée" : "louée à §6" + object.getClan().getName() + "§7 jusqu'au §6" + object.getExpirationDate())));
		commandCompo.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§bClique pour afficher des détails sur la parcelle !")));
		commandCompo.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clanplots info " + object.getID()));
		return commandCompo;
	}

	@Override
	protected String getCommand(int page) {
		return "/clanplots list " + parameter + " " + page;
	}
	
}
