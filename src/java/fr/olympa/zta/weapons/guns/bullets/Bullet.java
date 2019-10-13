package fr.olympa.zta.weapons.guns.bullets;

import java.util.Random;

import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.weapons.guns.Gun;

public abstract class Bullet{
	
	private static final Random random = new Random();
	
	private final FixedMetadataValue metadata = new FixedMetadataValue(OlympaZTA.getInstance(), this);
	protected final Gun gun;
	
	public Bullet(Gun gun){
		this.gun = gun;
	}
	
	public void launchProjectile(Player p){
		Vector velocity = p.getLocation().getDirection().multiply(gun.bulletSpeed.getValue());
		float bulletSpread = gun.bulletSpread.getValue();
		if (bulletSpread != 0) {
			float bulletSpreadHalf = bulletSpread / 2;
			velocity.add(new Vector(random.nextFloat() * bulletSpread - bulletSpreadHalf, random.nextFloat() * bulletSpread - bulletSpreadHalf, random.nextFloat() * bulletSpread - bulletSpreadHalf));
		}
		Snowball projectile = p.launchProjectile(Snowball.class, velocity);
		projectile.setMetadata("bullet", metadata);
		new BukkitRunnable(){
			World world = projectile.getWorld();
			public void run(){
				if (projectile.isValid()) {
					Vector velocity = projectile.getVelocity();
					world.spawnParticle(Particle.SMOKE_LARGE, projectile.getLocation(), 0, velocity.getX(), velocity.getY(), velocity.getZ(), velocity.normalize().length(), null, true);
				}else cancel();
			}
		}.runTaskTimerAsynchronously(OlympaZTA.getInstance(), 0, 4);
	}
	
	/**
	 * @param e Event appelé quand la balle touche quelque chose/quelqu'un
	 */
	public abstract void hit(ProjectileHitEvent e);
	
}
