package fr.olympa.zta.weapons.guns;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.BulletEffect;

public class GunCobra extends Gun{
	
	private static final PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 40, 1);
	
	public GunCobra(int id) {
		super(id);
	}

	public String getName(){
		return "King Cobra";
	}
	
	public Material getItemMaterial(){
		return Material.STONE_AXE;
	}
	
	public AmmoType getAmmoType(){
		return AmmoType.HEAVY;
	}
	
	public int getMaxAmmos(){
		return 6;
	}
	
	public int getFireRate(){
		return 15;
	}
	
	public int getChargeTime(){
		return 90;
	}
	
	public float getKnockback(){
		return CommonGunConstants.KNOCKBACK_LOW;
	}
	
	public float getBulletSpeed(){
		return CommonGunConstants.BULLET_SPEED_HIGH;
	}
	
	protected GunAccuracy getAccuracy(){
		return GunAccuracy.EXTREME;
	}
	
	public Bullet getFiredBullet(Player p){
		return new BulletEffect(this, 4, 5, effect);
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.SINGLE;
	}
	
}
