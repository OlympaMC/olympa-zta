package fr.olympa.zta.weapons.knives;

import org.bukkit.Material;

public class KnifeBiche extends Knife{
	
	public String getName(){
		return "Pied-de-biche";
	}
	
	public Material getItemMaterial(){
		return Material.ARROW;
	}
	
	public float getPlayerDamage(){
		return 3;
	}
	
	public float getEntityDamage(){
		return 3;
	}
	
}
