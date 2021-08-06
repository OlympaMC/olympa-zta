package fr.olympa.zta.utils;

import java.io.IOException;
import java.sql.SQLException;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.spigot.auctions.Auction;
import fr.olympa.api.spigot.auctions.AuctionsManager;
import fr.olympa.api.spigot.economy.MoneyPlayerInterface;
import fr.olympa.api.spigot.economy.tax.TaxManager;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;

public class AuctionsManagerZTA extends AuctionsManager {
	
	public AuctionsManagerZTA(Plugin plugin, String table, TaxManager tax) throws SQLException, ClassNotFoundException, IOException {
		super(plugin, table, tax);
	}
	
	@Override
	public synchronized void boughtAuction(Auction auction) throws SQLException {
		super.boughtAuction(auction);
		OlympaZTA.getInstance().gunRegistry.loadFromItems(new ItemStack[] { auction.item });
	}
	
	@Override
	public int getMaxAuctions(MoneyPlayerInterface player) {
		if (ZTAPermissions.GROUP_LEGENDE.hasPermission(player)) return 10;
		if (ZTAPermissions.GROUP_SAUVEUR.hasPermission(player)) return 8;
		if (ZTAPermissions.GROUP_SURVIVANT.hasPermission(player)) return 6;
		return 5;
	}
	
}
