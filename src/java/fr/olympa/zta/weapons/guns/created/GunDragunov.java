package fr.olympa.zta.weapons.guns.created;

import org.bukkit.Material;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.CommonGunConstants;
import fr.olympa.zta.weapons.guns.Gun;
import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.BulletEffect;

public class GunDragunov extends Gun{
	
	public static final String NAME = "Dragunov";
	public static final Material TYPE = Material.GOLDEN_HOE;

	private static final AttributeModifier zoomModifier = new AttributeModifier("zoom", -1, Operation.ADD_SCALAR);
	private static final PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 20, 1);
	
	public GunDragunov(int id) {
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
		return 1;
	}
	
	protected int getFireRate(){
		return -1;
	}
	
	protected int getChargeTime(){
		return 60;
	}
	
	protected float getKnockback(){
		return CommonGunConstants.KNOCKBACK_HIGH;
	}
	
	protected float getBulletSpeed(){
		return CommonGunConstants.BULLET_SPEED_ULTRA_HIGH;
	}
	
	protected GunAccuracy getAccuracy(){
		return GunAccuracy.EXTREME;
	}
	
	@Override
	protected float getBulletPlayerDamage() {
		return 14;
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
	
	public AttributeModifier getZoomModifier(){
		return zoomModifier;
	}
	
	@Override
	protected float getFireVolume() {
		return CommonGunConstants.SOUND_VOLUME_HIGH;
	}
	
	public boolean isCannonAllowed(){
		return true;
	}
	
}
