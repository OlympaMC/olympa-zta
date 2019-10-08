package fr.olympa.zta.weapons.guns;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.BulletEffect;

public class GunM1897 extends Gun{
	
	private static final PotionEffect effect = new PotionEffect(PotionEffectType.WITHER, 20, 0);
	
	public String getName(){
		return "M1897";
	}
	
	public Material getItemMaterial(){
		return Material.WOODEN_SHOVEL;
	}
	
	public AmmoType getAmmoType(){
		return AmmoType.CARTRIDGE;
	}
	
	public int getMaxAmmos(){
		return 5;
	}
	
	public int getFireRate(){
		return 30;
	}
	
	public int getChargeTime(){
		return 20;
	}
	
	public float getKnockback(){
		return 0.5f;
	}
	
	public float getBulletSpeed(){
		return Bullet.SPEED_LOW;
	}
	
	protected float getBulletSpread(){
		return 0.2f;
	}
	
	public Bullet getFiredBullet(Player p){
		return new BulletEffect(this, 7, 10, effect);
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.SINGLE;
	}
	
	public boolean isStockAllowed(){
		return true;
	}
	
}
