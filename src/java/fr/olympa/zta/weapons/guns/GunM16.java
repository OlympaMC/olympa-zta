package fr.olympa.zta.weapons.guns;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.BulletSimple;

public class GunM16 extends Gun{
	
	public GunM16(int id) {
		super(id);
	}

	public String getName(){
		return "M16";
	}
	
	public Material getItemMaterial(){
		return Material.WOODEN_PICKAXE;
	}
	
	public AmmoType getAmmoType(){
		return AmmoType.HEAVY;
	}
	
	public int getMaxAmmos(){
		return 20;
	}
	
	public int getFireRate(){
		return 10;
	}
	
	public int getChargeTime(){
		return 60;
	}
	
	public float getKnockback(){
		return CommonGunConstants.KNOCKBACK_MEDIUM;
	}
	
	public float getBulletSpeed(){
		return CommonGunConstants.BULLET_SPEED_HIGH;
	}
	
	protected GunAccuracy getAccuracy(){
		return GunAccuracy.HIGH;
	}
	
	public Bullet getFiredBullet(Player p){
		return new BulletSimple(this, 4, 5);
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.AUTOMATIC;
	}
	
	public GunMode getSecondaryMode(){
		return GunMode.BLAST;
	}
	
	public boolean isCannonAllowed(){
		return true;
	}
	
	public boolean isStockAllowed(){
		return true;
	}
	
}
