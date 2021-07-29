package fr.olympa.zta.mobs;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.mobs.custom.Mobs.Zombies;

public class PlayerHealthBar {
	
	private static final int DEAD_COOLDOWN = 3;
	private static final int DEFAULT_COOLDOWN = 6;
	
	private BossBar bar;
	private BukkitTask task;
	private int cooldown;
	
	public PlayerHealthBar(Player p) {
		bar = Bukkit.createBossBar("§4❤", BarColor.RED, BarStyle.SOLID);
		bar.setVisible(false);
		bar.addPlayer(p);
	}
	
	public void show(LivingEntity entity, double damage) {
		double health = Math.max(0, entity.getHealth() - damage);
		double maxHealth = entity.getMaxHealth();
		String name = entity.getCustomName();
		if (name == null && entity.hasMetadata("ztaZombieType")) {
			Zombies zombie = (Zombies) entity.getMetadata("ztaZombieType").get(0).value();
			name = zombie.getName();
		}
		if (name == null) {
			name = entity.getName();
		}
		bar.setTitle("§7" + name + "§7 : §c" + Integer.toString((int) Math.ceil(health)) + "/" + Integer.toString((int) maxHealth) + "❤");
		bar.setProgress(health / maxHealth);
		
		cooldown = health == 0 ? DEAD_COOLDOWN : DEFAULT_COOLDOWN;
		
		if (task == null) {
			bar.setVisible(true);
			task = Bukkit.getScheduler().runTaskTimerAsynchronously(OlympaZTA.getInstance(), () -> {
				if (--cooldown == 0) {
					hide();
				}
			}, 0, 20);
		}
	}
	
	public void hide() {
		bar.setVisible(false);
		if (task != null) {
			task.cancel();
			task = null;
		}
	}
	
}
