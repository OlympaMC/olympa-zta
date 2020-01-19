package fr.olympa.zta.weapons.guns.accessories;

import org.bukkit.Material;

import fr.olympa.zta.weapons.guns.Gun;

public class CannonDamage extends Cannon{
	
	public CannonDamage(int id) {
		super(id);
	}

	public String getName(){
		return "Canon V2";
	}
	
	public Material getItemMaterial(){
		return Material.SUGAR;
	}
	
	private static String[] desc = new String[] { "augmente le dégât des balles (+1)" };
	public String[] getEffectsDescription(){
		return desc;
	}
	
	public void apply(Gun gun){
		gun.damageAdded = 1;
	}
	
	public void remove(Gun gun){
		gun.damageAdded = 0;
	}
	
}
