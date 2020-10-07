package fr.olympa.zta.weapons.guns.created;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.CommonGunConstants;
import fr.olympa.zta.weapons.guns.Gun;
import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.BulletEffect;

public class GunCobra extends Gun{
	
	public static final String NAME = "King Cobra";
	public static final Material TYPE = Material.STONE_AXE;

	private static final PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 40, 1);
	
	public GunCobra(int id) {
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
	
	public int getMaxAmmos(){
		return 6;
	}
	
	public int getFireRate(){
		return 15;
	}
	
	public int getChargeTime(){
		return 80;
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
	
	@Override
	protected float getBulletPlayerDamage() {
		return 4;
	}
	
	@Override
	protected float getBulletEntityDamage() {
		return 4;
	}
	
	public Bullet getFiredBullet(Player p, float playerDamage, float entityDamage){
		return new BulletEffect(this, playerDamage, entityDamage, effect);
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.SINGLE;
	}
	
}
