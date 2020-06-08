package fr.olympa.zta.weapons.guns.created;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.CommonGunConstants;
import fr.olympa.zta.weapons.guns.Gun;
import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.BulletSimple;

public class GunP22 extends Gun{
	
	public static final String NAME = "P22";
	public static final Material TYPE = Material.IRON_AXE;

	public GunP22(int id) {
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
	
	protected int getMaxAmmos(){
		return 8;
	}
	
	protected int getFireRate(){
		return 10;
	}
	
	protected int getChargeTime(){
		return 30;
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
	
	public Bullet getFiredBullet(Player p){
		return new BulletSimple(this, 4, 3);
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.SEMI_AUTOMATIC;
	}
	
	public GunMode getSecondaryMode(){
		return GunMode.BLAST;
	}
	
	public boolean isCannonAllowed(){
		return true;
	}
	
}
