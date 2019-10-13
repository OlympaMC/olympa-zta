package fr.olympa.zta.weapons.guns;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.BulletSimple;

public class GunG19 extends Gun{
	
	public String getName(){
		return "Glock 19";
	}
	
	public Material getItemMaterial(){
		return Material.GOLDEN_AXE;
	}
	
	public AmmoType getAmmoType(){
		return AmmoType.HANDWORKED;
	}
	
	protected int getMaxAmmos(){
		return 16;
	}
	
	protected int getFireRate(){
		return 10;
	}
	
	protected int getChargeTime(){
		return 40;
	}
	
	protected float getKnockback(){
		return 0;
	}
	
	protected float getBulletSpeed(){
		return CommonGunConstants.BULLET_SPEED_MEDIUM;
	}
	
	protected GunAccuracy getAccuracy(){
		return GunAccuracy.HIGH;
	}
	
	public Bullet getFiredBullet(Player p){
		return new BulletSimple(this, 4, 1);
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.AUTOMATIC;
	}
	
	public GunMode getSecondaryMode(){
		return GunMode.SEMI_AUTOMATIC;
	}
	
	public boolean isCannonAllowed(){
		return true;
	}
	
}
