package fr.olympa.zta.weapons.guns;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.BulletSimple;

public class GunAK extends Gun{
	
	public String getName(){
		return "AK-20";
	}
	
	public Material getItemMaterial(){
		return Material.GOLDEN_PICKAXE;
	}
	
	public AmmoType getAmmoType(){
		return AmmoType.HANDWORKED;
	}
	
	protected int getMaxAmmos(){
		return 20;
	}
	
	protected int getFireRate(){
		return 15;
	}
	
	protected int getChargeTime(){
		return 70;
	}
	
	protected float getKnockback(){
		return CommonGunConstants.KNOCKBACK_LOW;
	}
	
	protected float getBulletSpeed(){
		return CommonGunConstants.BULLET_SPEED_HIGH;
	}
	
	protected GunAccuracy getAccuracy(){
		return GunAccuracy.HIGH;
	}
	
	public Bullet getFiredBullet(Player p){
		return new BulletSimple(this, 6, 3);
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.AUTOMATIC;
	}
	
	public boolean isScopeAllowed(){
		return true;
	}
	
	public boolean isCannonAllowed(){
		return true;
	}
	
	public boolean isStockAllowed(){
		return true;
	}
	
}
