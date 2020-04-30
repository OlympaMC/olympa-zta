package fr.olympa.zta.weapons.guns.accessories;

import org.bukkit.Material;

import fr.olympa.zta.utils.AttributeModifier;
import fr.olympa.zta.utils.AttributeModifier.Operation;
import fr.olympa.zta.weapons.guns.Gun;

public class CannonStabilizer extends Cannon{

	public static final String NAME = "Stabilisateur";
	public static final Material TYPE = Material.GHAST_TEAR;

	private static final AttributeModifier rateModifier = new AttributeModifier("cannon", Operation.MULTIPLY_VALUE, 1.2f);
	private static final AttributeModifier spreadModifier = new AttributeModifier("cannon", Operation.MULTIPLY_VALUE, 0.8f);
	
	public CannonStabilizer(int id) {
		super(id);
	}

	public String getName(){
		return NAME;
	}
	
	public Material getItemMaterial(){
		return TYPE;
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
		gun.fireRate.removeModifier(rateModifier.getName());
		gun.bulletSpread.removeModifier(spreadModifier.getName());
	}
	
}
