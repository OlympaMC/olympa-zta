package fr.olympa.zta.weapons.guns.accessories;

import org.bukkit.Material;

import fr.olympa.zta.utils.AttributeModifier;
import fr.olympa.zta.utils.AttributeModifier.Operation;
import fr.olympa.zta.weapons.guns.Gun;

public class CannonSilent extends Cannon{

	public static final String NAME = "Silencieux";
	public static final Material TYPE = Material.REDSTONE;

	private static final AttributeModifier modifier = new AttributeModifier("cannon", Operation.MULTIPLY_VALUE, 0.25f);
	
	public CannonSilent(int id) {
		super(id);
	}
	
	public String getName(){
		return NAME;
	}
	
	public Material getItemMaterial(){
		return TYPE;
	}
	
	private static String[] desc = new String[] { "réduit le volume de la détonation" };
	public String[] getEffectsDescription(){
		return desc;
	}
	
	public void apply(Gun gun){
		gun.fireVolume.addModifier(modifier);
	}
	
	public void remove(Gun gun){
		gun.fireVolume.removeModifier(modifier.getName());
	}
	
}
