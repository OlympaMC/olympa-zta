package fr.olympa.zta.weapons.knives;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import fr.olympa.zta.weapons.Weapon;
import fr.tristiisch.olympa.api.item.OlympaItemBuild;

public abstract class Knife extends Weapon{
	
	public String getInternalName(){
		return getClass().getSimpleName().substring(5); // nom de classe sans "Knife"
	}
	
	public ItemStack createItemStack(){
		return new OlympaItemBuild(getItemMaterial(), getName()).lore(
				"Dégâts aux joueurs: " + getPlayerDamage(),
				"Dégâts aux monstres: " + getEntityDamage()).build();
	}
	
	public void onDrop(PlayerDropItemEvent e){}
	
	public void onInteract(PlayerInteractEvent e){}
	
	public void onEntityHit(EntityDamageByEntityEvent e){
		if (e.getEntity() instanceof Player) {
			e.setDamage(getPlayerDamage());
		}else e.setDamage(getEntityDamage());
	}
	
	public abstract float getPlayerDamage();
	
	public abstract float getEntityDamage();
	
}
