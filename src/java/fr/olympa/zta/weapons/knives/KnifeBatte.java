package fr.olympa.zta.weapons.knives;

import org.bukkit.Material;

public class KnifeBatte extends Knife{
	
	public static final String NAME = "Batte";
	public static final Material TYPE = Material.BLAZE_ROD;

	public KnifeBatte(int id) {
		super(id);
	}

	public String getName(){
		return NAME;
	}
	
	public Material getItemMaterial(){
		return TYPE;
	}
	
	public float getPlayerDamage(){
		return 2;
	}
	
	public float getEntityDamage(){
		return 4;
	}
	
}
