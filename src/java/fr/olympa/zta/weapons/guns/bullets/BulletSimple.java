package fr.olympa.zta.weapons.guns.bullets;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.weapons.Knife;
import fr.olympa.zta.weapons.WeaponsListener;
import fr.olympa.zta.weapons.guns.Gun;

public class BulletSimple extends Bullet{
	
	public static final int NO_DAMAGE_TICKS = 1;

	private float playerDamage, entityDamage;
	
	public BulletSimple(Gun gun, float playerDamage, float entityDamage){
		super(gun);
		this.playerDamage = playerDamage;
		this.entityDamage = entityDamage;
	}
	
	public void hit(ProjectileHitEvent e){
		Player shooter = (Player) e.getEntity().getShooter();
		if (e.getHitEntity() != null && e.getHitEntity() instanceof LivingEntity) {
			LivingEntity hitEntity = (LivingEntity) e.getHitEntity();
			WeaponsListener.cancelDamageEvent = true;
			float damage = hitEntity instanceof Player ? playerDamage : entityDamage;

			Location blood;
			boolean stats = !hitEntity.hasMetadata("training");
			if (isHeadShot(e.getEntity(), hitEntity)) {
				damage *= 1.5;
				shooter.playSound(shooter.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5F, 1);
				blood = hitEntity.getEyeLocation();
				if (stats) OlympaPlayerZTA.get(shooter).headshots.increment();
			}else {
				blood = hitEntity.getLocation().add(0, 1, 0);
				if (stats) OlympaPlayerZTA.get(shooter).otherShots.increment();
			}
			hitEntity.getWorld().spawnParticle(Particle.BLOCK_CRACK, blood, 5, Knife.BLOOD_DATA);
			damage(hitEntity, shooter, damage);
		}else if (e.getHitBlock() != null) {
			e.getEntity().getWorld().spawnParticle(Particle.BLOCK_CRACK, e.getHitBlock().getLocation().add(0, 0.5, 0), 3, e.getHitBlock().getBlockData());
		}
	}
	
	public boolean isHeadShot(Projectile proj, LivingEntity entity) {
		BoundingBox bb = BoundingBox.of(entity.getEyeLocation(), 0.5, 0.5, 0.5);
		for (double i = 0.0D; i <= gun.bulletSpeed.getValue(); i += 0.8D) {
			Vector finalLoc = proj.getLocation().toVector();
			Vector direction = proj.getVelocity().normalize();
			direction.multiply(i);
			finalLoc.add(direction);
			if (bb.contains(finalLoc)) return true;
		}
		return false;
	}

	public void damage(LivingEntity entity, LivingEntity damager, float damage) {
		entity.damage(damage, damager);
		entity.setNoDamageTicks(NO_DAMAGE_TICKS);
	}

}
