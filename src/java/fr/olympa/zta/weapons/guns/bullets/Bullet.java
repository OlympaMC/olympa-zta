package fr.olympa.zta.weapons.guns.bullets;

import java.util.Random;

import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
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
		float speed = gun.bulletSpeed.getValue();
		Vector velocity = p.getLocation().getDirection().multiply(speed);

		float bulletSpread = gun.bulletSpread.getValue();
		if (bulletSpread != 0) {
			float bulletSpreadHalf = bulletSpread / 2;
			velocity.add(new Vector(random.nextFloat() * bulletSpread - bulletSpreadHalf, random.nextFloat() * bulletSpread - bulletSpreadHalf, random.nextFloat() * bulletSpread - bulletSpreadHalf));
		}

		boolean highVelocity = speed >= 4.5;
		Projectile projectile = p.launchProjectile(highVelocity ? LlamaSpit.class : Snowball.class, velocity);
		projectile.setMetadata("bullet", metadata);
		projectile.setBounce(false);
		projectile.setPersistent(false);

		if (highVelocity) { // nécessaire ? à supprimer peut-être
			new BukkitRunnable() {
				World world = projectile.getWorld();
				int previousTicksLived;
				
				public void run() {
					if (projectile.isValid()) {
						if (projectile.getTicksLived() == previousTicksLived) return;
						previousTicksLived = projectile.getTicksLived();
						Vector velocity = projectile.getVelocity();
						world.spawnParticle(Particle.SMOKE_LARGE, projectile.getLocation(), 0, velocity.getX(), velocity.getY(), velocity.getZ(), velocity.normalize().length(), null, true);
					}else cancel();
				}
			}.runTaskTimerAsynchronously(OlympaZTA.getInstance(), 0, 4);
		}
	}
	
	/**
	 * @param e Event appelé quand la balle touche quelque chose/quelqu'un
	 */
	public abstract void hit(ProjectileHitEvent e);
	
	@FunctionalInterface
	public static interface BulletCreator {
		public Bullet create(Gun gun, float playerDamage, float entityDamage);
	}
	
}
