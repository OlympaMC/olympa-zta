package fr.olympa.zta.weapons.guns;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.BulletSimple;

public class GunUZI extends Gun{
	
	public String getName(){
		return "UZI";
	}
	
	public Material getItemMaterial(){
		return Material.SLIME_BALL;
	}
	
	public AmmoType getAmmoType(){
		return AmmoType.LIGHT;
	}
	
	public int getMaxAmmos(){
		return 25;
	}
	
	public int getFireRate(){
		return 4;
	}
	
	public int getChargeTime(){
		return 50;
	}
	
	public float getKnockback(){
		return 0;
	}
	
	public float getBulletSpeed(){
		return Bullet.SPEED_LOW;
	}
	
	protected float getBulletSpread(){
		return 0.2f;
	}
	
	public Bullet getFiredBullet(Player p){
		return new BulletSimple(this, 3, 2);
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.AUTOMATIC;
	}
	
}
