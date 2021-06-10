package fr.olympa.zta.weapons.guns.bullets;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;

import fr.olympa.zta.weapons.guns.Gun;

public class BulletEffect extends BulletSimple{
	
	private PotionEffect effect;
	
	public BulletEffect(Gun gun, float playerDamage, float entityDamage, PotionEffect effect){
		super(gun, playerDamage, entityDamage);
		this.effect = effect;
	}
	
	public void damage(LivingEntity entity, LivingEntity damager, float damage){
		super.damage(entity, damager, damage);
		entity.addPotionEffect(effect);
	}
	
	public static class BulletEffectCreator implements BulletCreator {
		
		private PotionEffect effect;
		
		public BulletEffectCreator(PotionEffect effect) {
			this.effect = effect;
		}
		
		@Override
		public Bullet create(Gun gun, float playerDamage, float entityDamage) {
			return new BulletEffect(gun, playerDamage, entityDamage, effect);
		}
		
	}
	
}
