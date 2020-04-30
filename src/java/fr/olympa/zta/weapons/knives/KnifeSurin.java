package fr.olympa.zta.weapons.knives;

import org.bukkit.Material;

public class KnifeSurin extends Knife{

	public static final String NAME = "Surin";
	public static final Material TYPE = Material.STICK;

	public KnifeSurin(int id) {
		super(id);
	}

	public String getName(){
		return NAME;
	}

	public Material getItemMaterial(){
		return TYPE;
	}

	public float getPlayerDamage(){
		return 4;
	}

	public float getEntityDamage(){
		return 2;
	}
	
}
