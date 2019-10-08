package fr.olympa.zta.weapons.guns;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.BulletSimple;

public class Gun870 extends Gun{
	
	public String getName(){
		return "Remington 870 Express";
	}
	
	public Material getItemMaterial(){
		return Material.WOODEN_HOE;
	}
	
	public AmmoType getAmmoType(){
		return AmmoType.LIGHT;
	}
	
	public int getMaxAmmos(){
		return 4;
	}
	
	public int getFireRate(){
		return 30;
	}
	
	public int getChargeTime(){
		return 70;
	}
	
	public float getKnockback(){
		return 0.05f;
	}
	
	public float getBulletSpeed(){
		return Bullet.SPEED_LOW;
	}
	
	protected float getBulletSpread(){
		return 0.2f;
	}
	
	public Bullet getFiredBullet(Player p){
		return new BulletSimple(this, 4, 5);
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.SINGLE;
	}
	
	public boolean isCannonAllowed(){
		return true;
	}
	
	public boolean isScopeAllowed(){
		return true;
	}
	
}
