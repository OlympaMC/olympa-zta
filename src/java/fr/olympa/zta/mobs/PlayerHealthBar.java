package fr.olympa.zta.mobs;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.zta.OlympaZTA;

public class PlayerHealthBar {
	
	private static final int DEFAULT_COOLDOWN = 12;
	
	private BossBar bar;
	private LivingEntity entity;
	private BukkitTask task;
	private int cooldown;
	private double lastHealth;
	
	public PlayerHealthBar(Player p) {
		bar = Bukkit.createBossBar("§4❤", BarColor.RED, BarStyle.SOLID);
		bar.setVisible(false);
		bar.addPlayer(p);
	}
	
	public void show(LivingEntity newEntity) {
		cooldown = DEFAULT_COOLDOWN;
		if (entity == newEntity) return;
		entity = newEntity;
		
		lastHealth = entity.getHealth();
		updateBar();
		lastHealth = -1;
		
		if (task == null) {
			bar.setVisible(true);
			task = Bukkit.getScheduler().runTaskTimerAsynchronously(OlympaZTA.getInstance(), () -> {
				if (--cooldown == 0) {
					hide();
				}else {
					double newHealth = entity.getHealth();
					if (Math.abs(newHealth - lastHealth) <= 0.5) return; // pas d'update si trop faible différence
					lastHealth = newHealth;
					updateBar();
				}
			}, 10, 10);
		}
	}
	
	public void hide() {
		bar.setVisible(false);
		if (task != null) {
			task.cancel();
			task = null;
		}
	}
	
	private void updateBar() {
		double maxHealth = entity.getMaxHealth();
		bar.setTitle("§7" + entity.getName() + "§7 : §c" + Integer.toString((int) lastHealth) + "/" + Integer.toString((int) maxHealth) + "❤");
		bar.setProgress(lastHealth / maxHealth);
	}
	
}
