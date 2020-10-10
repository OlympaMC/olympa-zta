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
		return 14;
	}
	
	protected int getChargeTime(){
		return 100;
	}
	
	protected float getKnockback(){
		return CommonGunConstants.KNOCKBACK_LOW;
	}
	
	protected float getBulletSpeed(){
		return CommonGunConstants.BULLET_SPEED_ULTRA_LOW;
	}
	
	protected GunAccuracy getAccuracy(){
		return GunAccuracy.MEDIUM;
	}
	
	@Override
	protected float getBulletPlayerDamage() {
		return 3;
	}
	
	@Override
	protected float getBulletEntityDamage() {
		return 6;
	}
	
	public Bullet getFiredBullet(Player p, float playerDamage, float entityDamage){
		return new BulletEffect(this, playerDamage, entityDamage, effect);
	}
	
	@Override
	public int getFiredBulletsAmount() {
		return 5;
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.SEMI_AUTOMATIC;
	}
	
	@Override
	protected float getFireVolume() {
		return CommonGunConstants.SOUND_VOLUME_MEDIUM;
	}
	
	@Override
	protected String getFireSound() {
		return "zta.guns.pump";
	}
	
	public boolean isStockAllowed(){
		return true;
	}
	
}
