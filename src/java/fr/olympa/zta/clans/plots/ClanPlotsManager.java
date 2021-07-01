package fr.olympa.zta.clans.plots;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

import fr.olympa.api.common.sql.SQLColumn;
import fr.olympa.api.common.sql.SQLTable;
import fr.olympa.api.spigot.region.Region;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.clans.ClansManagerZTA;
import fr.olympa.zta.utils.map.DynmapLink;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class ClanPlotsManager implements Listener {

	public static final NamespacedKey SIGN_KEY = new NamespacedKey(OlympaZTA.getInstance(), "plotID");
	/*private static final String tableName = "`zta_clan_plots`";
	
	private static final OlympaStatement createPlot = new OlympaStatement("INSERT INTO " + tableName + " (`region`, `price`, `sign`, `spawn`) VALUES (?, ?, ?, ?)", true);
	public static final OlympaStatement updatePlotClan = new OlympaStatement("UPDATE " + tableName + " SET `clan` = ? WHERE (`id` = ?)");
	public static final OlympaStatement updatePlotNextPayment = new OlympaStatement("UPDATE " + tableName + " SET `next_payment` = ? WHERE (`id` = ?)");*/
	
	private SQLTable<ClanPlot> table;
	
	public SQLColumn<ClanPlot> columnID = new SQLColumn<ClanPlot>("id", "INT(11) UNSIGNED NOT NULL AUTO_INCREMENT", Types.INTEGER).setPrimaryKey(ClanPlot::getID);
	public SQLColumn<ClanPlot> columnRegion = new SQLColumn<ClanPlot>("region", "VARBINARY(8000) NOT NULL", Types.VARBINARY).setUpdatable();
	public SQLColumn<ClanPlot> columnClan = new SQLColumn<ClanPlot>("clan", "INT NULL DEFAULT -1", Types.INTEGER).setUpdatable();
	public SQLColumn<ClanPlot> columnSign = new SQLColumn<ClanPlot>("sign", "VARCHAR(100) NOT NULL", Types.VARCHAR);
	public SQLColumn<ClanPlot> columnSpawn = new SQLColumn<ClanPlot>("spawn", "VARCHAR(100) NOT NULL", Types.VARCHAR);
	public SQLColumn<ClanPlot> columnPrice = new SQLColumn<ClanPlot>("price", "INT NOT NULL", Types.INTEGER);
	public SQLColumn<ClanPlot> columnNextPayment = new SQLColumn<ClanPlot>("next_payment", "BIGINT NOT NULL DEFAULT -1", Types.BIGINT).setUpdatable();
	public SQLColumn<ClanPlot> columnLastChiefPayment = new SQLColumn<ClanPlot>("last_chief_payment", "BIGINT NOT NULL DEFAULT 0", Types.BIGINT).setUpdatable();

	private Map<Integer, ClanPlot> plots = new HashMap<>();

	private Location bookLocation;
	private Book book;
	
	public ClanPlotsManager(ClansManagerZTA clans, Location bookLocation) throws SQLException {
		this.bookLocation = bookLocation;
		
		table = new SQLTable<>("zta_clan_plots",
				Arrays.asList(columnID, columnRegion, columnClan, columnSign, columnSpawn, columnPrice, columnNextPayment),
				resultSet -> {
					try {
						ClanPlot plot = new ClanPlot(this, resultSet.getInt("id"), SpigotUtils.deserialize(resultSet.getBytes("region")), resultSet.getInt("price"), SpigotUtils.convertStringToLocation(resultSet.getString("sign")), SpigotUtils.convertStringToLocation(resultSet.getString("spawn")));
						DynmapLink.showClanPlot(plot);
						int clanID = resultSet.getInt("clan");
						if (clanID != -1) plot.setClan(clans.getClan(clanID), false);
						plot.setNextPayment(resultSet.getLong("next_payment"), false, true);
						plot.setLastChiefPayment(resultSet.getLong("last_chief_payment"), false);
						return plot;
					}catch (Exception ex) {
						OlympaZTA.getInstance().getLogger().severe("Une erreur est BookMeta survenue lors du chargement d'une parcelle.");
						ex.printStackTrace();
						return null;
					}
				});
		table.createOrAlter();

		plots = table.selectAll(null).stream().filter(Objects::nonNull).collect(Collectors.toMap(ClanPlot::getID, Function.identity()));
		
		updateBook();
		
		new ClanPlotsCommand(this).register();
	}
	
	public void updateBook() {
		net.kyori.adventure.inventory.Book.Builder builder = Book.builder();
		builder.title(Component.text("Parcelles de clans"));
		builder.author(Component.text("Olympa"));
		
		List<ClanPlot> rent = plots.values().stream().filter(x -> x.getClan() != null).collect(Collectors.toList());
		List<ClanPlot> free = plots.values().stream().filter(x -> x.getClan() == null).collect(Collectors.toList());
		
		Builder compo = Component.text().color(NamedTextColor.DARK_GRAY);
		compo.append(Component.text("\nΩ", NamedTextColor.GOLD));
		compo.append(Component.text(" | Parcelles de Clans\n\n\n").decorate(TextDecoration.BOLD));
		compo.append(Component.text("§7➤ ", NamedTextColor.GRAY));
		compo.append(Component.text(plots.size() + " parcelles sur    le monde\n\n", NamedTextColor.BLACK));
		compo.append(Component.text("§7➤ ", NamedTextColor.GRAY));
		compo.append(Component.text(rent.size() + " parcelles ").append(Component.text("   louées\n", NamedTextColor.RED, TextDecoration.BOLD)));
		compo.append(Component.text("§7➤ ", NamedTextColor.GRAY));
		compo.append(Component.text(free.size() + " parcelles ").append(Component.text("   libres", NamedTextColor.GOLD, TextDecoration.BOLD)));
		
		int amount = 3;
		for (int i = 0; i < rent.size(); i++) {
			if (amount++ >= 3) {
				builder.addPage(compo.build());
				amount = 0;
				compo = Component.text().content(" Parcelles louées :").color(NamedTextColor.DARK_GRAY);
			}
			ClanPlot plot = rent.get(i);
			compo.append(Component.text("\n\n➤ ", NamedTextColor.GRAY));
			compo.append(Component.text("#" + plot.getID(), NamedTextColor.GOLD));
			compo.append(Component.text(" (x:" + plot.getSign().getBlockX() + " y:" + plot.getSign().getBlockZ() + ") louée à "));
			TextComponent clanCompo = Component.text(plot.getClan().getName(), NamedTextColor.GOLD);
			compo.append(clanCompo);
			compo.append(Component.text(" → "));
			compo.append(Component.text(plot.getExpirationDate(), NamedTextColor.GOLD));
		}
		
		amount = 4;
		for (int i = 0; i < free.size(); i++) {
			if (amount++ >= 4) {
				builder.addPage(compo.build());
				amount = 0;
				compo = Component.text().content(" Parcelles libres :").color(NamedTextColor.DARK_GRAY);
			}
			ClanPlot plot = free.get(i);
			compo.append(Component.text("\n\n➤ ", NamedTextColor.GRAY));
			compo.append(Component.text("#" + plot.getID(), NamedTextColor.GOLD));
			compo.append(Component.text(" (x:" + plot.getSign().getBlockX() + " y:" + plot.getSign().getBlockZ() + ") à louer pour "));
			compo.append(Component.text(plot.getPriceFormatted(), NamedTextColor.GOLD));
		}
		builder.addPage(compo.build());
		
		book = builder.build();
	}

	public ClanPlot create(Region region, int price, Block sign, Location spawn) throws SQLException, IOException {
		Location signLocation = sign.getLocation();

		ResultSet resultSet = table.insert(SpigotUtils.serialize(region), SpigotUtils.convertLocationToString(signLocation), SpigotUtils.convertLocationToString(spawn), price);
		resultSet.next();

		ClanPlot plot = new ClanPlot(this, resultSet.getInt(1), region, price, signLocation, spawn);
		DynmapLink.showClanPlot(plot);
		plot.updateSign();
		plots.put(plot.getID(), plot);
		resultSet.close();

		return plot;
	}

	public ClanPlot getPlot(Location location) {
		for (ClanPlot plot : plots.values()) {
			if (plot.getTrackedRegion().getRegion().isIn(location)) return plot;
		}
		return null;
	}
	
	public Map<Integer, ClanPlot> getPlots() {
		return plots;
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onInteract(PlayerInteractEvent e) {
		Block clickedBlock = e.getClickedBlock();
		if (clickedBlock == null) return;

		if (clickedBlock.getType().name().contains("_SIGN")) {
			Sign sign = (Sign) clickedBlock.getState();
			if (sign.getPersistentDataContainer().has(SIGN_KEY, PersistentDataType.INTEGER)) plots.get(sign.getPersistentDataContainer().get(SIGN_KEY, PersistentDataType.INTEGER)).signClick(e.getPlayer());
		}
		if (clickedBlock.getLocation().equals(bookLocation)) {
			e.setCancelled(true);
			Bukkit.getScheduler().runTask(OlympaZTA.getInstance(), () -> e.getPlayer().openBook(book));
		}
	}

}
