package fr.olympa.zta.loot.packs;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.economy.OlympaMoney;
import fr.olympa.api.lines.BlinkingLine;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;

public class PackBlock {
	
	private Location location;
	private BukkitTask running;
	
	public PackBlock(Location location) {
		this.location = location;
		OlympaCore.getInstance().getHologramsManager().createHologram(location, false, true, new BlinkingLine<>((color, x) -> color + "Packs d'équipement", OlympaZTA.getInstance(), 50, ChatColor.GOLD, ChatColor.YELLOW));
	}
	
	public void click(Player p) {
		new LootPackGUI(this).create(p);
	}
	
	public synchronized void start(Player p, PackType type) {
		if (running != null) {
			Prefix.DEFAULT_BAD.sendMessage(p, "Un autre joueur est en train d'ouvrir un pack... Attend un peu avant d'essayer à ton tour !");
			return;
		}
		OlympaPlayerZTA player = OlympaPlayerZTA.get(p);
		if (player.getGameMoney().withdraw(type.getPrice())) {
			
		}else {
			Prefix.DEFAULT_BAD.sendMessage(p, "Tu n'as pas assez d'argent pour ouvrir un pack %s ! Il faut %s.", type.getName(), OlympaMoney.format(type.getPrice()));
		}
	}
	
}
