package fr.olympa.zta.weapons.knives;

import org.bukkit.Material;

public class KnifeBiche extends Knife{
	
	public static final String NAME = "Pied-de-biche";
	public static final Material TYPE = Material.ARROW;

	public KnifeBiche(int id) {
		super(id);
	}

	public String getName(){
		return NAME;
	}
	
	public Material getItemMaterial(){
		return TYPE;
	}
	
	public float getPlayerDamage(){
		return 3;
	}
	
	public float getEntityDamage(){
		return 3;
	}
	
}
