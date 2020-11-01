package fr.olympa.zta.weapons.guns;

import org.bukkit.entity.Player;

import fr.olympa.api.region.tracking.flags.AbstractProtectionFlag;

public class GunFlag extends AbstractProtectionFlag {

	private boolean freeAmmos;
	
	public GunFlag(boolean protectedByDefault, boolean freeAmmos) {
		super(protectedByDefault);
		this.freeAmmos = freeAmmos;
	}

	public boolean isFireEnabled(Player p) {
		return !protectedByDefault || !applies(p);
	}
	
	public boolean isFreeAmmos() {
		return freeAmmos;
	}

}
