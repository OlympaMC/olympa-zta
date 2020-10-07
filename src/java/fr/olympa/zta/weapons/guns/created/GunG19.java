package fr.olympa.zta.weapons.guns.created;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.CommonGunConstants;
import fr.olympa.zta.weapons.guns.Gun;
import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.BulletSimple;

public class GunG19 extends Gun{
	
	public static final String NAME = "Glock 19";
	public static final Material TYPE = Material.GOLDEN_AXE;

	public GunG19(int id) {
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
		return 15;
	}
	
	protected int getFireRate(){
		return 10;
	}
	
	protected int getChargeTime(){
		return 40;
	}
	
	protected float getKnockback(){
		return 0;
	}
	
	protected float getBulletSpeed(){
		return CommonGunConstants.BULLET_SPEED_MEDIUM;
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
		return 4;
	}
	
	public Bullet getFiredBullet(Player p, float playerDamage, float entityDamage){
		return new BulletSimple(this, playerDamage, entityDamage);
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.AUTOMATIC;
	}
	
	public GunMode getSecondaryMode(){
		return GunMode.SEMI_AUTOMATIC;
	}
	
	public boolean isCannonAllowed(){
		return true;
	}
	
}
