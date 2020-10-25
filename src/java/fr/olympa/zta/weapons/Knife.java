package fr.olympa.zta.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.olympa.api.utils.spigot.SpigotUtils;

public enum Knife implements Weapon, ItemStackable {
	
	MATRAQUE(Material.BLAZE_ROD, "Matraque", "Arme contondante, peu puissante contre les joueurs, utile pour se défendre contre les infectés.", 2, 3),
	BICHE(Material.ARROW, "Pied-de-biche", "Objet polyvalent contre les joueurs comme contre les infectés.", 3, 3),
	SURIN(Material.STICK, "Surin", "Couteau perforant, occasionnant plus de dégâts chez les joueurs que chez les infectés.", 4, 2),
	;
	
	private static final PotionEffect SPEED_EFFECT = new PotionEffect(PotionEffectType.SPEED, 9999999, 0, false, false);
	
	private final float playerDamage, entityDamage;
	
	private final ItemStack item;
	
	private Knife(Material material, String name, String description, float playerDamage, float entityDamage) {
		this.playerDamage = playerDamage;
		this.entityDamage = entityDamage;
		
		item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§b" + name);
		meta.setLore(SpigotUtils.wrapAndAlign(description, 35));
		meta.getPersistentDataContainer().set(WeaponsListener.KNIFE_KEY, PersistentDataType.INTEGER, ordinal());
		meta.setCustomModelData(1);
		item.setItemMeta(meta);
	}
	
	@Override
	public ItemStack createItem() {
		return item.clone();
	}
	
	public boolean isItem(ItemStack item) {
		return this == WeaponsListener.getWeapon(item);
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
