package fr.olympa.zta.weapons.guns.accessories;

import org.bukkit.Material;

import fr.olympa.zta.utils.AttributeModifier;
import fr.olympa.zta.utils.AttributeModifier.Operation;
import fr.olympa.zta.weapons.guns.Gun;

public class StockLight extends Stock{

	private static final AttributeModifier modifier = new AttributeModifier("stock", Operation.MULTIPLY_VALUE, 0.8f);
	
	public StockLight(int id) {
		super(id);
	}
	
	public String getName(){
		return "Crosse légère";
	}
	
	public Material getItemMaterial(){
		return Material.SPRUCE_FENCE_GATE;
	}
	
	private static String[] desc = new String[] { "réduit le recul" };
	public String[] getEffectsDescription(){
		return desc;
	}
	
	public void apply(Gun gun){
		gun.knockback.addModifier(modifier);
	}
	
	public void remove(Gun gun){
		gun.knockback.removeModifier(modifier.getName());
	}
	
}
