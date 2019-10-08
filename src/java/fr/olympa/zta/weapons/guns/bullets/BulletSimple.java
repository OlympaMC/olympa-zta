package fr.olympa.zta.weapons.guns.bullets;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;

import fr.olympa.zta.ZTAListener;
import fr.olympa.zta.weapons.guns.Gun;

public class BulletSimple extends Bullet{
	
	private float playerDamage, entityDamage;
	
	public BulletSimple(Gun gun, float playerDamage, float entityDamage){
		super(gun);
		this.playerDamage = playerDamage;
		this.entityDamage = entityDamage;
	}
	
	public void hit(ProjectileHitEvent e){
		Player shooter = (Player) e.getEntity().getShooter();
		if (e.getHitEntity() != null) {
			if (e.getHitEntity() instanceof Player) {
				ZTAListener.cancelDamageEvent = true;
				damage((Player) e.getHitEntity(), shooter, playerDamage + gun.damageAdded);
			}else if (e.getHitEntity() instanceof LivingEntity) {
				damage((LivingEntity) e.getHitEntity(), shooter, entityDamage + gun.damageAdded);
			}
		}
	}
	
	public void damage(LivingEntity entity, LivingEntity damager, float damage){
		entity.damage(damage, damager);
	}
	
}
