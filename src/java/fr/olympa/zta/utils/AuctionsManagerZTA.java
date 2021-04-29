package fr.olympa.zta.utils;

import java.io.IOException;
import java.sql.SQLException;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.auctions.Auction;
import fr.olympa.api.auctions.AuctionsManager;
import fr.olympa.api.economy.tax.TaxManager;
import fr.olympa.zta.OlympaZTA;

public class AuctionsManagerZTA extends AuctionsManager {
	
	public AuctionsManagerZTA(Plugin plugin, String table, TaxManager tax) throws SQLException, ClassNotFoundException, IOException {
		super(plugin, table, tax);
	}
	
	@Override
	public synchronized void boughtAuction(Auction auction) throws SQLException {
		super.boughtAuction(auction);
		OlympaZTA.getInstance().gunRegistry.loadFromItems(new ItemStack[] { auction.item });
	}
	
}
