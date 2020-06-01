package fr.olympa.zta.weapons.guns.bullets;

import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;

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
			float damage;
			if (e.getHitEntity() instanceof Player) {
				damage = playerDamage;
			}else {
				damage = entityDamage;
			}
			damage += gun.damageAdded;
			if (e.getEntity().getLocation().getY() - hitEntity.getLocation().getY() > 1.4) {
				damage *= 1.3;
				shooter.playSound(shooter.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5F, 1);
			}
			damage(hitEntity, shooter, damage);
		}
	}
	
	public void damage(LivingEntity entity, LivingEntity damager, float damage) {
		entity.damage(damage, damager);
		entity.setNoDamageTicks(NO_DAMAGE_TICKS);
	}

}
