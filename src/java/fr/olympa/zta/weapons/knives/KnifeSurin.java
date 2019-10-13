package fr.olympa.zta.weapons.knives;

import org.bukkit.Material;

public class KnifeSurin extends Knife{

	public String getName(){
		return "Surin";
	}

	public Material getItemMaterial(){
		return Material.STICK;
	}

	public float getPlayerDamage(){
		return 4;
	}

	public float getEntityDamage(){
		return 2;
	}
	
}
