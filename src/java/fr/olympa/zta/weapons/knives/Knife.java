package fr.olympa.zta.weapons.knives;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.zta.weapons.Weapon;

public abstract class Knife extends Weapon{
	
	public Knife(int id) {
		super(id);
	}

	public ItemStack createItemStack(){
		List<String> lore = new ArrayList<>(Arrays.asList(
				getFeatureLoreLine("Dégâts aux joueurs", getPlayerDamage()),
				getFeatureLoreLine("Dégâts aux monstres", getEntityDamage())));
		lore.addAll(getIDLoreLines());
		return addIdentifier(ItemUtils.item(getItemMaterial(), "§b" + getName(), lore.toArray(new String[0])));
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
