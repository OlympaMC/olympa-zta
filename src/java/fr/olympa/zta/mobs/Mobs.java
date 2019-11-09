package fr.olympa.zta.mobs;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Mobs {

	public static final List<PotionEffect> ZOMBIE_EFFECTS = Arrays.asList(new PotionEffect(PotionEffectType.SPEED, 999999, 0, false, false), new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 999999, 0, false, false));
	public static final List<PotionEffect> MOMIFIED_ZOMBIE_EFFECTS = Arrays.asList(new PotionEffect(PotionEffectType.SPEED, 999999, 0, false, false), new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 999999, 1, false, false), new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999, 0, false, false));
	private static Random random = new Random();

	public static void spawnZombie(Location location) {
		location.setYaw(random.nextInt(360));
		location.add(0.5, 0, 0.5); // sinon entités sont spawnées dans les coins des blocs et risquent de s'étouffer
		Zombie zombie = location.getWorld().spawn(location, Zombie.class);
		zombie.addPotionEffects(ZOMBIE_EFFECTS);
		zombie.getEquipment().clear();
	}

	public static Zombie spawnMomifiedZombie(Player p) {
		Zombie zombie = p.getWorld().spawn(p.getLocation(), Zombie.class);
		zombie.addPotionEffects(MOMIFIED_ZOMBIE_EFFECTS);
		zombie.getEquipment().setArmorContents(p.getInventory().getArmorContents());
		zombie.setBaby(false);
		zombie.setCustomName(p.getName() + " momifié");
		return zombie;
	}

}
