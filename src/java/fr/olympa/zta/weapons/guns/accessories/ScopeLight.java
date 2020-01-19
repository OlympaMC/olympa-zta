package fr.olympa.zta.weapons.guns.accessories;

import org.bukkit.Material;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.olympa.zta.weapons.guns.Gun;

public class ScopeLight extends Scope{

	private static final AttributeModifier zoomModifier = new AttributeModifier("zoom", -1, Operation.ADD_SCALAR);
	private static final PotionEffect effect = new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 0);
	
	public ScopeLight(int id) {
		super(id);
	}

	public String getName(){
		return "Mire V2";
	}
	
	public Material getItemMaterial(){
		return Material.BRICK;
	}
	
	private static String[] desc = new String[] { "dispose d'un zoom faible", "donne la vision nocturne" };
	public String[] getEffectsDescription(){
		return desc;
	}
	
	public void apply(Gun gun){
		gun.zoomModifier = zoomModifier;
	}
	
	public void remove(Gun gun){
		gun.zoomModifier = null;
	}
	
	public void zoomToggled(Player p, boolean zoom){
		if (zoom) {
			p.addPotionEffect(effect);
		}else p.removePotionEffect(effect.getType());
	}
	
}
