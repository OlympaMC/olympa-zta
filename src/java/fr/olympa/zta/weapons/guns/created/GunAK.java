package fr.olympa.zta.weapons.guns.created;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.CommonGunConstants;
import fr.olympa.zta.weapons.guns.Gun;
import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.BulletSimple;

public class GunAK extends Gun{
	
	public static final String NAME = "AK-20";
	public static final Material TYPE = Material.GOLDEN_PICKAXE;

	public GunAK(int id) {
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
		return 20;
	}
	
	protected int getFireRate(){
		return 5;
	}
	
	protected int getChargeTime(){
		return 70;
	}
	
	protected float getKnockback(){
		return CommonGunConstants.KNOCKBACK_LOW;
	}
	
	protected float getBulletSpeed(){
		return CommonGunConstants.BULLET_SPEED_MEDIUM;
	}
	
	protected GunAccuracy getAccuracy(){
		return GunAccuracy.HIGH;
	}
	
	@Override
	protected float getBulletPlayerDamage() {
		return 6;
	}
	
	@Override
	protected float getBulletEntityDamage() {
		return 3;
	}
	
	public Bullet getFiredBullet(Player p, float playerDamage, float entityDamage){
		return new BulletSimple(this, playerDamage, entityDamage);
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.AUTOMATIC;
	}
	
	@Override
	protected float getFireVolume() {
		return CommonGunConstants.SOUND_VOLUME_MEDIUM;
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
