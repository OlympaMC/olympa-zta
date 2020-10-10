package fr.olympa.zta.weapons.guns.created;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.CommonGunConstants;
import fr.olympa.zta.weapons.guns.Gun;
import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.BulletSimple;

public class Gun870 extends Gun{
	
	public static final String NAME = "Remington 870 Express";
	public static final Material TYPE = Material.WOODEN_HOE;

	public Gun870(int id) {
		super(id);
	}

	public String getName(){
		return NAME;
	}
	
	public Material getItemMaterial(){
		return TYPE;
	}
	
	public AmmoType getAmmoType(){
		return AmmoType.CARTRIDGE;
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
	
	@Override
	protected boolean isOneByOneCharge() {
		return true;
	}
	
	public float getKnockback(){
		return CommonGunConstants.KNOCKBACK_LOW;
	}
	
	public float getBulletSpeed(){
		return CommonGunConstants.BULLET_SPEED_LOW;
	}
	
	protected GunAccuracy getAccuracy(){
		return GunAccuracy.MEDIUM;
	}
	
	@Override
	protected float getBulletPlayerDamage() {
		return 4;
	}
	
	@Override
	protected float getBulletEntityDamage() {
		return 5;
	}
	
	public Bullet getFiredBullet(Player p, float playerDamage, float entityDamage){
		return new BulletSimple(this, playerDamage, entityDamage);
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.SINGLE;
	}
	
	@Override
	protected float getFireVolume() {
		return CommonGunConstants.SOUND_VOLUME_MEDIUM;
	}
	
	@Override
	protected String getFireSound() {
		return "zta.guns.pump";
	}
	
	public boolean isCannonAllowed(){
		return true;
	}
	
	public boolean isScopeAllowed(){
		return true;
	}
	
}
