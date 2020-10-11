package fr.olympa.zta.weapons.guns.created;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.CommonGunConstants;
import fr.olympa.zta.weapons.guns.Gun;
import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.BulletSimple;

public class GunSDMR extends Gun{
	
	public static final String NAME = "SDMR";
	public static final Material TYPE = Material.IRON_PICKAXE;

	public GunSDMR(int id) {
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
	
	protected int getMaxAmmos(){
		return 20;
	}
	
	protected int getFireRate(){
		return 6;
	}
	
	protected int getChargeTime(){
		return 50;
	}
	
	protected float getKnockback(){
		return CommonGunConstants.KNOCKBACK_MEDIUM;
	}
	
	protected float getBulletSpeed(){
		return CommonGunConstants.BULLET_SPEED_HIGH;
	}
	
	protected GunAccuracy getAccuracy(){
		return GunAccuracy.HIGH;
	}
	
	@Override
	protected float getBulletPlayerDamage() {
		return 5;
	}
	
	@Override
	protected float getBulletEntityDamage() {
		return 6;
	}
	
	public Bullet getFiredBullet(Player p, float playerDamage, float entityDamage){
		return new BulletSimple(this, playerDamage, entityDamage);
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.AUTOMATIC;
	}
	
	/*public GunMode getSecondaryMode(){
		return GunMode.SEMI_AUTOMATIC;
	}*/
	
	@Override
	protected float getFireVolume() {
		return CommonGunConstants.SOUND_VOLUME_MEDIUM;
	}
	
	@Override
	protected String getFireSound() {
		return "zta.guns.auto";
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
