package fr.olympa.zta.weapons.guns.accessories;

import org.bukkit.Material;

import fr.olympa.zta.utils.AttributeModifier;
import fr.olympa.zta.utils.AttributeModifier.Operation;
import fr.olympa.zta.weapons.guns.Gun;

public class CannonPower extends Cannon{
	
	private static final AttributeModifier modifier = new AttributeModifier("cannon", Operation.MULTIPLY_VALUE, 1.2f);
	
	public String getName(){
		return "Canon lourd";
	}
	
	public Material getItemMaterial(){
		return Material.GUNPOWDER;
	}
	
	private static String[] desc = new String[] { "augmente la vitesse des balles" };
	public String[] getEffectsDescription(){
		return desc;
	}
	
	public void apply(Gun gun){
		gun.bulletSpeed.addModifier(modifier);
	}
	
	public void remove(Gun gun){
		gun.bulletSpeed.removeModifier(modifier);
	}
	
}
