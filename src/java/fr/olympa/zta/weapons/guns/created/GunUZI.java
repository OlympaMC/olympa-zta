package fr.olympa.zta.weapons.guns.created;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.CommonGunConstants;
import fr.olympa.zta.weapons.guns.Gun;
import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.BulletSimple;

public class GunUZI extends Gun{
	
	public static final String NAME = "UZI";
	public static final Material TYPE = Material.SLIME_BALL;

	public GunUZI(int id) {
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
		return 25;
	}
	
	public int getFireRate(){
		return 3;
	}
	
	public int getChargeTime(){
		return 50;
	}
	
	public float getKnockback(){
		return 0;
	}
	
	public float getBulletSpeed(){
		return CommonGunConstants.BULLET_SPEED_ULTRA_LOW;
	}
	
	protected GunAccuracy getAccuracy(){
		return GunAccuracy.MEDIUM;
	}
	
	@Override
	protected float getBulletPlayerDamage() {
		return 2;
	}
	
	@Override
	protected float getBulletEntityDamage() {
		return 7;
	}
	
	public Bullet getFiredBullet(Player p, float playerDamage, float entityDamage){
		return new BulletSimple(this, playerDamage, entityDamage);
	}
	
	@Override
	protected float getFireVolume() {
		return CommonGunConstants.SOUND_VOLUME_MEDIUM;
	}
	
	@Override
	protected String getFireSound() {
		return "zta.guns.auto";
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.AUTOMATIC;
	}
	
}
