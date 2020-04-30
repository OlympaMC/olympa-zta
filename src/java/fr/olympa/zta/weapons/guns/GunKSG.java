package fr.olympa.zta.weapons.guns;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.BulletEffect;

public class GunKSG extends Gun{
	
	public static final String NAME = "KSG";
	public static final Material TYPE = Material.IRON_SHOVEL;

	private static final PotionEffect effect = new PotionEffect(PotionEffectType.WITHER, 40, 1);
	
	public GunKSG(int id) {
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
		return 10;
	}
	
	protected int getFireRate(){
		return 30;
	}
	
	protected int getChargeTime(){
		return 50;
	}
	
	protected float getKnockback(){
		return CommonGunConstants.KNOCKBACK_LOW;
	}
	
	protected float getBulletSpeed(){
		return CommonGunConstants.BULLET_SPEED_LOW;
	}
	
	protected GunAccuracy getAccuracy(){
		return GunAccuracy.MEDIUM;
	}
	
	public Bullet getFiredBullet(Player p){
		return new BulletEffect(this, 3, 6, effect);
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.SEMI_AUTOMATIC;
	}
	
	public boolean isStockAllowed(){
		return true;
	}
	
}
