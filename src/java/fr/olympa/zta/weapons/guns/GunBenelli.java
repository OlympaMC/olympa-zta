package fr.olympa.zta.weapons.guns;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.BulletEffect;

public class GunBenelli extends Gun{
	
	public static final String NAME = "Benelli M5 Super";
	public static final Material TYPE = Material.STONE_SHOVEL;

	private static final PotionEffect effect = new PotionEffect(PotionEffectType.WITHER, 40, 1);
	
	public GunBenelli(int id) {
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
	
	protected int getMaxAmmos(){
		return 8;
	}
	
	protected int getFireRate(){
		return 30;
	}
	
	protected int getChargeTime(){
		return 15;
	}
	
	protected boolean isOneByOneCharge(){
		return true;
	}
	
	protected float getKnockback(){
		return CommonGunConstants.KNOCKBACK_LOW;
	}
	
	protected float getBulletSpeed(){
		return CommonGunConstants.BULLET_SPEED_LOW;
	}
	
	protected GunAccuracy getAccuracy(){
		return GunAccuracy.LOW;
	}
	
	public Bullet getFiredBullet(Player p){
		return new BulletEffect(this, 5, 8, effect);
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.SEMI_AUTOMATIC;
	}
	
}
