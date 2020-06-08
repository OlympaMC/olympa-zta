package fr.olympa.zta.weapons.guns.created;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.CommonGunConstants;
import fr.olympa.zta.weapons.guns.Gun;
import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.BulletEffect;

public class GunStoner extends Gun{
	
	public static final String NAME = "Stoner 24";
	public static final Material TYPE = Material.IRON_HORSE_ARMOR;

	private static final PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 40, 1);
	private static final PotionEffect playerEffect = new PotionEffect(PotionEffectType.SLOW, 99999999, 1);
	
	public GunStoner(int id) {
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
	
	protected int getMaxAmmos(){
		return 55;
	}
	
	protected int getFireRate(){
		return 15;
	}
	
	protected int getChargeTime(){
		return 70;
	}
	
	protected float getKnockback(){
		return CommonGunConstants.KNOCKBACK_HIGH;
	}
	
	protected float getBulletSpeed(){
		return CommonGunConstants.BULLET_SPEED_MEDIUM;
	}
	
	protected GunAccuracy getAccuracy(){
		return GunAccuracy.MEDIUM;
	}
	
	public Bullet getFiredBullet(Player p){
		return new BulletEffect(this, 2, 3, effect);
	}
	
	public GunMode getPrimaryMode(){
		return GunMode.AUTOMATIC;
	}
	
	public boolean isCannonAllowed(){
		return true;
	}
	
	public boolean isStockAllowed(){
		return true;
	}
	
	public void itemHeld(Player p, ItemStack item){
		super.itemHeld(p, item);
		p.addPotionEffect(playerEffect);
	}
	
	public void itemNoLongerHeld(Player p, ItemStack item){
		super.itemNoLongerHeld(p, item);
		p.removePotionEffect(PotionEffectType.SLOW);
	}
	
}
