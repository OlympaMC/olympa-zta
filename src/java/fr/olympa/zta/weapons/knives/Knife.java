package fr.olympa.zta.weapons.knives;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.zta.weapons.Weapon;

public abstract class Knife extends Weapon{
	
	public ItemStack createItemStack(){
		return ItemUtils.item(getItemMaterial(), getName(),
				"Dégâts aux joueurs: " + getPlayerDamage(),
				"Dégâts aux monstres: " + getEntityDamage(),
				"",
				"Arme immatriculée: [I" + id + "]");
	}
	
	public void onInteract(PlayerInteractEvent e){}
	
	public void onEntityHit(EntityDamageByEntityEvent e){
		if (e.getEntity() instanceof Player) {
			e.setDamage(getPlayerDamage());
		}else e.setDamage(getEntityDamage());
	}
	
	public boolean drop(Player p, ItemStack item){
		return false;
	}
	
	public abstract float getPlayerDamage();
	
	public abstract float getEntityDamage();
	
}
