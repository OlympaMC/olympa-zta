package fr.olympa.zta.weapons.guns.accessories;

import org.bukkit.Material;

import fr.olympa.zta.utils.AttributeModifier;
import fr.olympa.zta.utils.AttributeModifier.Operation;
import fr.olympa.zta.weapons.guns.Gun;

public class StockStrong extends Stock{
	
	private static final AttributeModifier modifier = new AttributeModifier("stock", Operation.MULTIPLY_VALUE, 0.8f);
	
	public String getName(){
		return "Crosse lourde";
	}
	
	public Material getItemMaterial(){
		return Material.BIRCH_FENCE_GATE;
	}
	
	private static String[] desc = new String[] { "réduit la dispersion des balles" };
	public String[] getEffectsDescription(){
		return desc;
	}
	
	public void apply(Gun gun){
		gun.bulletSpread.addModifier(modifier);
	}
	
	public void remove(Gun gun){
		gun.bulletSpread.removeModifier(modifier);
	}
	
}
