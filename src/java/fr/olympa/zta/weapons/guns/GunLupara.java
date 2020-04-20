package fr.olympa.zta.weapons.guns;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.BulletEffect;

public class GunLupara extends Gun{
	
	private static final PotionEffect effect = new PotionEffect(PotionEffectType.WITHER, 40, 1);
	
	public GunLupara(int id) {
		super(id);
	}

	public String getName(){
		return "Lupara";
	}
	
	public Material getItemMaterial(){
		return Material.GOLDEN_SHOVEL;
	}
	
	public AmmoType getAmmoType(){
		return AmmoType.CARTRIDGE;
	}
	
	protected int getMaxAmmos(){
		return 2;
	}
	
	protected int getFireRate(){
		return 8;
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
	
	public Bullet getFiredBullet(Player p){
		return new BulletEffect(this, 5, 8, effect);
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.AUTOMATIC;
	}
	
	public boolean isStockAllowed(){
		return true;
	}
	
}
