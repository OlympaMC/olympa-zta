package fr.olympa.zta.weapons.knives;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.olympa.zta.weapons.Weapon;

public enum Knife implements Weapon {
	
	BATTE(Material.BLAZE_ROD, "Batte", 2, 3),
	BICHE(Material.ARROW, "Pied-de-biche", 3, 3),
	SURIN(Material.STICK, "Surin", 4, 2),
	;
	
	private static final PotionEffect SPEED_EFFECT = new PotionEffect(PotionEffectType.SPEED, 9999999, 0, false, false);
	
	private final float playerDamage, entityDamage;
	
	private final ItemStack item;
	
	private Knife(Material material, String name, float playerDamage, float entityDamage) {
		this.playerDamage = playerDamage;
		this.entityDamage = entityDamage;
		
		item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§b" + name);
		meta.setLore(Arrays.asList(Weapon.getFeatureLoreLine("Dégâts aux joueurs", playerDamage), Weapon.getFeatureLoreLine("Dégâts aux monstres", entityDamage)));
		meta.setCustomModelData(1);
		item.setItemMeta(meta);
	}
	
	public ItemStack getItem() {
		return item.clone();
	}
	
	public void onInteract(PlayerInteractEvent e){}
	
	public void onEntityHit(EntityDamageByEntityEvent e){
		if (e.getEntity() instanceof Player) {
			e.setDamage(playerDamage);
		}else e.setDamage(entityDamage);
	}
	
	public boolean drop(Player p, ItemStack item){
		return false;
	}
	
	@Override
	public void itemHeld(Player p, ItemStack item) {
		p.addPotionEffect(SPEED_EFFECT);
	}
	
	@Override
	public void itemNoLongerHeld(Player p, ItemStack item) {
		p.removePotionEffect(PotionEffectType.SPEED);
	}
	
}
