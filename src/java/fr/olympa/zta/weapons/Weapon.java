package fr.olympa.zta.weapons;

import java.util.Random;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public abstract class Weapon implements ItemStackable{
	
	// Système de cassage d'arme etc. ?
	
	public final Integer id = new Random().nextInt();
	
	public abstract String getInternalName();
	
	public abstract void onInteract(PlayerInteractEvent e);
	
	public abstract void onEntityHit(EntityDamageByEntityEvent e);
	
	public abstract void onDrop(PlayerDropItemEvent e);
	
}
