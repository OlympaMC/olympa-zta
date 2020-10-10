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

public class GunM1897 extends Gun{
	
	public static final String NAME = "M1897";
	public static final Material TYPE = Material.WOODEN_SHOVEL;

	private static final PotionEffect effect = new PotionEffect(PotionEffectType.WITHER, 40, 1);
	
	public GunM1897(int id) {
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
	
	public int getMaxAmmos(){
		return 5;
	}
	
	public int getFireRate(){
		return 30;
	}
	
	public int getChargeTime(){
		return 15;
	}
	
	protected boolean isOneByOneCharge(){
		return true;
	}
	
	public float getKnockback(){
		return CommonGunConstants.KNOCKBACK_MEDIUM;
	}
	
	public float getBulletSpeed(){
		return CommonGunConstants.BULLET_SPEED_ULTRA_LOW;
	}
	
	protected GunAccuracy getAccuracy(){
		return GunAccuracy.LOW;
	}
	
	@Override
	protected float getBulletPlayerDamage() {
		return 7;
	}
	
	@Override
	protected float getBulletEntityDamage() {
		return 10;
	}
	
	public Bullet getFiredBullet(Player p, float playerDamage, float entityDamage){
		return new BulletEffect(this, playerDamage, entityDamage, effect);
	}
	
	@Override
	public int getFiredBulletsAmount() {
		return 5;
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.SINGLE;
	}
	
	@Override
	protected String getFireSound() {
		return "zta.guns.pump";
	}
	
	@Override
	protected float getFireVolume() {
		return CommonGunConstants.SOUND_VOLUME_MEDIUM;
	}
	
	public boolean isStockAllowed(){
		return true;
	}
	
}
