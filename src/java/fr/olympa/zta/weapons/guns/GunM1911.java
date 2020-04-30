package fr.olympa.zta.weapons.guns;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.BulletSimple;

/**
 * Colt M1911, revolver, munitions légères
 */
public class GunM1911 extends Gun{
	
	public static final String NAME = "Colt M1911";
	public static final Material TYPE = Material.WOODEN_AXE;

	public GunM1911(int id) {
		super(id);
	}

	public String getName(){
		return NAME;
	}
	
	public Material getItemMaterial(){
		return TYPE;
	}
	
	public AmmoType getAmmoType(){
		return AmmoType.LIGHT;
	}
	
	public int getMaxAmmos(){
		return 7;
	}
	
	public int getFireRate(){
		return 10;
	}
	
	public int getChargeTime(){
		return 40;
	}
	
	public float getKnockback(){
		return 0f;
	}
	
	public float getBulletSpeed(){
		return CommonGunConstants.BULLET_SPEED_MEDIUM;
	}
	
	protected GunAccuracy getAccuracy(){
		return GunAccuracy.HIGH;
	}
	
	public Bullet getFiredBullet(Player p){
		return new BulletSimple(this, 3, 2);
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.SINGLE;
	}
	
	public boolean isCannonAllowed(){
		return true;
	}
	
}
