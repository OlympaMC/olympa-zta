package fr.olympa.zta.weapons.guns;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.BulletSimple;

public class GunSkorpion extends Gun{
	
	public String getName(){
		return "Skorpion VZ64";
	}
	
	public Material getItemMaterial(){
		return Material.MAGMA_CREAM;
	}
	
	public AmmoType getAmmoType(){
		return AmmoType.HANDWORKED;
	}
	
	protected int getMaxAmmos(){
		return 24;
	}
	
	protected int getFireRate(){
		return 4;
	}
	
	protected int getChargeTime(){
		return 50;
	}
	
	protected float getKnockback(){
		return 0;
	}
	
	protected float getBulletSpeed(){
		return CommonGunConstants.BULLET_SPEED_LOW;
	}
	
	protected GunAccuracy getAccuracy(){
		return GunAccuracy.MEDIUM;
	}
	
	public Bullet getFiredBullet(Player p){
		return new BulletSimple(this, 4, 1);
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.AUTOMATIC;
	}
	
}
