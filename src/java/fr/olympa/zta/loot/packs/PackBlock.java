package fr.olympa.zta.loot.packs;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import fr.olympa.api.economy.OlympaMoney;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.lines.BlinkingLine;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.loot.creators.LootCreator;
import fr.olympa.zta.loot.creators.LootCreator.Loot;

public class PackBlock {
	
	private static final ItemStack FLOATING_ITEM = ItemUtils.item(Material.CHEST, "Pack");
	
	private Random random = new Random();
	private Location location;
	private BukkitTask running;
	
	public PackBlock(Location location) {
		this.location = location.clone().add(0.5, 1, 0.5);
		OlympaCore.getInstance().getHologramsManager().createHologram(this.location.clone().add(0, 0.1, 0), false, true, new BlinkingLine<>((color, x) -> color + "§lPacks d'équipement", OlympaZTA.getInstance(), 50, ChatColor.GOLD, ChatColor.YELLOW));
	}
	
	public void click(Player p) {
		new LootPackGUI(this).create(p);
	}
	
	public synchronized void start(Player p, PackType type) {
		if (p.getInventory().firstEmpty() == -1) {
			Prefix.DEFAULT_BAD.sendMessage(p, "Il n'y a plus de place dans ton inventaire, libères-en une avant d'ouvrir la pack !");
			return;
		}
		if (running != null) {
			Prefix.DEFAULT_BAD.sendMessage(p, "Un autre joueur est en train d'ouvrir un pack... Attend un peu avant d'essayer à ton tour !");
			return;
		}
		OlympaPlayerZTA player = OlympaPlayerZTA.get(p);
		if (player.getGameMoney().has(type.getPrice())) {
			Prefix.DEFAULT.sendMessage(p, "Tu vas ouvrir un pack %s...", type.getName());
			Item item = location.getWorld().dropItem(location.clone().add(0, 2.5, 0), FLOATING_ITEM);
			item.setGravity(false);
			item.setPersistent(false);
			item.setVelocity(new Vector());
			item.setPickupDelay(Short.MAX_VALUE);
			running = Bukkit.getScheduler().runTaskTimer(OlympaZTA.getInstance(), new Runnable() {
				int y = 0;
				@Override
				public void run() {
					if (y++ == 25) {
						running.cancel();
						running = null;
						item.remove();
						
						if (player.getGameMoney().withdraw(type.getPrice())) {
							location.getWorld().spawnParticle(Particle.FLAME, location.getX(), location.getY() + (y / 10D), location.getZ(), 15, 0D, 0D, 0D, 0.08);
							location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
							List<LootCreator> picked = type.pick(random);
							ItemStack[] items = picked.stream().map(x -> {
								Loot loot = x.create(random);
								ItemStack realItem = loot.getRealItem();
								return realItem == null ? loot.getItem() : realItem;
							}).toArray(ItemStack[]::new);
							SpigotUtils.giveItems(p, items);
							Prefix.DEFAULT_GOOD.sendMessage(Bukkit.getOnlinePlayers(), "§2§l%s §atrouve %s dans un pack %s !", p.getName(), picked.stream().map(LootCreator::getTitle).collect(Collectors.joining("§a, §e", "§e", "§a")), type.getName());
						}else {
							Prefix.DEFAULT_BAD.sendMessage(p, "Tu as cru nous avoir ? Reviens avec assez d'argent pour acheter le pack.");
							Prefix.DEFAULT_BAD.sendMessage(Bukkit.getOnlinePlayers(), "%s a cru gruger le game en ne payant pas son pack ! Jetez-lui des pierres !", p.getName());
						}
					}else {
						location.getWorld().spawnParticle(Particle.CRIT, location.getX(), location.getY() + (y / 10D), location.getZ(), 4, 0D, 0D, 0D, 0);
					}
				}
			}, 1, 3);
		}else {
			Prefix.DEFAULT_BAD.sendMessage(p, "Tu n'as pas assez d'argent pour ouvrir un pack %s ! Il faut %s.", type.getName(), OlympaMoney.format(type.getPrice()));
		}
	}
	
}
