package fr.olympa.zta.weapons;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public interface Weapon {
	
	public default void onInteract(PlayerInteractEvent e) {}
	
	public default void onEntityHit(EntityDamageByEntityEvent e) {}
	
	/**
	 * Appelé quand un joueur drop l'item avec le clavier
	 * @param p Joueur qui a drop l'item
	 * @param item Item droppé
	 * @return <tt>true</tt> si l'action est annulée
	 */
	public default boolean drop(Player p, ItemStack item) {
		return false;
	}
	
	public default void itemHeld(Player p, ItemStack item, Weapon previous) {}
	
	public default void itemNoLongerHeld(Player p, ItemStack item) {}
	
}
