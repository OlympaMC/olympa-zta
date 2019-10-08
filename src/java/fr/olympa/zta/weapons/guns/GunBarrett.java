package fr.olympa.zta.weapons.guns;

import org.bukkit.Material;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.BulletEffect;

/**
 * Barrett M109, sniper, munitions lourdes
 */
public class GunBarrett extends Gun{
	
	private static final AttributeModifier zoomModifier = new AttributeModifier("zoom", -3, Operation.ADD_SCALAR);
	private static final PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 20, 1);
	
	public String getName(){
		return "Barrett M109";
	}
	
	public Material getItemMaterial(){
		return Material.IRON_HOE;
	}
	
	public AmmoType getAmmoType(){
		return AmmoType.HEAVY;
	}
	
	public int getMaxAmmos(){
		return 1;
	}
	
	public int getFireRate(){
		return -1; // 1 seule munition = pas de fire rate
	}
	
	public int getChargeTime(){
		return 80;
	}
	
	public float getKnockback(){
		return 1f;
	}
	
	public float getBulletSpeed(){
		return Bullet.SPEED_HIGH;
	}
	
	protected float getBulletSpread(){
		return 0;
	}
	
	public AttributeModifier getZoomModifier(){
		return zoomModifier;
	}
	
	public Bullet getFiredBullet(Player p){
		return new BulletEffect(this, 9, 10, effect);
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.SINGLE;
	}
	
	public boolean isCannonAllowed(){
		return true;
	}
	
}
