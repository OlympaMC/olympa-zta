package fr.olympa.zta.weapons.guns.created;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.CommonGunConstants;
import fr.olympa.zta.weapons.guns.Gun;
import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.BulletExplosive;

public class GunBazooka extends Gun {
	
	public static final String NAME = "Bazooka";
	public static final Material TYPE = Material.GOLDEN_HOE;
	
	public GunBazooka(int id) {
		super(id);
	}
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public Material getItemMaterial() {
		return TYPE;
	}
	
	@Override
	public AmmoType getAmmoType() {
		return AmmoType.HEAVY;
	}
	
	@Override
	protected int getMaxAmmos() {
		return 2;
	}
	
	@Override
	protected int getFireRate() {
		return 40;
	}
	
	@Override
	protected int getChargeTime() {
		return 70;
	}
	
	@Override
	protected float getKnockback() {
		return CommonGunConstants.KNOCKBACK_HIGH;
	}
	
	@Override
	protected float getBulletSpeed() {
		return CommonGunConstants.BULLET_SPEED_LOW;
	}
	
	@Override
	protected GunAccuracy getAccuracy() {
		return GunAccuracy.MEDIUM;
	}
	
	@Override
	protected float getBulletPlayerDamage() {
		return 0;
	}
	
	@Override
	protected float getBulletEntityDamage() {
		return 0;
	}
	
	@Override
	public Bullet getFiredBullet(Player p, float playerDamage, float entityDamage) {
		return new BulletExplosive(this, 5);
	}
	
	@Override
	public GunMode getPrimaryMode() {
		return GunMode.SINGLE;
	}
	
}
