package fr.olympa.zta.weapons.guns.created;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.olympa.zta.utils.AttributeModifier;
import fr.olympa.zta.utils.AttributeModifier.Operation;
import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.CommonGunConstants;
import fr.olympa.zta.weapons.guns.Gun;
import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.BulletEffect;

/**
 * Barrett M109, sniper, munitions lourdes
 */
public class GunBarrett extends Gun{
	
	public static final String NAME = "Barrett M109";
	public static final Material TYPE = Material.IRON_HOE;

	private static final AttributeModifier zoomModifier = new AttributeModifier("zoom", Operation.ADD_MULTIPLICATOR, -3);
	private static final PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 40, 1);
	
	public GunBarrett(int id) {
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
		return 1;
	}
	
	public int getFireRate(){
		return -1; // 1 seule munition = pas de fire rate
	}
	
	public int getChargeTime(){
		return 80;
	}
	
	public float getKnockback(){
		return CommonGunConstants.KNOCKBACK_HIGH;
	}
	
	public float getBulletSpeed(){
		return CommonGunConstants.BULLET_SPEED_ULTRA_HIGH;
	}
	
	protected GunAccuracy getAccuracy(){
		return GunAccuracy.EXTREME;
	}
	
	public AttributeModifier getZoomModifier(){
		return zoomModifier;
	}
	
	@Override
	protected float getBulletPlayerDamage() {
		return 9;
	}
	
	@Override
	protected float getBulletEntityDamage() {
		return 10;
	}
	
	public Bullet getFiredBullet(Player p, float playerDamage, float entityDamage){
		return new BulletEffect(this, playerDamage, entityDamage, effect);
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.SINGLE;
	}
	
	@Override
	protected float getFireVolume() {
		return CommonGunConstants.SOUND_VOLUME_HIGH;
	}
	
	@Override
	protected String getFireSound() {
		return "zta.guns.barrett";
	}
	
	public boolean isCannonAllowed(){
		return true;
	}
	
}
