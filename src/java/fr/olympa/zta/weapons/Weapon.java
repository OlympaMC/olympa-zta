package fr.olympa.zta.weapons;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public interface Weapon {
	
	public abstract void onInteract(PlayerInteractEvent e);
	
	public abstract void onEntityHit(EntityDamageByEntityEvent e);
	
	/**
	 * Appelé quand un joueur drop l'item avec le clavier
	 * @param p Joueur qui a drop l'item
	 * @param item Item droppé
	 * @return <tt>true</tt> si l'action est annulée
	 */
	public abstract boolean drop(Player p, ItemStack item);
	
	public abstract void itemHeld(Player p, ItemStack item, Weapon previous);
	
	public abstract void itemNoLongerHeld(Player p, ItemStack item);
	
}
