package fr.olympa.zta.weapons.guns.accessories;

import org.bukkit.Material;

import fr.olympa.zta.utils.AttributeModifier;
import fr.olympa.zta.utils.AttributeModifier.Operation;
import fr.olympa.zta.weapons.guns.Gun;

public class StockStrong extends Stock{
	
	public static final String NAME = "Crosse lourde";
	public static final Material TYPE = Material.BIRCH_FENCE_GATE;

	private static final AttributeModifier modifier = new AttributeModifier("stock", Operation.MULTIPLY_VALUE, 0.8f);
	
	public StockStrong(int id) {
		super(id);
	}

	public String getName(){
		return NAME;
	}
	
	public Material getItemMaterial(){
		return TYPE;
	}
	
	private static String[] desc = new String[] { "réduit la dispersion des balles" };
	public String[] getEffectsDescription(){
		return desc;
	}
	
	public void apply(Gun gun){
		gun.bulletSpread.addModifier(modifier);
	}
	
	public void remove(Gun gun){
		gun.bulletSpread.removeModifier(modifier.getName());
	}
	
}
