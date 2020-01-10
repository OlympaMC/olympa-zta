package fr.olympa.zta.weapons.guns.bullets;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.ProjectileHitEvent;

import fr.olympa.zta.weapons.guns.Gun;

public class BulletExplosive extends Bullet{
	
	private float speed;
	private float power;
	
	public BulletExplosive(Gun gun, float speed, float power){
		super(gun);
		this.speed = speed;
		this.power = power;
	}
	
	public void hit(ProjectileHitEvent e){
		Location lc = e.getHitEntity() != null ? e.getHitEntity().getLocation() : e.getHitBlock().getLocation();
		lc.getWorld().createExplosion(lc.getX(), lc.getY(), lc.getZ(), power, false, false, (Entity) e.getEntity().getShooter());
	}
	
	public float getBulletSpeed(){
		return speed;
	}
	
}
