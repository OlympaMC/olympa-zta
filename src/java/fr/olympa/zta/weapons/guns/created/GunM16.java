package fr.olympa.zta.weapons.guns.created;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.CommonGunConstants;
import fr.olympa.zta.weapons.guns.Gun;
import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.BulletSimple;

public class GunM16 extends Gun{
	
	public static final String NAME = "M16";
	public static final Material TYPE = Material.WOODEN_PICKAXE;

	public GunM16(int id) {
		super(id);
	}

	public String getName(){
		return NAME;
	}
	
	public Material getItemMaterial(){
		return TYPE;
	}
	
	public AmmoType getAmmoType(){
		return AmmoType.HEAVY;
	}
	
	public int getMaxAmmos(){
		return 20;
	}
	
	public int getFireRate(){
		return 8;
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
		return GunMode.AUTOMATIC;
	}
	
	public GunMode getSecondaryMode(){
		return GunMode.BLAST;
	}
	
	@Override
	protected float getFireVolume() {
		return CommonGunConstants.SOUND_VOLUME_MEDIUM;
	}
	
	@Override
	protected String getFireSound() {
		return "zta.guns.auto";
	}
	
	public boolean isCannonAllowed(){
		return true;
	}
	
	public boolean isStockAllowed(){
		return true;
	}
	
}
