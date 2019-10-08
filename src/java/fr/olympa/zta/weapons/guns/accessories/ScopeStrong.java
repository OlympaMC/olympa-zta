package fr.olympa.zta.weapons.guns.accessories;

import org.bukkit.Material;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;

import fr.olympa.zta.weapons.guns.Gun;

public class ScopeStrong extends Scope{
	
	private static final AttributeModifier zoomModifier = new AttributeModifier("zoom", -3, Operation.ADD_SCALAR);
	
	public String getName(){
		return "Lunette x3";
	}
	
	public Material getItemMaterial(){
		return Material.NETHER_BRICK;
	}
	
	private static String[] desc = new String[] { "dispose d'un zoom puissant" };
	public String[] getEffectsDescription(){
		return desc;
	}
	
	public void apply(Gun gun){
		gun.zoomModifier = zoomModifier;
	}
	
	public void remove(Gun gun){
		gun.zoomModifier = null;
	}
	
}
