package fr.olympa.zta.weapons.guns.created;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.CommonGunConstants;
import fr.olympa.zta.weapons.guns.Gun;
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
		return 60;
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
	
	@Override
	protected float getBulletPlayerDamage() {
		return 3;
	}
	
	@Override
	protected float getBulletEntityDamage() {
		return 3;
	}
	
	public Bullet getFiredBullet(Player p, float playerDamage, float entityDamage){
		return new BulletSimple(this, playerDamage, entityDamage);
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.SINGLE;
	}
	
	@Override
	protected float getFireVolume() {
		return CommonGunConstants.SOUND_VOLUME_LOW;
	}
	
	public boolean isCannonAllowed(){
		return true;
	}
	
}
