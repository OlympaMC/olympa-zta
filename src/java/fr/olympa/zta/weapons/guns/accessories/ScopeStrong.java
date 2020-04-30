package fr.olympa.zta.weapons.guns.accessories;

import org.bukkit.Material;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;

import fr.olympa.zta.weapons.guns.Gun;

public class ScopeStrong extends Scope{
	
	public static final String NAME = "Lunette x3";
	public static final Material TYPE = Material.NETHER_BRICK;

	private static final AttributeModifier zoomModifier = new AttributeModifier("zoom", -3, Operation.ADD_SCALAR);
	
	public ScopeStrong(int id) {
		super(id);
	}

	public String getName(){
		return NAME;
	}
	
	public Material getItemMaterial(){
		return TYPE;
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
