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

public class GunLupara extends Gun{
	
	public static final String NAME = "Lupara";
	public static final Material TYPE = Material.GOLDEN_SHOVEL;

	private static final PotionEffect effect = new PotionEffect(PotionEffectType.WITHER, 40, 1);
	
	public GunLupara(int id) {
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
		return 1;
	}
	
	protected int getFireRate(){
		return -1;
	}
	
	protected int getChargeTime(){
		return 10;
	}
	
	protected float getKnockback(){
		return 0;
	}
	
	protected float getBulletSpeed(){
		return CommonGunConstants.BULLET_SPEED_LOW;
	}
	
	protected GunAccuracy getAccuracy(){
		return GunAccuracy.LOW;
	}
	
	@Override
	protected float getBulletPlayerDamage() {
		return 5;
	}
	
	@Override
	protected float getBulletEntityDamage() {
		return 8;
	}
	
	public Bullet getFiredBullet(Player p, float playerDamage, float entityDamage){
		return new BulletEffect(this, playerDamage, entityDamage, effect);
	}
	
	@Override
	public int getFiredBulletsAmount() {
		return 2;
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.AUTOMATIC;
	}
	
	public boolean isStockAllowed(){
		return true;
	}
	
}
