package fr.olympa.zta.weapons.guns.accessories;

import org.bukkit.Material;

import fr.olympa.zta.utils.AttributeModifier;
import fr.olympa.zta.utils.AttributeModifier.Operation;
import fr.olympa.zta.weapons.guns.Gun;

public class CannonStabilizer extends Cannon{

	private static final AttributeModifier rateModifier = new AttributeModifier("cannon", Operation.MULTIPLY_VALUE, 1.2f);
	private static final AttributeModifier spreadModifier = new AttributeModifier("cannon", Operation.MULTIPLY_VALUE, 0.8f);
	
	public CannonStabilizer(int id) {
		super(id);
	}

	public String getName(){
		return "Stabilisateur";
	}
	
	public Material getItemMaterial(){
		return Material.GHAST_TEAR;
	}
	
	private static String[] desc = new String[] { "augmente la cadence de tir", "diminue la dispersion des balles" };
	public String[] getEffectsDescription(){
		return desc;
	}
	
	public void apply(Gun gun){
		gun.fireRate.addModifier(rateModifier);
		gun.bulletSpread.addModifier(spreadModifier);
	}
	
	public void remove(Gun gun){
		gun.fireRate.removeModifier(rateModifier);
		gun.bulletSpread.removeModifier(spreadModifier);
	}
	
}
