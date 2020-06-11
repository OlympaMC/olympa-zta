package fr.olympa.zta.weapons.guns.accessories;

import org.bukkit.Material;

import fr.olympa.zta.weapons.guns.Gun;

public class CannonCaC extends Cannon {

	public static final String NAME = "Baïonnette";
	public static final Material TYPE = Material.GLOWSTONE_DUST;
	
	public CannonCaC(int id) {
		super(id);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Material getItemMaterial() {
		return TYPE;
	}
	
	private static String[] desc = new String[] { "augmente les dégâts au corps-à-corps (+4)" };
	public String[] getEffectsDescription(){
		return desc;
	}

	@Override
	public void apply(Gun gun) {
		gun.damageCaC = 4;
	}

	@Override
	public void remove(Gun gun) {
		gun.damageCaC = 0;
	}

}
