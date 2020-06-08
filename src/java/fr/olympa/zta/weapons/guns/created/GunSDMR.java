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
	
	public Bullet getFiredBullet(Player p){
		return new BulletSimple(this, 5, 6);
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.AUTOMATIC;
	}
	
	/*public GunMode getSecondaryMode(){
		return GunMode.SEMI_AUTOMATIC;
	}*/

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
