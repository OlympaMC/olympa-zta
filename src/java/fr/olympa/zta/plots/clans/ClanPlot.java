package fr.olympa.zta.plots.clans;

import fr.olympa.api.region.Region;
import fr.olympa.zta.clans.ClanZTA;

public class ClanPlot {

	private final int id;
	private final Region region;
	private final int price;

	private ClanZTA clan;

	public ClanPlot(int id, Region region, int price) {
		this.id = id;
		this.region = region;
		this.price = price;
	}

	public ClanZTA getClan() {
		return clan;
	}

	public void setClan(ClanZTA clan) {
		this.clan = clan;
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

}
