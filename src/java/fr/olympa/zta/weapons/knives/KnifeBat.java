package fr.olympa.zta.weapons.knives;

import org.bukkit.Material;

public class KnifeBat extends Knife{
	
	public String getName(){
		return "Batte";
	}
	
	public Material getItemMaterial(){
		return Material.BLAZE_ROD;
	}
	
	public float getPlayerDamage(){
		return 2;
	}
	
	public float getEntityDamage(){
		return 4;
	}
	
}
