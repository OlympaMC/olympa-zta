package fr.olympa.zta.weapons.guns.accessories;

import org.bukkit.entity.Player;

public abstract class Scope extends Accessory{
	
	public Scope(int id) {
		super(id);
	}

	public void zoomToggled(Player p, boolean zoom){}
	
	public AccessoryType getType() {
		return AccessoryType.SCOPE;
	}

}
