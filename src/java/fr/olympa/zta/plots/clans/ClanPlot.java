package fr.olympa.zta.plots.clans;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import fr.olympa.api.region.Region;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.clans.ClanZTA;

public class ClanPlot {

	private final int id;
	private final Region region;
	private final int price;
	private final Location sign;

	private ClanZTA clan;

	public ClanPlot(int id, Region region, int price, Location sign) {
		this.id = id;
		this.region = region;
		this.price = price;
		this.sign = sign;
	}

	public ClanZTA getClan() {
		return clan;
	}

	public void setClan(ClanZTA clan) {
		this.clan = clan;
		
		Sign sign = (Sign) this.sign.getBlock().getState();
		
		sign.setLine(0, "[" + price + "/semaine]");
		if (clan == null) {
			sign.setLine(2, "Parcelle à vendre");
		}else {
			clan.cachedPlot = this;
			sign.setLine(2, clan.getName());
		}
	}

	public int getID() {
		return id;
	}

	public Region getRegion() {
		return region;
	}

	public int getPrice() {
		return price;
	}

	public Location getSign() {
		return sign;
	}

	public boolean onInteract(Player player) {
		return OlympaPlayerZTA.get(player).getClan() != clan;
	}

	public void signClick(Player p) {
		OlympaPlayerZTA player = OlympaPlayerZTA.get(p);
		ClanZTA clan = player.getClan();
		if (clan == null) {
			Prefix.DEFAULT_BAD.sendMessage(p, "Cette parcelle n'est louable qu'à un clan.");
			return;
		}
		if (clan.cachedPlot != null) {
			Prefix.DEFAULT_BAD.sendMessage(p, "Ton clan loue déjà une parcelle !");
			return;
		}
		if (clan.getChief() != player.getInformation()) {
			Prefix.DEFAULT_BAD.sendMessage(p, "Seul le chef du clan peut décider de louer une parcelle.");
			return;
		}
	}

}
