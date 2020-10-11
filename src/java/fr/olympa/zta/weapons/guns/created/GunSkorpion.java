package fr.olympa.zta.weapons.guns.created;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.CommonGunConstants;
import fr.olympa.zta.weapons.guns.Gun;
import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.BulletSimple;

public class GunSkorpion extends Gun{
	
	public static final String NAME = "Skorpion VZ64";
	public static final Material TYPE = Material.MAGMA_CREAM;

	public GunSkorpion(int id) {
		super(id);
	}

	public String getName(){
		return NAME;
	}
	
	public Material getItemMaterial(){
		return TYPE;
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
	
	@Override
	protected float getBulletPlayerDamage() {
		return 3;
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
